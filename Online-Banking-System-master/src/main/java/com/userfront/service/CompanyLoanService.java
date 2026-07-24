package com.userfront.service;

import com.userfront.domain.CompanyLoanCalculationRequest;
import com.userfront.domain.CompanyLoanSummary;

public interface CompanyLoanService {

    CompanyLoanSummary getLoanSummary(Long companyId, CompanyLoanCalculationRequest request);

    CompanyLoanSummary getRepaymentPlan(Long companyId, CompanyLoanCalculationRequest request);
}
