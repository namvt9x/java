package com.userfront.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.userfront.domain.User;
import com.userfront.service.UserService;

@RestController
@RequestMapping("/account-classification")
@PreAuthorize("hasRole('ADMIN')")
public class AccountClassificationController {

    @Autowired
    private UserService userService;

    @GetMapping("/regular")
    public List<AccountClassificationResponse> findRegularAccounts() {
        return userService.findRegularUserList().stream()
                .map(AccountClassificationResponse::fromUser)
                .collect(Collectors.toList());
    }

    @GetMapping("/company")
    public List<AccountClassificationResponse> findCompanyAccounts() {
        return userService.findCompanyUserList().stream()
                .map(AccountClassificationResponse::fromUser)
                .collect(Collectors.toList());
    }

    @GetMapping("/{userId}")
    public AccountClassificationResponse findAccountClassification(@PathVariable("userId") Long userId) {
        return userService.findUserList().stream()
                .filter(user -> userId.equals(user.getUserId()))
                .findFirst()
                .map(AccountClassificationResponse::fromUser)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    private static class AccountClassificationResponse {
        private Long userId;
        private String username;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String accountType;
        private Long companyId;
        private String companyCode;
        private String companyName;

        public static AccountClassificationResponse fromUser(User user) {
            AccountClassificationResponse response = new AccountClassificationResponse();
            response.setUserId(user.getUserId());
            response.setUsername(user.getUsername());
            response.setFirstName(user.getFirstName());
            response.setLastName(user.getLastName());
            response.setEmail(user.getEmail());
            response.setPhone(user.getPhone());
            response.setAccountType(user.getCompany() == null ? "REGULAR" : "COMPANY");
            response.setCompanyId(user.getCompany() == null ? null : user.getCompany().getCompanyId());
            response.setCompanyCode(user.getCompany() == null ? null : user.getCompany().getCode());
            response.setCompanyName(user.getCompany() == null ? null : user.getCompany().getName());
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

        public String getAccountType() {
            return accountType;
        }

        public void setAccountType(String accountType) {
            this.accountType = accountType;
        }

        public Long getCompanyId() {
            return companyId;
        }

        public void setCompanyId(Long companyId) {
            this.companyId = companyId;
        }

        public String getCompanyCode() {
            return companyCode;
        }

        public void setCompanyCode(String companyCode) {
            this.companyCode = companyCode;
        }

        public String getCompanyName() {
            return companyName;
        }

        public void setCompanyName(String companyName) {
            this.companyName = companyName;
        }
    }
}
