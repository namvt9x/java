package com.userfront;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public PaymentResponse execute(@RequestBody PaymentRequest request) {
        return paymentService.execute(request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<PaymentResponse> handleIllegalArgument(IllegalArgumentException exception) {
        PaymentResponse response = new PaymentResponse(false, false, false, exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
