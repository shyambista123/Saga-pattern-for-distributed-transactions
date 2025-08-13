package com.example.basedomains.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentSuccessEvent {
    private Long orderId;
    private String product;
    private int quantity;

    public PaymentSuccessEvent(Long orderId) {
        this.orderId = orderId;
    }
}
