package com.userfront.service.UserServiceImpl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.userfront.domain.User;
import com.userfront.domain.UserLoanCalculationRequest;
import com.userfront.domain.UserLoanRepaymentItem;
import com.userfront.domain.UserLoanSummary;
import com.userfront.service.UserLoanService;
import com.userfront.service.UserService;

@Service
public class UserLoanServiceImpl implements UserLoanService {

    private static final int SCALE = 2;
    private static final MathContext MATH_CONTEXT = new MathContext(16, RoundingMode.HALF_UP);
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal TWELVE = new BigDecimal("12");
    private static final BigDecimal COMPANY_RATE_DISCOUNT = new BigDecimal("0.50");
    private static final BigDecimal UNPAID_INTEREST_PENALTY_RATE = new BigDecimal("0.10");
    private static final BigDecimal EXPIRED_LOAN_PENALTY_RATE = new BigDecimal("0.03");

    @Autowired
    private UserService userService;

    public UserLoanSummary getLoanSummary(String username, UserLoanCalculationRequest request) {
        return buildLoanSummary(username, request, false);
    }

    public UserLoanSummary getRepaymentPlan(String username, UserLoanCalculationRequest request) {
        return buildLoanSummary(username, request, true);
    }

    private UserLoanSummary buildLoanSummary(String username, UserLoanCalculationRequest request, boolean includeSchedule) {
        User user = findUserOrThrow(username);
        validateRequest(request);

        int paidMonths = request.getPaidMonths() == null ? 0 : request.getPaidMonths();
        int paidInterestMonths = request.getPaidInterestMonths() == null ? paidMonths : request.getPaidInterestMonths();
        LocalDate disbursedDate = parseDate(request.getDisbursedDate());
        BigDecimal annualInterestRate = resolveAnnualInterestRate(user, request);
        LoanMetrics metrics = calculateLoanMetrics(request.getPrincipal(), annualInterestRate, request.getTermMonths(), paidMonths, paidInterestMonths);
        LoanPenalty penalty = calculatePenalty(request, annualInterestRate, paidMonths, paidInterestMonths, disbursedDate, metrics.getRemainingPrincipal());

        UserLoanSummary summary = new UserLoanSummary();
        summary.setUserId(user.getUserId());
        summary.setUsername(user.getUsername());
        summary.setFullName(buildFullName(user));
        summary.setCompanyUser(user.getCompany() != null);
        summary.setCompanyId(user.getCompany() == null ? null : user.getCompany().getCompanyId());
        summary.setCompanyName(user.getCompany() == null ? null : user.getCompany().getName());
        summary.setPrincipal(scale(request.getPrincipal()));
        summary.setAnnualInterestRate(scale(annualInterestRate));
        summary.setTermMonths(request.getTermMonths());
        summary.setPaidMonths(paidMonths);
        summary.setPaidInterestMonths(paidInterestMonths);
        summary.setRemainingMonths(Math.max(request.getTermMonths() - paidMonths, 0));
        summary.setOverdueMonths(penalty.getOverdueMonths());
        summary.setUnpaidInterestMonths(penalty.getUnpaidInterestMonths());
        summary.setDisbursedDate(disbursedDate);
        summary.setNextDueDate(disbursedDate == null ? null : disbursedDate.plusMonths((long) paidMonths + 1L));
        summary.setMonthlyPayment(metrics.getMonthlyPayment());
        summary.setTotalInterest(metrics.getTotalInterest());
        summary.setTotalPayment(metrics.getTotalPayment());
        summary.setPaidPrincipal(metrics.getPaidPrincipal());
        summary.setPaidInterest(metrics.getPaidInterest());
        summary.setRemainingPrincipal(metrics.getRemainingPrincipal());
        summary.setRemainingInterest(metrics.getRemainingInterest());
        summary.setPenaltyAmount(penalty.getPenaltyAmount());
        summary.setLoanExpired(penalty.isLoanExpired());
        summary.setLateInterestPayment(penalty.isLateInterestPayment());
        summary.setStatus(determineStatus(paidMonths, request.getTermMonths(), metrics.getRemainingPrincipal(), penalty));

        if (includeSchedule) {
            summary.setRepaymentSchedule(buildRepaymentSchedule(request, disbursedDate, annualInterestRate));
        }

        return summary;
    }

