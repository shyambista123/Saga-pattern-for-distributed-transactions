package com.example.paymentservice.controller;

import com.example.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/success/{id}")
    public String processSuccessPayment(@PathVariable Long id){
        paymentService.simulateSuccessPayment(id);
        return "Payment success for order with order id: " + id;
    }

    @PostMapping("/failure/{id}")
    public String processFailurePayment(@PathVariable Long id){
        paymentService.simulateFailurePayment(id);
        return "Payment failed for order with order id: " + id;
    }
}
