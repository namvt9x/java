package com.userfront.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.userfront.domain.User;
import com.userfront.service.UserService;

@RestController
@RequestMapping("/promotions")
public class PromotionController {

    private static final int LOYAL_CUSTOMER_YEARS = 5;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/loyalty", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getLoyaltyPromotions(Principal principal) {
        User user = userService.findByUsername(principal.getName());

        return ResponseEntity.ok(buildPromotionResponse(user));
    }

    @RequestMapping(value = "/loyalty/apply", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> applyPromotionCode(Principal principal,
                                                                  @RequestParam("code") String code) {
        User user = userService.findByUsername(principal.getName());
        Map<String, Object> response = buildPromotionResponse(user);
        List<Map<String, Object>> promotionCodes = getPromotionCodes();

        if (!(Boolean) response.get("eligible")) {
            response.put("applied", false);
            response.put("message", "Customer is not eligible for loyalty promotion codes yet.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        for (Map<String, Object> promotionCode : promotionCodes) {
            if (((String) promotionCode.get("code")).equalsIgnoreCase(code)) {
                response.put("applied", true);
                response.put("appliedCode", promotionCode);
                response.put("message", "Promotion code applied successfully.");
                return ResponseEntity.ok(response);
            }
        }

        response.put("applied", false);
        response.put("message", "Promotion code does not exist.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    private Map<String, Object> buildPromotionResponse(User user) {
        Map<String, Object> response = new LinkedHashMap<>();
        boolean eligible = isEligibleForLoyaltyPromotion(user);

        response.put("username", user.getUsername());
        response.put("customerSince", user.getCustomerSince());
        response.put("requiredYears", LOYAL_CUSTOMER_YEARS);
        response.put("eligible", eligible);
        response.put("promotionCodes", eligible ? getPromotionCodes() : Collections.emptyList());

        if (user.getCustomerSince() == null) {
            response.put("message", "Customer join date is missing, so loyalty promotions cannot be applied.");
        } else if (eligible) {
            response.put("message", "Customer is eligible for loyalty promotion codes.");
        } else {
            response.put("message", "Customer must have more than 5 years with the bank to use loyalty promotion codes.");
        }

        return response;
    }

    private boolean isEligibleForLoyaltyPromotion(User user) {
        if (user.getCustomerSince() == null) {
            return false;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.YEAR, -LOYAL_CUSTOMER_YEARS);

        return user.getCustomerSince().before(calendar.getTime());
    }

    private List<Map<String, Object>> getPromotionCodes() {
        List<Map<String, Object>> promotionCodes = new ArrayList<>();
        promotionCodes.add(buildPromotionCode("LOYAL5", "5% fee discount for loyal customers"));
        promotionCodes.add(buildPromotionCode("LOYAL10", "10% discount on premium banking service"));
        promotionCodes.add(buildPromotionCode("VIPPERK", "Free priority processing for selected requests"));
        return promotionCodes;
    }

    private Map<String, Object> buildPromotionCode(String code, String description) {
        Map<String, Object> promotionCode = new LinkedHashMap<>();
        promotionCode.put("code", code);
        promotionCode.put("description", description);
        return promotionCode;
    }
}
