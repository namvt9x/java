package com.userfront.service.UserServiceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.userfront.dao.CompanyDao;
import com.userfront.dao.UserDao;
import com.userfront.domain.Company;
import com.userfront.domain.User;
import com.userfront.service.CompanyService;

@Service
@Transactional
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    private CompanyDao companyDao;

    @Autowired
    private UserDao userDao;

    @Override
    public Company findByUsername(String username) {
        User user = userDao.findByUsername(username);
        if (user == null) {
            return null;
        }

        return companyDao.findByUser(user);
    }

    @Override
    public Company saveForUser(String username, Company company) {
        User user = userDao.findByUsername(username);
        if (user == null) {
            return null;
        }

        Company existingCompany = companyDao.findByUser(user);

        if (existingCompany != null) {
            existingCompany.setName(company.getName());
            existingCompany.setTaxCode(company.getTaxCode());
            existingCompany.setAddress(company.getAddress());
            existingCompany.setPhone(company.getPhone());
            existingCompany.setEmail(company.getEmail());
            existingCompany.setDescription(company.getDescription());
            return companyDao.save(existingCompany);
        }

        company.setUser(user);
        return companyDao.save(company);
    }

    @Override
    public List<Company> findAll() {
        return companyDao.findAll();
    }
}
