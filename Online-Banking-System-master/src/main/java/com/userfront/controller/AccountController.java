package com.userfront.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.userfront.domain.PrimaryAccount;
import com.userfront.domain.PrimaryTransaction;
import com.userfront.domain.SavingsAccount;
import com.userfront.domain.SavingsTransaction;
import com.userfront.domain.User;
import com.userfront.service.AccountService;
import com.userfront.service.TransactionService;
import com.userfront.service.UserService;

@Controller
@RequestMapping("/account")
public class AccountController {
	
	@Autowired
    private UserService userService;
	
	@Autowired
	private AccountService accountService;
	
	@Autowired
	private TransactionService transactionService;
	
	@RequestMapping("/primaryAccount")
	public String primaryAccount(Model model, Principal principal) {
		List<PrimaryTransaction> primaryTransactionList = transactionService.findPrimaryTransactionList(principal.getName());
		
		User user = userService.findByUsername(principal.getName());
        PrimaryAccount primaryAccount = user.getPrimaryAccount();

        model.addAttribute("primaryAccount", primaryAccount);
        model.addAttribute("primaryTransactionList", primaryTransactionList);
		
		return "primaryAccount";
	}

	@RequestMapping("/savingsAccount")
    public String savingsAccount(Model model, Principal principal) {
		List<SavingsTransaction> savingsTransactionList = transactionService.findSavingsTransactionList(principal.getName());
        User user = userService.findByUsername(principal.getName());
        SavingsAccount savingsAccount = user.getSavingsAccount();

        model.addAttribute("savingsAccount", savingsAccount);
        model.addAttribute("savingsTransactionList", savingsTransactionList);

        return "savingsAccount";
    }
	
	@RequestMapping(value = "/deposit", method = RequestMethod.GET)
    public String deposit(Model model) {
        model.addAttribute("accountType", "");
        model.addAttribute("amount", "");

        return "deposit";
    }

    @RequestMapping(value = "/deposit", method = RequestMethod.POST)
    public String depositPOST(@ModelAttribute("amount") String amount, @ModelAttribute("accountType") String accountType, Principal principal) {
        accountService.deposit(accountType, Double.parseDouble(amount), principal);

        return "redirect:/userFront";
    }
    
    @RequestMapping(value = "/withdraw", method = RequestMethod.GET)
    public String withdraw(Model model) {
        model.addAttribute("accountType", "");
        model.addAttribute("amount", "");

        return "withdraw";
    }

    @RequestMapping(value = "/withdraw", method = RequestMethod.POST)
    public String withdrawPOST(@ModelAttribute("amount") String amount, @ModelAttribute("accountType") String accountType, Principal principal) {
        accountService.withdraw(accountType, Double.parseDouble(amount), principal);

        return "redirect:/userFront";
    }

    @RequestMapping(value = "/mobileTopup", method = RequestMethod.GET)
    public String mobileTopup(Model model, Principal principal) {
        populateAccountBalances(model, principal);

        if (!model.containsAttribute("accountType")) {
            model.addAttribute("accountType", "");
        }
        if (!model.containsAttribute("phoneNumber")) {
            model.addAttribute("phoneNumber", "");
        }
        if (!model.containsAttribute("carrier")) {
            model.addAttribute("carrier", "");
        }
        if (!model.containsAttribute("amount")) {
            model.addAttribute("amount", "");
        }

        return "mobileTopup";
    }

    @RequestMapping(value = "/mobileTopup", method = RequestMethod.POST)
    public String mobileTopupPost(
            @ModelAttribute("accountType") String accountType,
            @ModelAttribute("phoneNumber") String phoneNumber,
            @ModelAttribute("carrier") String carrier,
            @ModelAttribute("amount") String amount,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addFlashAttribute("accountType", accountType);
        redirectAttributes.addFlashAttribute("phoneNumber", phoneNumber);
        redirectAttributes.addFlashAttribute("carrier", carrier);
        redirectAttributes.addFlashAttribute("amount", amount);

        try {
            accountService.mobileTopUp(accountType, phoneNumber, carrier, Double.parseDouble(amount), principal);
            redirectAttributes.addFlashAttribute("successMessage", "Mobile top up completed successfully.");
            redirectAttributes.addFlashAttribute("accountType", "");
            redirectAttributes.addFlashAttribute("phoneNumber", "");
            redirectAttributes.addFlashAttribute("carrier", "");
            redirectAttributes.addFlashAttribute("amount", "");
        } catch (NumberFormatException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Amount must be a valid number.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/account/mobileTopup";
    }

    private void populateAccountBalances(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        model.addAttribute("primaryAccount", user.getPrimaryAccount());
        model.addAttribute("savingsAccount", user.getSavingsAccount());
    }
}
