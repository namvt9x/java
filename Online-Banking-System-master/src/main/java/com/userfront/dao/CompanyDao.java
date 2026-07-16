package com.userfront.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.userfront.domain.Company;

public interface CompanyDao extends CrudRepository<Company, Long> {

    Company findByCode(String code);

    List<Company> findAll();
}
