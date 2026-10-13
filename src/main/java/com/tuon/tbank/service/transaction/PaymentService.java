package com.tuon.tbank.service.transaction;

import com.tuon.tbank.entity.transaction.Payment;
import com.tuon.tbank.exception.serviceException.ResourceNotFoundException;
import com.tuon.tbank.repository.transaction.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public List<Payment> getAllPayments(){
        return paymentRepository.findAll();
    }

    public Payment getPaymentById(Long id){
        Optional<Payment> payment = paymentRepository.findById(id);
        return payment.orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
    }
}
