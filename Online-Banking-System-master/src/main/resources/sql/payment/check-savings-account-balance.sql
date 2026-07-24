SELECT
    CASE
        WHEN sa.account_balance >= ? THEN TRUE
        ELSE FALSE
    END AS has_sufficient_balance
FROM user u
INNER JOIN savings_account sa
    ON u.savings_account_id = sa.id
WHERE u.username = ?
