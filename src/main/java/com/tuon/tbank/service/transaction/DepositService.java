package com.tuon.tbank.service.transaction;

import com.tuon.tbank.entity.transaction.Deposit;
import com.tuon.tbank.exception.ResourceNotFoundException;
import com.tuon.tbank.repository.transaction.DepositRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DepositService {

    private final DepositRepository depositRepository;

    public DepositService(DepositRepository depositRepository) {
        this.depositRepository = depositRepository;
    }

    public List<Deposit> getAllDeposits(){
        return depositRepository.findAll();
    }

    public Deposit getDepositById(Long id){
        Optional<Deposit> deposit = depositRepository.findById(id);
        return deposit.orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
    }
}
