package com.userfront.service.UserServiceImpl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.userfront.domain.Company;
import com.userfront.domain.CompanyLoanCalculationRequest;
import com.userfront.domain.CompanyLoanRepaymentItem;
import com.userfront.domain.CompanyLoanSummary;
import com.userfront.service.CompanyLoanService;
import com.userfront.service.CompanyService;

@Service
public class CompanyLoanServiceImpl implements CompanyLoanService {

    private static final int SCALE = 2;
    private static final MathContext MATH_CONTEXT = new MathContext(16, RoundingMode.HALF_UP);
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal TWELVE = new BigDecimal("12");

    @Autowired
    private CompanyService companyService;

    public CompanyLoanSummary getLoanSummary(Long companyId, CompanyLoanCalculationRequest request) {
        return buildLoanSummary(companyId, request, false);
    }

    public CompanyLoanSummary getRepaymentPlan(Long companyId, CompanyLoanCalculationRequest request) {
        return buildLoanSummary(companyId, request, true);
    }

    private CompanyLoanSummary buildLoanSummary(Long companyId, CompanyLoanCalculationRequest request, boolean includeSchedule) {
        Company company = findCompanyOrThrow(companyId);
        validateRequest(request);

        int paidMonths = request.getPaidMonths() == null ? 0 : request.getPaidMonths();
        LocalDate disbursedDate = parseDate(request.getDisbursedDate());
        LoanMetrics metrics = calculateLoanMetrics(request.getPrincipal(), request.getAnnualInterestRate(), request.getTermMonths(), paidMonths);

        CompanyLoanSummary summary = new CompanyLoanSummary();
        summary.setCompanyId(company.getCompanyId());
        summary.setCompanyCode(company.getCode());
        summary.setCompanyName(company.getName());
        summary.setPrincipal(scale(request.getPrincipal()));
        summary.setAnnualInterestRate(scale(request.getAnnualInterestRate()));
        summary.setTermMonths(request.getTermMonths());
        summary.setPaidMonths(paidMonths);
        summary.setRemainingMonths(Math.max(request.getTermMonths() - paidMonths, 0));
        summary.setDisbursedDate(disbursedDate);
        summary.setNextDueDate(disbursedDate == null ? null : disbursedDate.plusMonths((long) paidMonths + 1L));
        summary.setMonthlyPayment(metrics.getMonthlyPayment());
        summary.setTotalInterest(metrics.getTotalInterest());
        summary.setTotalPayment(metrics.getTotalPayment());
        summary.setPaidPrincipal(metrics.getPaidPrincipal());
        summary.setPaidInterest(metrics.getPaidInterest());
        summary.setRemainingPrincipal(metrics.getRemainingPrincipal());
        summary.setRemainingInterest(metrics.getRemainingInterest());
        summary.setStatus(determineStatus(paidMonths, request.getTermMonths(), metrics.getRemainingPrincipal()));

        if (includeSchedule) {
            summary.setRepaymentSchedule(buildRepaymentSchedule(request, disbursedDate));
        }

        return summary;
    }

    private List<CompanyLoanRepaymentItem> buildRepaymentSchedule(CompanyLoanCalculationRequest request, LocalDate disbursedDate) {
        List<CompanyLoanRepaymentItem> schedule = new ArrayList<>();
        BigDecimal principal = request.getPrincipal();
        BigDecimal annualInterestRate = request.getAnnualInterestRate();
        int termMonths = request.getTermMonths();
        BigDecimal monthlyRate = monthlyRate(annualInterestRate);
        BigDecimal monthlyPayment = calculateMonthlyPayment(principal, annualInterestRate, termMonths);
        BigDecimal remainingPrincipal = principal;

        for (int month = 1; month <= termMonths; month++) {
            BigDecimal interestPayment = scale(remainingPrincipal.multiply(monthlyRate, MATH_CONTEXT));
            BigDecimal principalPayment = scale(monthlyPayment.subtract(interestPayment));

            if (month == termMonths || principalPayment.compareTo(remainingPrincipal) > 0) {
                principalPayment = scale(remainingPrincipal);
                monthlyPayment = scale(principalPayment.add(interestPayment));
            }

            remainingPrincipal = scale(remainingPrincipal.subtract(principalPayment));

            CompanyLoanRepaymentItem item = new CompanyLoanRepaymentItem();
            item.setInstallmentNumber(month);
            item.setDueDate(disbursedDate == null ? null : disbursedDate.plusMonths(month));
            item.setPaymentAmount(monthlyPayment);
            item.setPrincipalPayment(principalPayment);
            item.setInterestPayment(interestPayment);
            item.setRemainingPrincipal(remainingPrincipal.max(BigDecimal.ZERO));
            schedule.add(item);
        }

        return schedule;
    }

