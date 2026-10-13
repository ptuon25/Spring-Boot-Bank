package com.tuon.tbank.service.investment;

import com.tuon.tbank.entity.investment.Investment;
import com.tuon.tbank.exception.serviceException.ResourceNotFoundException;
import com.tuon.tbank.repository.investment.InvestmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InvestmentService {

    private final InvestmentRepository investmentRepository;

    public InvestmentService(InvestmentRepository investmentRepository) {
        this.investmentRepository = investmentRepository;
    }

    public List<Investment> getAllInvestments(){
        return investmentRepository.findAll();
    }

    public Investment getInvestmentById(Long id){
        Optional<Investment> investment = investmentRepository.findById(id);
        return investment.orElseThrow(() -> new ResourceNotFoundException("Investment not found with id: " + id));
    }

}
