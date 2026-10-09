package com.tuon.tbank.controller.investment;

import com.tuon.tbank.entity.investment.Investment;
import com.tuon.tbank.service.investment.InvestmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/investments")
public class InvestmentController {

    private final InvestmentService investmentService;

    public InvestmentController(InvestmentService investmentService) {
        this.investmentService = investmentService;
    }

    @GetMapping
    public ResponseEntity<List<Investment>> getAllInvestments(){
        List<Investment> investments = investmentService.getAllInvestments();
        return ResponseEntity.ok().body(investments);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Investment> getInvestmentById(@PathVariable Long id){
        Investment investment = investmentService.getInvestmentById(id);
        return ResponseEntity.ok().body(investment);
    }
}
