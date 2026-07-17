package com.userfront.service;

import com.userfront.domain.UserLoanCalculationRequest;
import com.userfront.domain.UserLoanSummary;

public interface UserLoanService {

    UserLoanSummary getLoanSummary(String username, UserLoanCalculationRequest request);

    UserLoanSummary getRepaymentPlan(String username, UserLoanCalculationRequest request);
}
