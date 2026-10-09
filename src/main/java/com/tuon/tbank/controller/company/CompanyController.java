package com.tuon.tbank.controller.company;

import com.tuon.tbank.entity.company.Company;
import com.tuon.tbank.service.company.CompanyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public ResponseEntity<List<Company>> getAllCompanies(){
        List<Company> companies = companyService.getAllCompanies();
        return ResponseEntity.ok().body(companies);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Company> getClientById(@PathVariable Long id){
        Company company = companyService.getCompanyById(id);
        return ResponseEntity.ok().body(company);
    }

}
