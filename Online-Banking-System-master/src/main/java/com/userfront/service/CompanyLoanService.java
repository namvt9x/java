package com.userfront.service;

import java.util.List;

import com.userfront.domain.CompanyLoanBadDebtReviewRequest;
import com.userfront.domain.CompanyLoanCalculationRequest;
import com.userfront.domain.CompanyLoanSummary;

public interface CompanyLoanService {

    CompanyLoanSummary getLoanSummary(Long companyId, CompanyLoanCalculationRequest request);

    CompanyLoanSummary getRepaymentPlan(Long companyId, CompanyLoanCalculationRequest request);

    CompanyLoanSummary reviewBadDebt(Long companyId, CompanyLoanCalculationRequest request);

    List<CompanyLoanSummary> reviewBadDebtList(List<CompanyLoanBadDebtReviewRequest> requests);
}
