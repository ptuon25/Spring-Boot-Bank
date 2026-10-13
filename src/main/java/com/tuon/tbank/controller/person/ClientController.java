package com.tuon.tbank.controller.person;

import com.tuon.tbank.entity.person.Client;
import com.tuon.tbank.dto.auth.ClientRegistrationRequest;
import com.tuon.tbank.dto.auth.ClientRegistrationResponse;
import com.tuon.tbank.dto.person.ClientProfileResponse;
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
    public ResponseEntity<List<Client>> getAllClients(){
        List<Client> clients = clientService.getAllClients();
        return ResponseEntity.ok().body(clients);
    }

    @GetMapping(value = "/{id}")
    @PreAuthorize("@clientAuthorization.canReadClient(#id, authentication)")
    public ResponseEntity<ClientProfileResponse> getClientById(@PathVariable Long id){
        Client client = clientService.getClientById(id);
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

    @PutMapping (value = "/{id}")
    public ResponseEntity<Client> updateClient(@PathVariable Long id, @RequestBody Client client){
        Client obj = clientService.updateClient(id, client);
        return ResponseEntity.ok().body(obj);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id){
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }


}
