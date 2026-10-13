package com.tuon.tbank.service.investment;

import com.tuon.tbank.entity.investment.InvestmentPortfolio;
import com.tuon.tbank.exception.serviceException.ResourceNotFoundException;
import com.tuon.tbank.repository.investment.InvestmentPortfolioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InvestmentPortfolioService {

    private final InvestmentPortfolioRepository investmentPortfolioRepository;

    public InvestmentPortfolioService(InvestmentPortfolioRepository investmentPortfolioRepository) {
        this.investmentPortfolioRepository = investmentPortfolioRepository;
    }

    public List<InvestmentPortfolio> getAllInvestmentPortfolios() {
        return investmentPortfolioRepository.findAll();
    }

    public InvestmentPortfolio getInvestmentPortfolioById(Long id) {
        Optional<InvestmentPortfolio> investmentPortfolio = investmentPortfolioRepository.findById(id);
        return investmentPortfolio.orElseThrow(() -> new ResourceNotFoundException("Investment Portfolio not found with id: " + id));
    }
}
