package com.userfront.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.userfront.domain.CompanyLoanBadDebtReviewRequest;
import com.userfront.domain.CompanyLoanCalculationRequest;
import com.userfront.domain.CompanyLoanSummary;
import com.userfront.service.CompanyLoanService;

@RestController
@RequestMapping("/company-loan-bad-debts")
@PreAuthorize("hasRole('ADMIN')")
public class CompanyLoanBadDebtController {

    @Autowired
    private CompanyLoanService companyLoanService;

    @PostMapping("/{companyId}/review")
    public CompanyLoanSummary reviewBadDebt(
            @PathVariable("companyId") Long companyId,
            @RequestBody CompanyLoanCalculationRequest request
    ) {
        return companyLoanService.reviewBadDebt(companyId, request);
    }

    @PostMapping("/review-list")
    public List<CompanyLoanSummary> reviewBadDebtList(
            @RequestBody List<CompanyLoanBadDebtReviewRequest> requests
    ) {
        return companyLoanService.reviewBadDebtList(requests);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
