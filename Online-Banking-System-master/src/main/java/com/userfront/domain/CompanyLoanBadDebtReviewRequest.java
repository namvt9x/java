package com.userfront.domain;

public class CompanyLoanBadDebtReviewRequest {

    private Long companyId;
    private CompanyLoanCalculationRequest loan;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public CompanyLoanCalculationRequest getLoan() {
        return loan;
    }

    public void setLoan(CompanyLoanCalculationRequest loan) {
        this.loan = loan;
    }
}
