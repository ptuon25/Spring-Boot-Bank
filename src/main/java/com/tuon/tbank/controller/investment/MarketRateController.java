package com.tuon.tbank.controller.investment;

import com.tuon.tbank.entity.investment.MarketRate;
import com.tuon.tbank.service.investment.MarketRateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/market-ratings")
public class MarketRateController {

    private final MarketRateService marketRateService;

    public MarketRateController(MarketRateService marketRateService) {
        this.marketRateService = marketRateService;
    }

    @GetMapping
    public ResponseEntity<List<MarketRate>> getAllMarketRates(){
        List<MarketRate> marketRates = marketRateService.getAllMarketRates();
        return ResponseEntity.ok().body(marketRates);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<MarketRate> getMarketRateById(@PathVariable Long id){
        MarketRate marketRate = marketRateService.getMarketRateById(id);
        return ResponseEntity.ok().body(marketRate);
    }
}
