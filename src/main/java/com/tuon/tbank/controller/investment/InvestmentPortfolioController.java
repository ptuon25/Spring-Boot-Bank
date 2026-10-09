package com.tuon.tbank.controller.investment;

import com.tuon.tbank.entity.investment.InvestmentPortfolio;
import com.tuon.tbank.service.investment.InvestmentPortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/investment-portfolios")
public class InvestmentPortfolioController {

    private final InvestmentPortfolioService investmentPortfolioService;

    public InvestmentPortfolioController(InvestmentPortfolioService investmentPortfolioService) {
        this.investmentPortfolioService = investmentPortfolioService;
    }

    @GetMapping
    public ResponseEntity<List<InvestmentPortfolio>> getAllInvesmentPortfolios(){
        List<InvestmentPortfolio> investmentPortfolios = investmentPortfolioService.getAllInvestmentPortfolios();
        return ResponseEntity.ok().body(investmentPortfolios);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<InvestmentPortfolio> getInvestmentPortfolioById(@PathVariable Long id){
        InvestmentPortfolio investmentPortfolio = investmentPortfolioService.getInvestmentPortfolioById(id);
        return ResponseEntity.ok().body(investmentPortfolio);
    }
}
