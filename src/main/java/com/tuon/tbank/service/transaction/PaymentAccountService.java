package com.tuon.tbank.service.transaction;

import com.tuon.tbank.entity.transaction.external.PaymentAccount;
import com.tuon.tbank.exception.ResourceNotFoundException;
import com.tuon.tbank.repository.transaction.PaymentAccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentAccountService {

    private final PaymentAccountRepository paymentAccountRepository;

    public PaymentAccountService(PaymentAccountRepository paymentAccountRepository) {
        this.paymentAccountRepository = paymentAccountRepository;
    }

    public List<PaymentAccount> getAllPaymentAccounts(){
        return paymentAccountRepository.findAll();
    }

    public PaymentAccount getPaymentAccountById(Long id){
        Optional<PaymentAccount> paymentAccount = paymentAccountRepository.findById(id);
        return paymentAccount.orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
    }
}
