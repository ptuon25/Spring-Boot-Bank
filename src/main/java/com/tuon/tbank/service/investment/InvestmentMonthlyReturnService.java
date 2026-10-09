package com.tuon.tbank.service.investment;

import com.tuon.tbank.entity.investment.InvestmentMonthlyReturn;
import com.tuon.tbank.exception.ResourceNotFoundException;
import com.tuon.tbank.repository.investment.InvestmentMonthlyReturnRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InvestmentMonthlyReturnService {

    private final InvestmentMonthlyReturnRepository investmentMonthlyReturnRepository;

    public InvestmentMonthlyReturnService(InvestmentMonthlyReturnRepository investmentMonthlyReturnRepository) {
        this.investmentMonthlyReturnRepository = investmentMonthlyReturnRepository;
    }

    public List<InvestmentMonthlyReturn> getAllIMR(){
        return investmentMonthlyReturnRepository.findAll();
    }

    public InvestmentMonthlyReturn getIMRById(Long id){
        Optional<InvestmentMonthlyReturn> investmentMonthlyReturn = investmentMonthlyReturnRepository.findById(id);
        return investmentMonthlyReturn.orElseThrow(() -> new ResourceNotFoundException("Investment Monthly Return not found with id: " + id));
    }
}
