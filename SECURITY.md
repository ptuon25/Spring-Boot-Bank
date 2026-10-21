# Autenticacao e autorizacao

## Componentes

- `config.SecurityConfig`: BCrypt, `AuthenticationManager`, codificacao/validacao JWT, API stateless e regras de acesso.
- `security.auth.BankUserDetailsService` e `BankUserDetails`: carrega cliente/funcionario pelos repositorios existentes, exige status ativo e monta authorities sem alterar as entidades.
- `security.auth.JwtService`: emite tokens HS256 com identificador, tipo de identidade, authorities, emissor e timestamps.
- `security.auth.ClientAuthorization`: permite a leitura do proprio perfil do cliente ou por funcionario `ADMIN`, validando tambem a identidade JWT.
- `security.auth.EmployeeAuthorization`: exige identidade `EMPLOYEE` e authority de cargo para consultas internas e operacoes administrativas de clientes.
- `service.auth.AuthService` e `controller.auth.AuthController`: autenticam com `AuthenticationManager` e retornam DTOs sem dados de entidade.
- `dto.auth` e `dto.person.ClientProfileResponse`: DTOs validados de login/cadastro e respostas limitadas, sem entidades nem relacionamentos privados.
- `ClientService.registerClient`: codifica a senha com BCrypt antes de persistir. O cadastro publico nao recebe status, data de afiliacao nem hash.
- `Person.passwordHash` e marcado para nunca ser serializado. As atualizacoes cadastrais existentes continuam sem modificar a senha.

Foi adicionada `spring-boot-starter-oauth2-resource-server`, que fornece a integracao Resource Server e Nimbus JOSE/JWT gerenciada pelo BOM do Spring Boot 4.1.1. As dependencias Spring Security, Validation e BCrypt ja existiam por meio dos starters atuais.

## Configuracao

Configure estas variaveis para iniciar fora do perfil de teste:

| Variavel | Uso |
|---|---|
| `DB_URL` | JDBC URL PostgreSQL, por exemplo `jdbc:postgresql://localhost:5432/tbank` |
| `DB_USERNAME` | Usuario PostgreSQL |
| `DB_PASSWORD` | Senha PostgreSQL |
| `JWT_SECRET` | Chave HS256 em Base64, com no minimo 32 bytes depois de decodificada |
| `JWT_EXPIRATION` | Duracao ISO-8601 opcional; padrao `PT15M` |
| `JWT_ISSUER` | Emissor JWT opcional; padrao `tbank` |

Gere uma chave exclusiva para o ambiente (nao reutilize a chave fixa do perfil de teste). No PowerShell, por exemplo:

```powershell
$bytes = [byte[]]::new(32)
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$env:DB_URL = "jdbc:postgresql://localhost:5432/tbank"
$env:DB_USERNAME = "tbank"
$env:DB_PASSWORD = "<senha-do-banco>"
.\mvnw.cmd spring-boot:run
```

O projeto ainda nao define migracoes de schema; o schema PostgreSQL precisa existir antes de iniciar. O perfil padrao nao ativa o H2 Console nem carrega dados de demonstração. Para executar localmente sem PostgreSQL, use o perfil `test`:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=test
```

Esse perfil usa H2 em memoria, disponibiliza o H2 Console apenas para testes e inclui usuarios de demonstracao. A chave e as senhas semeadas nesse perfil sao exclusivas de teste e nao devem ser copiadas para outro ambiente.

## Login e Postman

Envie `POST /auth/login` com `Content-Type: application/json`:

```json
{
  "email": "alice_johnson@tbank.com",
  "password": "senha-da-conta"
}
```

A resposta contem `accessToken`, `tokenType` (`Bearer`) e `expiresIn` em segundos. Para proteger uma requisicao, envie:

```http
Authorization: Bearer <token>
```

No Postman, selecione **Authorization > Bearer Token** e informe `accessToken`. Login aceita clientes e funcionarios; ambos precisam ter status `ACTIVE`. Credenciais incorretas, usuario inexistente ou conta inativa retornam a mesma resposta 401. Tokens expirados, malformados ou com assinatura/emissor invalidos tambem retornam 401.

No perfil `test`, os usuarios semeados para demonstracao sao:

| Identidade | E-mail | Senha de teste |
|---|---|---|
| Cliente ativo | `john_doe@hotmail.com` | `client-test-password` |
| Funcionario MANAGER | `alice_johnson@tbank.com` | `manager-test-password` |
| Funcionario ADMIN | `admin@tbank.test` | `admin-test-password` |

## Rotas e matriz provisoria

- Publicas: `POST /auth/login` e `POST /clients`. O cadastro recebe senha original e armazena somente BCrypt; nao aceita `passwordHash`.
- `GET /clients/{id}`: proprio cliente ou funcionario `ADMIN`.
- `GET /clients` e `GET /clients/search/name?name=...`, `/clients/search/status?status=...`, `/clients/search/email?email=...`, `/clients/search/document?document=...`: funcionarios `ASSOCIATE`, `MANAGER` ou `ADMIN`; clientes e empresas clientes nao podem consultar cadastros de terceiros.
- `GET /clients/deleted`, atualizacao e exclusao de clientes: somente funcionario `ADMIN`.
- `/employees/**` e `/audits/**`: somente `ADMIN`.
- `POST /business-accounts`: `MANAGER` ou `ADMIN`.
- Demais rotas existentes de contas, empresas, investimentos e transacoes: exigem funcionario autenticado (`ASSOCIATE`, `MANAGER` ou `ADMIN`). Clientes nao recebem acesso a recursos financeiros genericos enquanto nao houver regras/rotas que verifiquem a titularidade.
- Rotas nao listadas ficam protegidas e exigem autoridade de funcionario. Nenhuma permissao de funcionario e atribuida a clientes.

Esta matriz e uma politica conservadora inicial, nao substitui regras finais de negocio. Em particular, o acesso de cada papel a transacoes, contas, investimentos e operacoes administrativas deve ser confirmado antes de uso real.

`ClientService` tambem define `blockClient`, `deactivateClient` e `reactivateClient`. Esses metodos exigem identidade `EMPLOYEE` com authority `ROLE_ADMIN`; atualmente nao ha rotas HTTP para essas operacoes, portanto permanecem indisponiveis externamente.

CSRF esta desativado porque a API e stateless e aceita JWT exclusivamente no cabecalho Authorization; nao armazene o token em cookies enviados automaticamente pelo navegador. CORS nao e aberto globalmente. O H2 Console so pode ser permitido pelo perfil de teste.

## Limites

Tokens duram 15 minutos por padrao e nao ha refresh token, blacklist ou revogacao imediata. Desativar um usuario ou alterar seu papel nao invalida tokens ja emitidos; eles permanecem utilizaveis ate expirarem. Mantenha a chave secreta privada e rotacione-a para invalidar todos os tokens emitidos com a chave anterior.
