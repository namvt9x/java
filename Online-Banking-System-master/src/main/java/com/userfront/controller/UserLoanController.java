package com.userfront.controller;

import java.math.BigDecimal;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.userfront.domain.UserLoanCalculationRequest;
import com.userfront.domain.UserLoanSummary;
import com.userfront.service.UserLoanService;

@RestController
@RequestMapping("/user-loans")
@PreAuthorize("hasRole('USER')")
public class UserLoanController {

    @Autowired
    private UserLoanService userLoanService;

    @GetMapping("/me/summary")
    public UserLoanSummary getLoanSummary(
            Principal principal,
            @RequestParam("principal") BigDecimal loanPrincipal,
            @RequestParam(value = "annualInterestRate", required = false) BigDecimal annualInterestRate,
            @RequestParam("termMonths") Integer termMonths,
            @RequestParam(value = "paidMonths", defaultValue = "0") Integer paidMonths,
            @RequestParam(value = "paidInterestMonths", required = false) Integer paidInterestMonths,
            @RequestParam(value = "disbursedDate", required = false) String disbursedDate
    ) {
        UserLoanCalculationRequest request = new UserLoanCalculationRequest();
        request.setPrincipal(loanPrincipal);
        request.setAnnualInterestRate(annualInterestRate);
        request.setTermMonths(termMonths);
        request.setPaidMonths(paidMonths);
        request.setPaidInterestMonths(paidInterestMonths);
        request.setDisbursedDate(disbursedDate);

        return userLoanService.getLoanSummary(principal.getName(), request);
    }

    @PostMapping("/me/summary")
    public UserLoanSummary getLoanSummaryByBody(
            Principal principal,
            @RequestBody UserLoanCalculationRequest request
    ) {
        return userLoanService.getLoanSummary(principal.getName(), request);
    }

    @PostMapping("/me/repayment-plan")
    public UserLoanSummary getRepaymentPlan(
            Principal principal,
            @RequestBody UserLoanCalculationRequest request
    ) {
        return userLoanService.getRepaymentPlan(principal.getName(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
