package com.tuon.tbank.controller.investment;

import com.tuon.tbank.entity.investment.InvestmentMonthlyReturn;
import com.tuon.tbank.service.investment.InvestmentMonthlyReturnService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/investment-monthly-returns")
public class InvestmentMonthlyReturnController {

    private final InvestmentMonthlyReturnService investmentMonthlyReturnService;

    public InvestmentMonthlyReturnController(InvestmentMonthlyReturnService investmentMonthlyReturnService) {
        this.investmentMonthlyReturnService = investmentMonthlyReturnService;
    }

    @GetMapping
    public ResponseEntity<List<InvestmentMonthlyReturn>> getAllInvestmentMonthlyReturns(){
        List<InvestmentMonthlyReturn> investmentMonthlyReturns = investmentMonthlyReturnService.getAllIMR();
        return ResponseEntity.ok().body(investmentMonthlyReturns);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<InvestmentMonthlyReturn> getInvestmentMonthlyReturnById(@PathVariable Long id){
        InvestmentMonthlyReturn investmentMonthlyReturn = investmentMonthlyReturnService.getIMRById(id);
        return ResponseEntity.ok().body(investmentMonthlyReturn);
    }
}
