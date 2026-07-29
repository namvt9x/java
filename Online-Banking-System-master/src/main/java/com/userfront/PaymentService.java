package com.userfront;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final JdbcTemplate jdbcTemplate;
    private final SqlResourceLoader sqlResourceLoader;
    private final String procedureName;
    private final String primaryAccountUpdateSql;
    private final String savingsAccountUpdateSql;

    public PaymentService(
            JdbcTemplate jdbcTemplate,
            SqlResourceLoader sqlResourceLoader,
            @Value("${payment.procedure.name}") String procedureName,
            @Value("${payment.primary-account-update-sql}") String primaryAccountUpdateSql,
            @Value("${payment.savings-account-update-sql}") String savingsAccountUpdateSql
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.sqlResourceLoader = sqlResourceLoader;
        this.procedureName = procedureName;
        this.primaryAccountUpdateSql = primaryAccountUpdateSql;
        this.savingsAccountUpdateSql = savingsAccountUpdateSql;
    }

    @Transactional
    public PaymentResponse execute(PaymentRequest request) {
        validate(request);

        AccountType accountType = AccountType.from(request.getAccountType());
        boolean sufficientBalance = hasSufficientBalance(accountType, request.getUsername(), request.getAmount());
        if (!sufficientBalance) {
            return new PaymentResponse(false, false, false, "Insufficient balance.");
        }

        callProcedure(request, accountType);
        int updatedRows = executeAccountUpdateThatCanFireTrigger(accountType, request.getUsername(), request.getAmount());

        return new PaymentResponse(
                true,
                true,
                updatedRows > 0,
                "Payment flow completed. PRC was called and the account update was sent to the DB trigger path."
        );
    }

    private boolean hasSufficientBalance(AccountType accountType, String username, BigDecimal amount) {
        String sql = sqlResourceLoader.load(accountType.getBalanceSqlPath());

        Boolean result = jdbcTemplate.query(
                sql,
                new Object[]{amount, username},
                rs -> rs.next() && rs.getBoolean("has_sufficient_balance")
        );

        return Boolean.TRUE.equals(result);
    }

    private void callProcedure(PaymentRequest request, AccountType accountType) {
        jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement statement = connection.prepareCall(buildProcedureCallSql())) {
                statement.setString(1, request.getUsername());
                statement.setString(2, accountType.name());
                statement.setBigDecimal(3, request.getAmount());
                statement.setString(4, normalize(request.getPayee()));
                statement.setString(5, normalize(request.getReference()));
                statement.execute();
                return null;
            }
        });
    }

    private int executeAccountUpdateThatCanFireTrigger(AccountType accountType, String username, BigDecimal amount) {
        String updateSql = accountType == AccountType.PRIMARY
                ? primaryAccountUpdateSql
                : savingsAccountUpdateSql;

        // The trigger itself lives in MySQL. This UPDATE is the statement path that causes it to fire.
        return jdbcTemplate.update(updateSql, amount, username);
    }

    private String buildProcedureCallSql() {
        return "{CALL " + procedureName + "(?, ?, ?, ?, ?)}";
    }

    private void validate(PaymentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required.");
        }
        if (isBlank(request.getUsername())) {
            throw new IllegalArgumentException("username is required.");
        }
        if (isBlank(request.getAccountType())) {
            throw new IllegalArgumentException("accountType is required.");
        }
        if (isBlank(request.getPayee())) {
            throw new IllegalArgumentException("payee is required.");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
