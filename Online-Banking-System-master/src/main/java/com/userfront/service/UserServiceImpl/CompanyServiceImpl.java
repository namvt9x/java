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

    public List<Company> findAll() {
        return companyDao.findAll();
    }

    public Company findById(Long companyId) {
        return companyDao.findById(companyId).orElse(null);
    }

    public Company createCompany(Company company) {
        if (companyDao.findByCode(company.getCode()) != null) {
            throw new IllegalArgumentException("Company code already exists.");
        }

        return companyDao.save(company);
    }

    public Company updateCompany(Long companyId, Company company) {
        Company existingCompany = findCompanyOrThrow(companyId);
        Company companyWithSameCode = companyDao.findByCode(company.getCode());

        if (companyWithSameCode != null && !companyWithSameCode.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("Company code already exists.");
        }

        existingCompany.setCode(company.getCode());
        existingCompany.setName(company.getName());
        existingCompany.setAddress(company.getAddress());
        existingCompany.setPhone(company.getPhone());
        existingCompany.setEmail(company.getEmail());
        existingCompany.setActive(company.isActive());

        return companyDao.save(existingCompany);
    }

    public void deleteCompany(Long companyId) {
        Company company = findCompanyOrThrow(companyId);

        for (User user : company.getUsers()) {
            user.setCompany(null);
        }

        companyDao.delete(company);
    }

    public User assignUser(Long companyId, Long userId) {
        Company company = findCompanyOrThrow(companyId);
        User user = findUserOrThrow(userId);

        user.setCompany(company);
        return userDao.save(user);
    }

    public User removeUser(Long companyId, Long userId) {
        Company company = findCompanyOrThrow(companyId);
        User user = findUserOrThrow(userId);

        if (user.getCompany() == null || !companyId.equals(user.getCompany().getCompanyId())) {
            throw new IllegalArgumentException("User is not linked to this company.");
        }

        user.setCompany(null);
        return userDao.save(user);
    }

    private Company findCompanyOrThrow(Long companyId) {
        Company company = findById(companyId);

        if (company == null) {
            throw new IllegalArgumentException("Company not found.");
        }

        return company;
    }

    private User findUserOrThrow(Long userId) {
        User user = userDao.findById(userId).orElse(null);

        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        return user;
    }
}
