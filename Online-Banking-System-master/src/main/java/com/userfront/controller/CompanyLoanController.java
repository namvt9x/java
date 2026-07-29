package com.userfront.controller;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.userfront.domain.CompanyLoanCalculationRequest;
import com.userfront.domain.CompanyLoanSummary;
import com.userfront.service.CompanyLoanService;

@RestController
@RequestMapping("/company-loans")
@PreAuthorize("hasRole('ADMIN')")
public class CompanyLoanController {

    @Autowired
    private CompanyLoanService companyLoanService;

    @GetMapping("/{companyId}/summary")
    public CompanyLoanSummary getLoanSummary(
            @PathVariable("companyId") Long companyId,
            @RequestParam("principal") BigDecimal principal,
            @RequestParam("annualInterestRate") BigDecimal annualInterestRate,
            @RequestParam("termMonths") Integer termMonths,
            @RequestParam(value = "paidMonths", defaultValue = "0") Integer paidMonths,
            @RequestParam(value = "disbursedDate", required = false) String disbursedDate
    ) {
        CompanyLoanCalculationRequest request = new CompanyLoanCalculationRequest();
        request.setPrincipal(principal);
        request.setAnnualInterestRate(annualInterestRate);
        request.setTermMonths(termMonths);
        request.setPaidMonths(paidMonths);
        request.setDisbursedDate(disbursedDate);

        return companyLoanService.getLoanSummary(companyId, request);
    }

    @PostMapping("/{companyId}/summary")
    public CompanyLoanSummary getLoanSummaryByBody(
            @PathVariable("companyId") Long companyId,
            @RequestBody CompanyLoanCalculationRequest request
    ) {
        return companyLoanService.getLoanSummary(companyId, request);
    }

    @PostMapping("/{companyId}/repayment-plan")
    public CompanyLoanSummary getRepaymentPlan(
            @PathVariable("companyId") Long companyId,
            @RequestBody CompanyLoanCalculationRequest request
    ) {
        return companyLoanService.getRepaymentPlan(companyId, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