    private List<UserLoanRepaymentItem> buildRepaymentSchedule(UserLoanCalculationRequest request, LocalDate disbursedDate, BigDecimal annualInterestRate) {
        List<UserLoanRepaymentItem> schedule = new ArrayList<>();
        BigDecimal principal = request.getPrincipal();
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

            UserLoanRepaymentItem item = new UserLoanRepaymentItem();
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

    private LoanMetrics calculateLoanMetrics(BigDecimal principal, BigDecimal annualInterestRate, int termMonths, int paidMonths, int paidInterestMonths) {
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
            }
            if (month <= paidInterestMonths) {
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

    private String determineStatus(int paidMonths, int termMonths, BigDecimal remainingPrincipal, LoanPenalty penalty) {
        if (penalty.getPenaltyAmount().compareTo(BigDecimal.ZERO) > 0) {
            return penalty.isLoanExpired() ? "OVERDUE" : "LATE_INTEREST";
        }
        if (paidMonths >= termMonths || remainingPrincipal.compareTo(BigDecimal.ZERO) == 0) {
            return "CLOSED";
        }
        if (paidMonths == 0) {
            return "NEW";
        }
        return "ACTIVE";
    }

    private void validateRequest(UserLoanCalculationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Loan request is required.");
        }
        if (request.getPrincipal() == null || request.getPrincipal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Principal must be greater than 0.");
        }
        if (request.getAnnualInterestRate() != null && request.getAnnualInterestRate().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Annual interest rate must be greater than or equal to 0.");
        }
        if (request.getTermMonths() == null || request.getTermMonths() <= 0) {
            throw new IllegalArgumentException("Term months must be greater than 0.");
        }

        int paidMonths = request.getPaidMonths() == null ? 0 : request.getPaidMonths();
        int paidInterestMonths = request.getPaidInterestMonths() == null ? paidMonths : request.getPaidInterestMonths();
        if (paidMonths < 0) {
            throw new IllegalArgumentException("Paid months cannot be negative.");
        }
        if (paidMonths > request.getTermMonths()) {
            throw new IllegalArgumentException("Paid months cannot exceed term months.");
        }
        if (paidInterestMonths < 0) {
            throw new IllegalArgumentException("Paid interest months cannot be negative.");
        }
        if (paidInterestMonths > request.getTermMonths()) {
            throw new IllegalArgumentException("Paid interest months cannot exceed term months.");
        }
        if (paidInterestMonths < paidMonths) {
            throw new IllegalArgumentException("Paid interest months cannot be less than paid months.");
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

    private User findUserOrThrow(String username) {
        User user = userService.findByUsername(username);

        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        return user;
    }

    private BigDecimal resolveAnnualInterestRate(User user, UserLoanCalculationRequest request) {
        if (request.getAnnualInterestRate() != null) {
            return request.getAnnualInterestRate();
        }

        BigDecimal baseRate;
        int termMonths = request.getTermMonths();

        if (termMonths <= 6) {
            baseRate = new BigDecimal("5.50");
        } else if (termMonths <= 12) {
            baseRate = new BigDecimal("7.00");
        } else if (termMonths <= 24) {
            baseRate = new BigDecimal("8.50");
        } else {
            baseRate = new BigDecimal("10.00");
        }

        if (user.getCompany() != null) {
            baseRate = baseRate.subtract(COMPANY_RATE_DISCOUNT);
        }

        return scale(baseRate);
    }

    private LoanPenalty calculatePenalty(
            UserLoanCalculationRequest request,
            BigDecimal annualInterestRate,
            int paidMonths,
            int paidInterestMonths,
            LocalDate disbursedDate,
            BigDecimal remainingPrincipal
    ) {
        LoanPenalty penalty = new LoanPenalty();
        penalty.setPenaltyAmount(BigDecimal.ZERO);
        penalty.setOverdueMonths(0);
        penalty.setUnpaidInterestMonths(0);
        penalty.setLoanExpired(false);
        penalty.setLateInterestPayment(false);

        if (disbursedDate == null) {
            return penalty;
        }

        LocalDate today = LocalDate.now();
        if (today.isBefore(disbursedDate)) {
            return penalty;
        }

        int elapsedMonths = (int) ChronoUnit.MONTHS.between(disbursedDate, today);
        int dueMonths = Math.min(Math.max(elapsedMonths, 0), request.getTermMonths());
        int unpaidInterestMonths = Math.max(dueMonths - paidInterestMonths, 0);
        int overdueMonths = Math.max(elapsedMonths - request.getTermMonths(), 0);

        BigDecimal monthlyInterestPayment = scale(request.getPrincipal().multiply(monthlyRate(annualInterestRate), MATH_CONTEXT));
        BigDecimal unpaidInterestPenalty = monthlyInterestPayment
                .multiply(new BigDecimal(unpaidInterestMonths), MATH_CONTEXT)
                .multiply(UNPAID_INTEREST_PENALTY_RATE, MATH_CONTEXT);

        BigDecimal expiredPenalty = BigDecimal.ZERO;
        boolean loanExpired = overdueMonths > 0 && remainingPrincipal.compareTo(BigDecimal.ZERO) > 0;
        if (loanExpired) {
            expiredPenalty = remainingPrincipal
                    .multiply(EXPIRED_LOAN_PENALTY_RATE, MATH_CONTEXT)
                    .multiply(new BigDecimal(overdueMonths), MATH_CONTEXT);
        }

        penalty.setOverdueMonths(overdueMonths);
        penalty.setUnpaidInterestMonths(unpaidInterestMonths);
        penalty.setLateInterestPayment(unpaidInterestMonths > 0);
        penalty.setLoanExpired(loanExpired);
        penalty.setPenaltyAmount(scale(unpaidInterestPenalty.add(expiredPenalty)));
        return penalty;
    }

    private String buildFullName(User user) {
        String firstName = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String lastName = user.getLastName() == null ? "" : user.getLastName().trim();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isEmpty() ? user.getUsername() : fullName;
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

    private static class LoanPenalty {
        private BigDecimal penaltyAmount;
        private int overdueMonths;
        private int unpaidInterestMonths;
        private boolean loanExpired;
        private boolean lateInterestPayment;

        public BigDecimal getPenaltyAmount() {
            return penaltyAmount;
        }

        public void setPenaltyAmount(BigDecimal penaltyAmount) {
            this.penaltyAmount = penaltyAmount;
        }

        public int getOverdueMonths() {
            return overdueMonths;
        }

        public void setOverdueMonths(int overdueMonths) {
            this.overdueMonths = overdueMonths;
        }

        public int getUnpaidInterestMonths() {
            return unpaidInterestMonths;
        }

        public void setUnpaidInterestMonths(int unpaidInterestMonths) {
            this.unpaidInterestMonths = unpaidInterestMonths;
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
    }
}
