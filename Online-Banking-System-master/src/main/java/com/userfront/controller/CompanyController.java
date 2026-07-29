package com.userfront.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.userfront.domain.Company;
import com.userfront.domain.User;
import com.userfront.service.CompanyService;

@RestController
@RequestMapping("/company")
@PreAuthorize("hasRole('ADMIN')")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @GetMapping
    public List<CompanyResponse> findAll() {
        return companyService.findAll().stream()
                .map(CompanyResponse::fromCompany)
                .collect(Collectors.toList());
    }

    @GetMapping("/{companyId}")
    public CompanyResponse findById(@PathVariable("companyId") Long companyId) {
        return CompanyResponse.fromCompany(companyService.findById(companyId));
    }

    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(@RequestBody CompanyRequest request) {
        Company company = companyService.createCompany(request.toCompany());
        return ResponseEntity.status(HttpStatus.CREATED).body(CompanyResponse.fromCompany(company));
    }

    @PutMapping("/{companyId}")
    public CompanyResponse updateCompany(@PathVariable("companyId") Long companyId, @RequestBody CompanyRequest request) {
        Company company = companyService.updateCompany(companyId, request.toCompany());
        return CompanyResponse.fromCompany(company);
    }

    @DeleteMapping("/{companyId}")
    public ResponseEntity<Void> deleteCompany(@PathVariable("companyId") Long companyId) {
        companyService.deleteCompany(companyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{companyId}/accounts")
    public List<UserAccountResponse> findAccountsByCompany(@PathVariable("companyId") Long companyId) {
        Company company = companyService.findById(companyId);

        if (company == null) {
            throw new IllegalArgumentException("Company not found.");
        }

        return company.getUsers().stream()
                .map(UserAccountResponse::fromUser)
                .collect(Collectors.toList());
    }

    @PutMapping("/{companyId}/accounts/{userId}")
    public UserAccountResponse assignAccountToCompany(
            @PathVariable("companyId") Long companyId,
            @PathVariable("userId") Long userId
    ) {
        return UserAccountResponse.fromUser(companyService.assignUser(companyId, userId));
    }

    @DeleteMapping("/{companyId}/accounts/{userId}")
    public UserAccountResponse removeAccountFromCompany(
            @PathVariable("companyId") Long companyId,
            @PathVariable("userId") Long userId
    ) {
        return UserAccountResponse.fromUser(companyService.removeUser(companyId, userId));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    private static class CompanyRequest {
        private String code;
        private String name;
        private String address;
        private String phone;
        private String email;
        private boolean active = true;

        public Company toCompany() {
            Company company = new Company();
            company.setCode(code);
            company.setName(name);
            company.setAddress(address);
            company.setPhone(phone);
            company.setEmail(email);
            company.setActive(active);
            return company;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }

    private static class CompanyResponse {
        private Long companyId;
        private String code;
        private String name;
        private String address;
        private String phone;
        private String email;
        private boolean active;
        private int totalAccounts;

        public static CompanyResponse fromCompany(Company company) {
            if (company == null) {
                throw new IllegalArgumentException("Company not found.");
            }

            CompanyResponse response = new CompanyResponse();
            response.setCompanyId(company.getCompanyId());
            response.setCode(company.getCode());
            response.setName(company.getName());
            response.setAddress(company.getAddress());
            response.setPhone(company.getPhone());
            response.setEmail(company.getEmail());
            response.setActive(company.isActive());
            response.setTotalAccounts(company.getUsers() == null ? 0 : company.getUsers().size());
            return response;
        }

        public Long getCompanyId() {
            return companyId;
        }

        public void setCompanyId(Long companyId) {
            this.companyId = companyId;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public int getTotalAccounts() {
            return totalAccounts;
        }

        public void setTotalAccounts(int totalAccounts) {
            this.totalAccounts = totalAccounts;
        }
    }

    private static class UserAccountResponse {
        private Long userId;
        private String username;
        private String email;
        private String phone;
        private Integer primaryAccountNumber;
        private Integer savingsAccountNumber;
        private Long companyId;

        public static UserAccountResponse fromUser(User user) {
            UserAccountResponse response = new UserAccountResponse();
            response.setUserId(user.getUserId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setPhone(user.getPhone());
            response.setPrimaryAccountNumber(user.getPrimaryAccount() == null ? null : user.getPrimaryAccount().getAccountNumber());
            response.setSavingsAccountNumber(user.getSavingsAccount() == null ? null : user.getSavingsAccount().getAccountNumber());
            response.setCompanyId(user.getCompany() == null ? null : user.getCompany().getCompanyId());
            return response;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public Integer getPrimaryAccountNumber() {
            return primaryAccountNumber;
        }

        public void setPrimaryAccountNumber(Integer primaryAccountNumber) {
            this.primaryAccountNumber = primaryAccountNumber;
        }

        public Integer getSavingsAccountNumber() {
            return savingsAccountNumber;
        }

        public void setSavingsAccountNumber(Integer savingsAccountNumber) {
            this.savingsAccountNumber = savingsAccountNumber;
        }

        public Long getCompanyId() {
            return companyId;
        }

        public void setCompanyId(Long companyId) {
            this.companyId = companyId;
        }
    }
}
