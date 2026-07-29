package com.userfront.domain;

import java.math.BigDecimal;

public class CompanyLoanCalculationRequest {

    private BigDecimal principal;
    private BigDecimal annualInterestRate;
    private Integer termMonths;
    private Integer paidMonths;
    private String disbursedDate;

    public BigDecimal getPrincipal() {
        return principal;
    }

    public void setPrincipal(BigDecimal principal) {
        this.principal = principal;
    }

    public BigDecimal getAnnualInterestRate() {
        return annualInterestRate;
    }

    public void setAnnualInterestRate(BigDecimal annualInterestRate) {
        this.annualInterestRate = annualInterestRate;
    }

    public Integer getTermMonths() {
        return termMonths;
    }

    public void setTermMonths(Integer termMonths) {
        this.termMonths = termMonths;
    }

    public Integer getPaidMonths() {
        return paidMonths;
    }

    public void setPaidMonths(Integer paidMonths) {
        this.paidMonths = paidMonths;
    }

    public String getDisbursedDate() {
        return disbursedDate;
    }

    public void setDisbursedDate(String disbursedDate) {
        this.disbursedDate = disbursedDate;
    }
}
