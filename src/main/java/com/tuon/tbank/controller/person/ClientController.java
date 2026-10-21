package com.tuon.tbank.controller.person;

import com.tuon.tbank.dto.person.ClientUpdateRequest;
import com.tuon.tbank.entity.person.Client;
import com.tuon.tbank.dto.auth.ClientRegistrationRequest;
import com.tuon.tbank.dto.auth.ClientRegistrationResponse;
import com.tuon.tbank.dto.person.ClientProfileResponse;
import com.tuon.tbank.enums.person.ClientStatus;
import com.tuon.tbank.service.person.ClientService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    @PreAuthorize("@employeeAuthorization.canSearchClients(authentication)")
    public ResponseEntity<List<ClientProfileResponse>> getAllClients() {
        List<Client> clients = clientService.getAllClients();
        List<ClientProfileResponse> response = clients.stream().
                map(client -> new ClientProfileResponse
                        (client.getId(), client.getName(), client.getEmail()))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/deleted")
    @PreAuthorize("@employeeAuthorization.canManageClients(authentication)")
    public ResponseEntity<List<ClientProfileResponse>> getAllDeletedClients() {
        List<Client> clients = clientService.getDeletedClients();
        List<ClientProfileResponse> response = clients.stream().
                map(client -> new ClientProfileResponse
                        (client.getId(), client.getName(), client.getEmail())).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{id}")
    @PreAuthorize("@clientAuthorization.canReadClient(#id, authentication)")
    public ResponseEntity<ClientProfileResponse> getClientById(@PathVariable Long id) {
        Client client = clientService.getClientById(id);
        return ResponseEntity.ok(new ClientProfileResponse(client.getId(), client.getName(), client.getEmail()));
    }

    @GetMapping(value = "/search/name")
    @PreAuthorize("@employeeAuthorization.canSearchClients(authentication)")
    public ResponseEntity<List<ClientProfileResponse>> getClientByName(@RequestParam String name) {
        List<Client> clients = clientService.getClientByName(name);
        List<ClientProfileResponse> response = clients.stream().
                map(client -> new ClientProfileResponse(client.getId(), client.getName(), client.getEmail())).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/search/email")
    @PreAuthorize("@employeeAuthorization.canSearchClients(authentication)")
    public ResponseEntity<ClientProfileResponse> getClientByEmail(@RequestParam String email) {
        Client client = clientService.getClientByEmail(email);
        return ResponseEntity.ok(new ClientProfileResponse(client.getId(), client.getName(), client.getEmail()));
    }

    @GetMapping(value = "/search/status")
    @PreAuthorize("@employeeAuthorization.canSearchClients(authentication)")
    public ResponseEntity<List<ClientProfileResponse>> getClientByStatus(
            @RequestParam ClientStatus status) {
        List<Client> clients = clientService.getClientByStatus(status);
        List<ClientProfileResponse> response = clients.stream()
                .map(client -> new ClientProfileResponse(client.getId(), client.getName(), client.getEmail()))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/search/document")
    @PreAuthorize("@employeeAuthorization.canManageClients(authentication)")
    public ResponseEntity<ClientProfileResponse> getClientByDocument(@RequestParam String document) {
        Client client = clientService.getClientByDocument(document);
        return ResponseEntity.ok(new ClientProfileResponse(client.getId(), client.getName(), client.getEmail()));
    }

    @PatchMapping(value = "/{id}/block")
    @PreAuthorize("@employeeAuthorization.canManageClients(authentication)")
    public ResponseEntity<ClientProfileResponse> blockClient(@PathVariable Long id){
        Client client = clientService.blockClient(id);
        return ResponseEntity.ok(new ClientProfileResponse(client.getId(), client.getName(), client.getEmail()));
    }

    @PatchMapping(value = "/{id}/deactivate")
    @PreAuthorize("@employeeAuthorization.canManageClients(authentication)")
    public ResponseEntity<ClientProfileResponse> deactivateClient(@PathVariable Long id){
        Client client = clientService.deactivateClient(id);
        return ResponseEntity.ok(new ClientProfileResponse(client.getId(), client.getName(), client.getEmail()));
    }

    @PatchMapping(value = "/{id}/reactivate")
    @PreAuthorize("@employeeAuthorization.canManageClients(authentication)")
    public ResponseEntity<ClientProfileResponse> reactivateClient(@PathVariable Long id){
        Client client = clientService.reactivateClient(id);
        return ResponseEntity.ok(new ClientProfileResponse(client.getId(), client.getName(), client.getEmail()));
    }

    @PostMapping
    public ResponseEntity<ClientRegistrationResponse> insertClient(
            @Valid @RequestBody ClientRegistrationRequest request) {
        Client obj = clientService.registerClient(request.name(), request.birthDate(), request.document(),
                request.email(), request.password());
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(obj.getId()).toUri();
        return ResponseEntity.created(uri).body(new ClientRegistrationResponse(obj.getId(), obj.getName(), obj.getEmail()));
    }

    @PutMapping(value = "/{id}")
    @PreAuthorize("@employeeAuthorization.canManageClients(authentication)")
    public ResponseEntity<ClientProfileResponse> updateClient(@PathVariable Long id, @Valid @RequestBody ClientUpdateRequest request) {
        Client client = new Client();
        client.setName(request.name());
        client.setBirthDate(request.birthDate());
        client.setDocument(request.document());
        client.setEmail(request.email());
        Client obj = clientService.updateClient(id, client);
        return ResponseEntity.ok(new ClientProfileResponse(obj.getId(), obj.getName(), obj.getEmail()));
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("@employeeAuthorization.canManageClients(authentication)")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }


}
