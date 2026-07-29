package com.userfront;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/execute")
    public PaymentResponse execute(@RequestBody PaymentRequest request) {
        return paymentService.execute(request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<PaymentResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(new PaymentResponse(false, false, false, ex.getMessage()));
    }
}
