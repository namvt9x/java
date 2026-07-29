package com.userfront.service;

import java.util.List;

import com.userfront.domain.Company;
import com.userfront.domain.User;

public interface CompanyService {

    List<Company> findAll();

    Company findById(Long companyId);

    Company createCompany(Company company);

    Company updateCompany(Long companyId, Company company);

    void deleteCompany(Long companyId);

    User assignUser(Long companyId, Long userId);

    User removeUser(Long companyId, Long userId);
}
