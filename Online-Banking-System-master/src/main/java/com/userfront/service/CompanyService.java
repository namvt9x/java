package com.userfront.service;

import java.util.List;

import com.userfront.domain.Company;

public interface CompanyService {

    Company findByUsername(String username);

    Company saveForUser(String username, Company company);

    List<Company> findAll();
}
