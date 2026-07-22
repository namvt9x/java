SELECT
    CASE
        WHEN pa.account_balance >= ? THEN TRUE
        ELSE FALSE
    END AS has_sufficient_balance
FROM user u
INNER JOIN primary_account pa
    ON u.primary_account_id = pa.id
WHERE u.username = ?
