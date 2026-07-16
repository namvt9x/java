package com.userfront.resource;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.userfront.domain.Company;
import com.userfront.service.CompanyService;

@RestController
@RequestMapping("/api/company")
@PreAuthorize("hasRole('ADMIN')")
public class CompanyResource {

    @Autowired
    private CompanyService companyService;

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public List<Company> companyList() {
        return companyService.findAll();
    }

    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public Company getCompanyByUsername(@RequestParam("username") String username) {
        return companyService.findByUsername(username);
    }
}
