package com.userfront;

public enum AccountType {
    PRIMARY("sql/payment/check-primary-account-balance.sql"),
    SAVINGS("sql/payment/check-savings-account-balance.sql");

    private final String balanceSqlPath;

    AccountType(String balanceSqlPath) {
        this.balanceSqlPath = balanceSqlPath;
    }

    public String getBalanceSqlPath() {
        return balanceSqlPath;
    }

    public static AccountType from(String value) {
        for (AccountType accountType : values()) {
            if (accountType.name().equalsIgnoreCase(value)) {
                return accountType;
            }
        }

        throw new IllegalArgumentException("Unsupported accountType: " + value);
    }
}
