package com.userfront.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.userfront.domain.Company;
import com.userfront.domain.User;
import com.userfront.service.CompanyService;

@RestController
@RequestMapping("/company/{companyId}/employees")
@PreAuthorize("hasRole('ADMIN')")
public class CompanyEmployeeController {

    @Autowired
    private CompanyService companyService;

    @GetMapping
    public List<EmployeeResponse> findEmployeesByCompany(@PathVariable("companyId") Long companyId) {
        Company company = findCompanyOrThrow(companyId);

        return company.getUsers().stream()
                .map(EmployeeResponse::fromUser)
                .collect(Collectors.toList());
    }

    @GetMapping("/{userId}")
    public EmployeeResponse findEmployeeByCompany(
            @PathVariable("companyId") Long companyId,
            @PathVariable("userId") Long userId
    ) {
        Company company = findCompanyOrThrow(companyId);

        return company.getUsers().stream()
                .filter(user -> userId.equals(user.getUserId()))
                .findFirst()
                .map(EmployeeResponse::fromUser)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found in this company."));
    }

    @PutMapping("/{userId}")
    public EmployeeResponse assignEmployeeToCompany(
            @PathVariable("companyId") Long companyId,
            @PathVariable("userId") Long userId
    ) {
        return EmployeeResponse.fromUser(companyService.assignUser(companyId, userId));
    }

    @DeleteMapping("/{userId}")
    public EmployeeResponse removeEmployeeFromCompany(
            @PathVariable("companyId") Long companyId,
            @PathVariable("userId") Long userId
    ) {
        return EmployeeResponse.fromUser(companyService.removeUser(companyId, userId));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    private Company findCompanyOrThrow(Long companyId) {
        Company company = companyService.findById(companyId);

        if (company == null) {
            throw new IllegalArgumentException("Company not found.");
        }

        return company;
    }

    private static class EmployeeResponse {
        private Long userId;
        private String username;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private Integer primaryAccountNumber;
        private Integer savingsAccountNumber;
        private Long companyId;

        public static EmployeeResponse fromUser(User user) {
            EmployeeResponse response = new EmployeeResponse();
            response.setUserId(user.getUserId());
            response.setUsername(user.getUsername());
            response.setFirstName(user.getFirstName());
            response.setLastName(user.getLastName());
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

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
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
