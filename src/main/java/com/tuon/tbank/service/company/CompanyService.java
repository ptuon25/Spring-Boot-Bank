package com.tuon.tbank.service.company;

import com.tuon.tbank.entity.company.Company;
import com.tuon.tbank.exception.ResourceNotFoundException;
import com.tuon.tbank.repository.company.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public List<Company> getAllCompanies(){
        return companyRepository.findAll();
    }

    public Company getCompanyById(Long id){
        Optional<Company> company = companyRepository.findById(id);
        return company.orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
    }
}
