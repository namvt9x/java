package com.userfront.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.userfront.domain.Company;
import com.userfront.domain.User;

public interface CompanyDao extends CrudRepository<Company, Long> {

    Company findByUser(User user);

    Company findByTaxCode(String taxCode);

    List<Company> findAll();
}
