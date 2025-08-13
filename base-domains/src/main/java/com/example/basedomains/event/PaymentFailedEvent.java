package com.example.basedomains.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentFailedEvent {
    private Long orderId;
    private String product;
    private int quantity;
}
