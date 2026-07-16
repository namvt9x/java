package com.userfront.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.userfront.domain.Company;
import com.userfront.service.CompanyService;

@Controller
@RequestMapping("/company")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public String profile(Principal principal, Model model) {
        Company company = companyService.findByUsername(principal.getName());

        if (company == null) {
            company = new Company();
        }

        model.addAttribute("company", company);

        return "companyProfile";
    }

    @RequestMapping(value = "/profile", method = RequestMethod.POST)
    public String profilePost(@ModelAttribute("company") Company company, Principal principal, Model model) {
        Company savedCompany = companyService.saveForUser(principal.getName(), company);
        model.addAttribute("company", savedCompany);
        model.addAttribute("successMessage", true);

        return "companyProfile";
    }
}
