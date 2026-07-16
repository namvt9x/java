package com.userfront.service.UserServiceImpl;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Date;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.userfront.dao.PrimaryAccountDao;
import com.userfront.dao.SavingsAccountDao;
import com.userfront.domain.PrimaryAccount;
import com.userfront.domain.PrimaryTransaction;
import com.userfront.domain.SavingsAccount;
import com.userfront.domain.SavingsTransaction;
import com.userfront.domain.User;
import com.userfront.service.AccountService;
import com.userfront.service.TransactionService;
import com.userfront.service.UserService;

@Service
public class AccountServiceImpl implements AccountService {
	
	private static int nextAccountNumber = 11223145;
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\d{10,11}");

    @Autowired
    private PrimaryAccountDao primaryAccountDao;

    @Autowired
    private SavingsAccountDao savingsAccountDao;

    @Autowired
    private UserService userService;
    
    @Autowired
    private TransactionService transactionService;

    public PrimaryAccount createPrimaryAccount() {
        PrimaryAccount primaryAccount = new PrimaryAccount();
        primaryAccount.setAccountBalance(new BigDecimal(0.0));
        primaryAccount.setAccountNumber(accountGen());

        primaryAccountDao.save(primaryAccount);

        return primaryAccountDao.findByAccountNumber(primaryAccount.getAccountNumber());
    }

    public SavingsAccount createSavingsAccount() {
        SavingsAccount savingsAccount = new SavingsAccount();
        savingsAccount.setAccountBalance(new BigDecimal(0.0));
        savingsAccount.setAccountNumber(accountGen());

        savingsAccountDao.save(savingsAccount);

        return savingsAccountDao.findByAccountNumber(savingsAccount.getAccountNumber());
    }
    
    public void deposit(String accountType, double amount, Principal principal) {
        User user = userService.findByUsername(principal.getName());

        if (accountType.equalsIgnoreCase("Primary")) {
            PrimaryAccount primaryAccount = user.getPrimaryAccount();
            primaryAccount.setAccountBalance(primaryAccount.getAccountBalance().add(new BigDecimal(amount)));
            primaryAccountDao.save(primaryAccount);

            Date date = new Date();

            PrimaryTransaction primaryTransaction = new PrimaryTransaction(date, "Deposit to Primary Account", "Account", "Finished", amount, primaryAccount.getAccountBalance(), primaryAccount);
            transactionService.savePrimaryDepositTransaction(primaryTransaction);
            
        } else if (accountType.equalsIgnoreCase("Savings")) {
            SavingsAccount savingsAccount = user.getSavingsAccount();
            savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().add(new BigDecimal(amount)));
            savingsAccountDao.save(savingsAccount);

            Date date = new Date();
            SavingsTransaction savingsTransaction = new SavingsTransaction(date, "Deposit to savings Account", "Account", "Finished", amount, savingsAccount.getAccountBalance(), savingsAccount);
            transactionService.saveSavingsDepositTransaction(savingsTransaction);
        }
    }
    
    public void withdraw(String accountType, double amount, Principal principal) {
        User user = userService.findByUsername(principal.getName());

        if (accountType.equalsIgnoreCase("Primary")) {
            PrimaryAccount primaryAccount = user.getPrimaryAccount();
            primaryAccount.setAccountBalance(primaryAccount.getAccountBalance().subtract(new BigDecimal(amount)));
            primaryAccountDao.save(primaryAccount);

            Date date = new Date();

            PrimaryTransaction primaryTransaction = new PrimaryTransaction(date, "Withdraw from Primary Account", "Account", "Finished", amount, primaryAccount.getAccountBalance(), primaryAccount);
            transactionService.savePrimaryWithdrawTransaction(primaryTransaction);
        } else if (accountType.equalsIgnoreCase("Savings")) {
            SavingsAccount savingsAccount = user.getSavingsAccount();
            savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().subtract(new BigDecimal(amount)));
            savingsAccountDao.save(savingsAccount);

            Date date = new Date();
            SavingsTransaction savingsTransaction = new SavingsTransaction(date, "Withdraw from savings Account", "Account", "Finished", amount, savingsAccount.getAccountBalance(), savingsAccount);
            transactionService.saveSavingsWithdrawTransaction(savingsTransaction);
        }
    }

    public void mobileTopUp(String accountType, String phoneNumber, String carrier, double amount, Principal principal) {
        validateMobileTopUpRequest(accountType, phoneNumber, carrier, amount);

        User user = userService.findByUsername(principal.getName());
        String description = "Mobile top up for " + carrier + " - " + phoneNumber;
        Date date = new Date();

        if (accountType.equalsIgnoreCase("Primary")) {
            PrimaryAccount primaryAccount = user.getPrimaryAccount();
            ensureSufficientBalance(primaryAccount.getAccountBalance(), amount);
            primaryAccount.setAccountBalance(primaryAccount.getAccountBalance().subtract(BigDecimal.valueOf(amount)));
            primaryAccountDao.save(primaryAccount);

            PrimaryTransaction primaryTransaction = new PrimaryTransaction(date, description, "Mobile Top Up", "Finished", amount, primaryAccount.getAccountBalance(), primaryAccount);
            transactionService.savePrimaryWithdrawTransaction(primaryTransaction);
        } else if (accountType.equalsIgnoreCase("Savings")) {
            SavingsAccount savingsAccount = user.getSavingsAccount();
            ensureSufficientBalance(savingsAccount.getAccountBalance(), amount);
            savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().subtract(BigDecimal.valueOf(amount)));
            savingsAccountDao.save(savingsAccount);

            SavingsTransaction savingsTransaction = new SavingsTransaction(date, description, "Mobile Top Up", "Finished", amount, savingsAccount.getAccountBalance(), savingsAccount);
            transactionService.saveSavingsWithdrawTransaction(savingsTransaction);
        } else {
            throw new IllegalArgumentException("Invalid account type.");
        }
    }
    
    private int accountGen() {
        return ++nextAccountNumber;
    }

    private void validateMobileTopUpRequest(String accountType, String phoneNumber, String carrier, double amount) {
        if (!"Primary".equalsIgnoreCase(accountType) && !"Savings".equalsIgnoreCase(accountType)) {
            throw new IllegalArgumentException("Please select a valid account.");
        }

        if (phoneNumber == null || !PHONE_PATTERN.matcher(phoneNumber.trim()).matches()) {
            throw new IllegalArgumentException("Phone number must contain 10 or 11 digits.");
        }

        if (carrier == null || carrier.trim().isEmpty()) {
            throw new IllegalArgumentException("Please select a carrier.");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Top up amount must be greater than 0.");
        }
    }

    private void ensureSufficientBalance(BigDecimal balance, double amount) {
        if (balance.compareTo(BigDecimal.valueOf(amount)) < 0) {
            throw new IllegalArgumentException("Insufficient balance for this mobile top up.");
        }
    }

	

}
