package com.tuon.tbank.service.person;

import com.tuon.tbank.entity.person.Client;
import com.tuon.tbank.exception.ResourceNotFoundException;
import com.tuon.tbank.repository.person.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<Client> getAllClients(){
        return clientRepository.findAll();
    }

    public Client getClientById(Long id){
        Optional<Client> client = clientRepository.findById(id);
        return client.orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
    }
}
