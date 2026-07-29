package com.userfront.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.userfront.domain.User;
import com.userfront.domain.Company;
import com.userfront.service.CompanyService;
import com.userfront.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CompanyService companyService;

    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public String profile(Principal principal, Model model) {
        User user = userService.findByUsername(principal.getName());
        addProfileAttributes(model, user);

        return "profile";
    }

    @RequestMapping(value = "/profile", method = RequestMethod.POST)
    public String profilePost(@ModelAttribute("user") User newUser, Principal principal, Model model) {
        User user = userService.findByUsername(principal.getName());
        user.setUsername(newUser.getUsername());
        user.setFirstName(newUser.getFirstName());
        user.setLastName(newUser.getLastName());
        user.setEmail(newUser.getEmail());
        user.setPhone(newUser.getPhone());

        userService.saveUser(user);
        addProfileAttributes(model, user);

        return "profile";
    }

    private void addProfileAttributes(Model model, User user) {
        Company company = null;

        if (user.getCompany() != null) {
            company = companyService.findById(user.getCompany().getCompanyId());
        }

        model.addAttribute("user", user);
        model.addAttribute("company", company);
        model.addAttribute("companyUser", company != null);
    }

}