    private LoanMetrics calculateLoanMetrics(BigDecimal principal, BigDecimal annualInterestRate, int termMonths, int paidMonths) {
        BigDecimal monthlyRate = monthlyRate(annualInterestRate);
        BigDecimal baseMonthlyPayment = calculateMonthlyPayment(principal, annualInterestRate, termMonths);
        BigDecimal remainingPrincipal = principal;
        BigDecimal paidPrincipal = BigDecimal.ZERO;
        BigDecimal paidInterest = BigDecimal.ZERO;
        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal lastMonthlyPayment = baseMonthlyPayment;

        for (int month = 1; month <= termMonths; month++) {
            BigDecimal interestPayment = scale(remainingPrincipal.multiply(monthlyRate, MATH_CONTEXT));
            BigDecimal principalPayment = scale(baseMonthlyPayment.subtract(interestPayment));
            BigDecimal paymentAmount = baseMonthlyPayment;

            if (month == termMonths || principalPayment.compareTo(remainingPrincipal) > 0) {
                principalPayment = scale(remainingPrincipal);
                paymentAmount = scale(principalPayment.add(interestPayment));
            }

            remainingPrincipal = scale(remainingPrincipal.subtract(principalPayment));
            totalInterest = scale(totalInterest.add(interestPayment));
            lastMonthlyPayment = paymentAmount;

            if (month <= paidMonths) {
                paidPrincipal = scale(paidPrincipal.add(principalPayment));
                paidInterest = scale(paidInterest.add(interestPayment));
            }
        }

        LoanMetrics metrics = new LoanMetrics();
        metrics.setMonthlyPayment(lastMonthlyPayment);
        metrics.setTotalInterest(totalInterest);
        metrics.setTotalPayment(scale(principal.add(totalInterest)));
        metrics.setPaidPrincipal(paidPrincipal);
        metrics.setPaidInterest(paidInterest);
        metrics.setRemainingPrincipal(scale(principal.subtract(paidPrincipal)).max(BigDecimal.ZERO));
        metrics.setRemainingInterest(scale(totalInterest.subtract(paidInterest)).max(BigDecimal.ZERO));
        return metrics;
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal annualInterestRate, int termMonths) {
        BigDecimal monthlyRate = monthlyRate(annualInterestRate);

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return scale(principal.divide(new BigDecimal(termMonths), SCALE, RoundingMode.HALF_UP));
        }

        BigDecimal factor = BigDecimal.ONE.add(monthlyRate, MATH_CONTEXT).pow(termMonths, MATH_CONTEXT);
        BigDecimal numerator = principal.multiply(monthlyRate, MATH_CONTEXT).multiply(factor, MATH_CONTEXT);
        BigDecimal denominator = factor.subtract(BigDecimal.ONE, MATH_CONTEXT);

        return scale(numerator.divide(denominator, SCALE, RoundingMode.HALF_UP));
    }

    private BigDecimal monthlyRate(BigDecimal annualInterestRate) {
        return annualInterestRate.divide(ONE_HUNDRED, 10, RoundingMode.HALF_UP)
                .divide(TWELVE, 10, RoundingMode.HALF_UP);
    }

    private String determineStatus(int paidMonths, int termMonths, BigDecimal remainingPrincipal) {
        if (paidMonths >= termMonths || remainingPrincipal.compareTo(BigDecimal.ZERO) == 0) {
            return "CLOSED";
        }
        if (paidMonths == 0) {
            return "NEW";
        }
        return "ACTIVE";
    }

    private void validateRequest(CompanyLoanCalculationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Loan request is required.");
        }
        if (request.getPrincipal() == null || request.getPrincipal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Principal must be greater than 0.");
        }
        if (request.getAnnualInterestRate() == null || request.getAnnualInterestRate().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Annual interest rate must be greater than or equal to 0.");
        }
        if (request.getTermMonths() == null || request.getTermMonths() <= 0) {
            throw new IllegalArgumentException("Term months must be greater than 0.");
        }

        int paidMonths = request.getPaidMonths() == null ? 0 : request.getPaidMonths();
        if (paidMonths < 0) {
            throw new IllegalArgumentException("Paid months cannot be negative.");
        }
        if (paidMonths > request.getTermMonths()) {
            throw new IllegalArgumentException("Paid months cannot exceed term months.");
        }

        parseDate(request.getDisbursedDate());
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Disbursed date must use ISO format yyyy-MM-dd.");
        }
    }

    private Company findCompanyOrThrow(Long companyId) {
        Company company = companyService.findById(companyId);

        if (company == null) {
            throw new IllegalArgumentException("Company not found.");
        }

        return company;
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    private static class LoanMetrics {
        private BigDecimal monthlyPayment;
        private BigDecimal totalInterest;
        private BigDecimal totalPayment;
        private BigDecimal paidPrincipal;
        private BigDecimal paidInterest;
        private BigDecimal remainingPrincipal;
        private BigDecimal remainingInterest;

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
    }
}
