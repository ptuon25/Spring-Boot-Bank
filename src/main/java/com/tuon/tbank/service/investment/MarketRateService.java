package com.tuon.tbank.service.investment;

import com.tuon.tbank.entity.investment.MarketRate;
import com.tuon.tbank.exception.serviceException.ResourceNotFoundException;
import com.tuon.tbank.repository.investment.MarketRateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MarketRateService {

    private final MarketRateRepository marketRateRepository;

    public MarketRateService(MarketRateRepository marketRateRepository) {
        this.marketRateRepository = marketRateRepository;
    }

    public List<MarketRate> getAllMarketRates(){
        return marketRateRepository.findAll();
    }

    public MarketRate getMarketRateById(Long id){
        Optional<MarketRate> marketRate = marketRateRepository.findById(id);
        return marketRate.orElseThrow(() -> new ResourceNotFoundException("Market rate not found with id: " + id));
    }
}
