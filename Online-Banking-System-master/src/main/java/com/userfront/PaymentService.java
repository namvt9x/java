package com.userfront;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private static final String PROCEDURE_NAME = "prc_process_payment";
    private static final String PRIMARY_ACCOUNT_UPDATE_SQL =
            "UPDATE primary_account SET account_balance = account_balance - ? WHERE user_username = ?";

    private final JdbcTemplate jdbcTemplate;

    public PaymentService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public PaymentResponse execute(PaymentRequest request) {
        validate(request);

        String accountType = normalizeAccountType(request.getAccountType());
        callProcedure(request, accountType);
        int updatedRows = executeAccountUpdateThatCanFireTrigger(request.getUsername(), request.getAmount());

        return new PaymentResponse(
                true,
                true,
                updatedRows > 0,
                "Payment flow completed. PRC was called and the account update was sent to the DB trigger path."
        );
    }

    private void callProcedure(PaymentRequest request, String accountType) {
        jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement statement = connection.prepareCall(buildProcedureCallSql())) {
                statement.setString(1, request.getUsername());
                statement.setString(2, accountType);
                statement.setBigDecimal(3, request.getAmount());
                statement.setString(4, normalize(request.getPayee()));
                statement.setString(5, normalize(request.getReference()));
                statement.execute();
                return null;
            }
        });
    }

    private int executeAccountUpdateThatCanFireTrigger(String username, BigDecimal amount) {
        // The trigger itself lives in MySQL. This UPDATE is the statement path that causes it to fire.
        return jdbcTemplate.update(PRIMARY_ACCOUNT_UPDATE_SQL, amount, username);
    }

    private String buildProcedureCallSql() {
        return "{CALL " + PROCEDURE_NAME + "(?, ?, ?, ?, ?)}";
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
        normalizeAccountType(request.getAccountType());
        if (isBlank(request.getPayee())) {
            throw new IllegalArgumentException("payee is required.");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0.");
        }
    }

    private String normalizeAccountType(String value) {
        if (isPrimaryAccount(value)) {
            return AccountType.PRIMARY.name();
        }

        throw new IllegalArgumentException("Unsupported accountType: " + value);
    }

    private boolean isPrimaryAccount(String accountType) {
        return AccountType.PRIMARY.name().equalsIgnoreCase(accountType);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
