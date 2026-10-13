package com.tuon.tbank.service.transaction;

import com.tuon.tbank.entity.transaction.reversalTransaction.TransactionReversal;
import com.tuon.tbank.exception.serviceException.ResourceNotFoundException;
import com.tuon.tbank.repository.transaction.TransactionReversalRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionReversalService {

    private final TransactionReversalRepository transactionReversalRepository;

    public TransactionReversalService(TransactionReversalRepository transactionReversalRepository) {
        this.transactionReversalRepository = transactionReversalRepository;
    }

    public List<TransactionReversal> getAllTransactionReversals(){
        return transactionReversalRepository.findAll();
    }

    public TransactionReversal getTransactionReversalById(Long id){
        Optional<TransactionReversal> transactionReversal = transactionReversalRepository.findById(id);
        return transactionReversal.orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
    }
}
