package com.tuon.tbank.service.transaction;

import com.tuon.tbank.entity.transaction.Withdrawal;
import com.tuon.tbank.exception.ResourceNotFoundException;
import com.tuon.tbank.repository.transaction.WithdrawalRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WithdrawalService {

    private final WithdrawalRepository withdrawalRepository;

    public WithdrawalService(WithdrawalRepository withdrawalRepository) {
        this.withdrawalRepository = withdrawalRepository;
    }

    public List<Withdrawal> getAllWithdrawals(){
        return withdrawalRepository.findAll();
    }

    public Withdrawal getWithdrawalById(Long id){
        Optional<Withdrawal> withdrawal = withdrawalRepository.findById(id);
        return withdrawal.orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
    }
}
