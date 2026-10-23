package com.tuon.tbank.controller.person;

import com.tuon.tbank.dto.person.ClientPasswordChangeRequest;
import com.tuon.tbank.dto.person.ClientPasswordResetRequest;
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

/**
 * Exposes REST endpoints for client registration, retrieval, profile updates,
 * password management, status management, and logical deletion.
 */
@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    /**
     * Creates a client controller with the required service.
     *
     * @param clientService the service responsible for client business logic
     */
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    /**
     * Retrieves the profiles of all clients available for employee searches.
     *
     * @return a list of client profiles
     */
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

    /**
     * Retrieves the profiles of all logically deleted clients.
     *
     * @return a list of deleted client profiles
     */
    @GetMapping("/deleted")
    @PreAuthorize("@employeeAuthorization.canManageClients(authentication)")
    public ResponseEntity<List<ClientProfileResponse>> getAllDeletedClients() {
        List<Client> clients = clientService.getDeletedClients();
        List<ClientProfileResponse> response = clients.stream().
                map(client -> new ClientProfileResponse
                        (client.getId(), client.getName(), client.getEmail())).toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a client's profile by its unique identifier.
     *
     * @param id the unique identifier of the client
     * @return the client's profile
     */
    @GetMapping(value = "/{id}")
    @PreAuthorize("@clientAuthorization.canReadClient(#id, authentication)")
    public ResponseEntity<ClientProfileResponse> getClientById(@PathVariable Long id) {
        Client client = clientService.getClientById(id);
        return ResponseEntity.ok(new ClientProfileResponse(client.getId(), client.getName(), client.getEmail()));
    }

    /**
     * Searches for clients by name.
     *
     * @param name the name to search for
     * @return a list of matching client profiles
     */
    @GetMapping(value = "/search/name")
    @PreAuthorize("@employeeAuthorization.canSearchClients(authentication)")
    public ResponseEntity<List<ClientProfileResponse>> getClientByName(@RequestParam String name) {
        List<Client> clients = clientService.getClientByName(name);
        List<ClientProfileResponse> response = clients.stream().
                map(client -> new ClientProfileResponse(client.getId(), client.getName(), client.getEmail())).toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a client's profile by email address.
     *
     * @param email the email address to search for
     * @return the matching client profile
     */
    @GetMapping(value = "/search/email")
    @PreAuthorize("@employeeAuthorization.canSearchClients(authentication)")
    public ResponseEntity<ClientProfileResponse> getClientByEmail(@RequestParam String email) {
        Client client = clientService.getClientByEmail(email);
        return ResponseEntity.ok(new ClientProfileResponse(client.getId(), client.getName(), client.getEmail()));
    }

    /**
     * Retrieves the profiles of clients with the specified status.
     *
     * @param status the client status to search for
     * @return a list of matching client profiles
     */
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

    /**
     * Retrieves a client's profile by identification document.
     *
     * @param document the identification document to search for
     * @return the matching client profile
     */
    @GetMapping(value = "/search/document")
    @PreAuthorize("@employeeAuthorization.canManageClients(authentication)")
    public ResponseEntity<ClientProfileResponse> getClientByDocument(@RequestParam String document) {
        Client client = clientService.getClientByDocument(document);
        return ResponseEntity.ok(new ClientProfileResponse(client.getId(), client.getName(), client.getEmail()));
    }

    /**
     * Blocks a client and returns the updated client profile.
     *
     * @param id the unique identifier of the client to block
     * @return the updated client profile
     */
    @PatchMapping(value = "/{id}/block")
    @PreAuthorize("@employeeAuthorization.canManageClients(authentication)")
    public ResponseEntity<ClientProfileResponse> blockClient(@PathVariable Long id) {
        Client client = clientService.blockClient(id);
        return ResponseEntity.ok(new ClientProfileResponse(client.getId(), client.getName(), client.getEmail()));
    }

    /**
     * Deactivates a client and returns the updated client profile.
     *
     * @param id the unique identifier of the client to deactivate
     * @return the updated client profile
     */
    @PatchMapping(value = "/{id}/deactivate")
    @PreAuthorize("@employeeAuthorization.canManageClients(authentication)")
    public ResponseEntity<ClientProfileResponse> deactivateClient(@PathVariable Long id) {
        Client client = clientService.deactivateClient(id);
        return ResponseEntity.ok(new ClientProfileResponse(client.getId(), client.getName(), client.getEmail()));
    }

    /**
     * Reactivates a client and returns the updated client profile.
     *
     * @param id the unique identifier of the client to reactivate
     * @return the updated client profile
     */
    @PatchMapping(value = "/{id}/reactivate")
    @PreAuthorize("@employeeAuthorization.canManageClients(authentication)")
    public ResponseEntity<ClientProfileResponse> reactivateClient(@PathVariable Long id) {
        Client client = clientService.reactivateClient(id);
        return ResponseEntity.ok(new ClientProfileResponse(client.getId(), client.getName(), client.getEmail()));
    }

    /**
     * Registers a new client using the data provided in the request.
     * Returns HTTP 201 Created with the registration response and the URI
     * of the newly created resource in the Location header.
     *
     * @param request the client registration data
     * @return the registration response and resource location
     */
    @PostMapping
    public ResponseEntity<ClientRegistrationResponse> insertClient(
            @Valid @RequestBody ClientRegistrationRequest request) {
        Client obj = clientService.registerClient(request.name(), request.birthDate(), request.document(),
                request.email(), request.password());
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(obj.getId()).toUri();
        return ResponseEntity.created(uri).body(new ClientRegistrationResponse(obj.getId(), obj.getName(), obj.getEmail()));
    }

    /**
     * Updates an existing client's profile using the data provided in the request.
     *
     * @param id      the unique identifier of the client to update
     * @param request the updated client information
     * @return the updated client profile
     */
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

    /**
     * Changes a client's password after receiving the current and new passwords.
     * Returns HTTP 204 No Content when the operation succeeds.
     *
     * @param id      the unique identifier of the client
     * @param request the current and new password data
     * @return an empty response with HTTP 204 No Content
     */
    @PatchMapping("/{id}/password")
    @PreAuthorize("@clientAuthorization.isOwner(#id, authentication)")
    public ResponseEntity<Void> changeOwnPassword(@PathVariable Long id,
                                                  @Valid @RequestBody ClientPasswordChangeRequest request) {
        clientService.changeOwnPassword(id, request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    /**
     * Resets a client's password using the new password provided by an
     * authorized employee. Returns HTTP 204 No Content when the operation succeeds.
     *
     * @param id      the unique identifier of the client
     * @param request the new password data
     * @return an empty response with HTTP 204 No Content
     */
    @PatchMapping("/{id}/password/reset")
    @PreAuthorize("@employeeAuthorization.canManageClients(authentication)")
    public ResponseEntity<Void> resetClientPassword(@PathVariable Long id,
                                                    @Valid @RequestBody ClientPasswordResetRequest request) {
        clientService.resetClientPassword(id, request.newPassword());
        return ResponseEntity.noContent().build();
    }

    /**
     * Logically deletes a client.
     * Returns HTTP 204 No Content when the operation succeeds.
     *
     * @param id the unique identifier of the client to delete
     * @return an empty response with HTTP 204 No Content
     */
    @DeleteMapping(value = "/{id}")
    @PreAuthorize("@employeeAuthorization.canManageClients(authentication)")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }


}
