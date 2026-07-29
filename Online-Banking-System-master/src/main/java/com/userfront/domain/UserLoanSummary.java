package com.userfront.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class UserLoanSummary {

    private Long userId;
    private String username;
    private String fullName;
    private boolean companyUser;
    private Long companyId;
    private String companyName;
    private BigDecimal principal;
    private BigDecimal annualInterestRate;
    private Integer termMonths;
    private Integer paidMonths;
    private Integer paidInterestMonths;
    private Integer remainingMonths;
    private Integer overdueMonths;
    private Integer unpaidInterestMonths;
    private LocalDate disbursedDate;
    private LocalDate nextDueDate;
    private BigDecimal monthlyPayment;
    private BigDecimal totalInterest;
    private BigDecimal totalPayment;
    private BigDecimal paidPrincipal;
    private BigDecimal paidInterest;
    private BigDecimal remainingPrincipal;
    private BigDecimal remainingInterest;
    private BigDecimal penaltyAmount;
    private boolean loanExpired;
    private boolean lateInterestPayment;
    private String status;
    private List<UserLoanRepaymentItem> repaymentSchedule;

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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isCompanyUser() {
        return companyUser;
    }

    public void setCompanyUser(boolean companyUser) {
        this.companyUser = companyUser;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

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

    public Integer getPaidInterestMonths() {
        return paidInterestMonths;
    }

    public void setPaidInterestMonths(Integer paidInterestMonths) {
        this.paidInterestMonths = paidInterestMonths;
    }

    public Integer getRemainingMonths() {
        return remainingMonths;
    }

    public void setRemainingMonths(Integer remainingMonths) {
        this.remainingMonths = remainingMonths;
    }

    public Integer getOverdueMonths() {
        return overdueMonths;
    }

    public void setOverdueMonths(Integer overdueMonths) {
        this.overdueMonths = overdueMonths;
    }

    public Integer getUnpaidInterestMonths() {
        return unpaidInterestMonths;
    }

    public void setUnpaidInterestMonths(Integer unpaidInterestMonths) {
        this.unpaidInterestMonths = unpaidInterestMonths;
    }

    public LocalDate getDisbursedDate() {
        return disbursedDate;
    }

    public void setDisbursedDate(LocalDate disbursedDate) {
        this.disbursedDate = disbursedDate;
    }

    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public BigDecimal getMonthlyPayment() {
        return monthlyPayment;
    }

    public void setMonthlyPayment(BigDecimal monthlyPayment) {
        this.monthlyPayment = monthlyPayment;
    }

    public BigDecimal getTotalInterest() {
        return totalInterest;
    }

    public void setTotalInterest(BigDecimal totalInterest) {
        this.totalInterest = totalInterest;
    }

    public BigDecimal getTotalPayment() {
        return totalPayment;
    }

    public void setTotalPayment(BigDecimal totalPayment) {
        this.totalPayment = totalPayment;
    }

    public BigDecimal getPaidPrincipal() {
        return paidPrincipal;
    }

    public void setPaidPrincipal(BigDecimal paidPrincipal) {
        this.paidPrincipal = paidPrincipal;
    }

    public BigDecimal getPaidInterest() {
        return paidInterest;
    }

    public void setPaidInterest(BigDecimal paidInterest) {
        this.paidInterest = paidInterest;
    }

    public BigDecimal getRemainingPrincipal() {
        return remainingPrincipal;
    }

    public void setRemainingPrincipal(BigDecimal remainingPrincipal) {
        this.remainingPrincipal = remainingPrincipal;
    }

    public BigDecimal getRemainingInterest() {
        return remainingInterest;
    }

    public void setRemainingInterest(BigDecimal remainingInterest) {
        this.remainingInterest = remainingInterest;
    }

    public BigDecimal getPenaltyAmount() {
        return penaltyAmount;
    }

    public void setPenaltyAmount(BigDecimal penaltyAmount) {
        this.penaltyAmount = penaltyAmount;
    }

    public boolean isLoanExpired() {
        return loanExpired;
    }

    public void setLoanExpired(boolean loanExpired) {
        this.loanExpired = loanExpired;
    }

    public boolean isLateInterestPayment() {
        return lateInterestPayment;
    }

    public void setLateInterestPayment(boolean lateInterestPayment) {
        this.lateInterestPayment = lateInterestPayment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<UserLoanRepaymentItem> getRepaymentSchedule() {
        return repaymentSchedule;
    }

    public void setRepaymentSchedule(List<UserLoanRepaymentItem> repaymentSchedule) {
        this.repaymentSchedule = repaymentSchedule;
    }
}
