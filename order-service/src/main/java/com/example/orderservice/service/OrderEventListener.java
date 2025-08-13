package com.example.orderservice.service;

import com.example.basedomains.event.InventoryFailedEvent;
import com.example.basedomains.event.PaymentFailedEvent;
import com.example.basedomains.event.PaymentSuccessEvent;
import com.example.orderservice.config.KafkaConfig;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventListener {
    private final OrderRepository orderRepository;

    @KafkaListener(topics = "inventory-failed", groupId = KafkaConfig.CONSUMER_GROUP_ID)
    public void handleInventoryFailureEvent(InventoryFailedEvent event){
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            order.setStatus("CANCELLED");
            orderRepository.save(order);
            log.info(event.getReason());
        });
    }

    @KafkaListener(topics = "payment-failed", groupId = KafkaConfig.CONSUMER_GROUP_ID)
    public void handlePaymentFailureEvent(PaymentFailedEvent event){
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            order.setStatus("CANCELLED");
            orderRepository.save(order);
            log.info("Order with id {} cancelled due to payment failure event", event.getOrderId());
        });
    }

    @KafkaListener(topics = {"payment-success"}, groupId = KafkaConfig.CONSUMER_GROUP_ID)
    public void handleSuccessEvent(PaymentSuccessEvent event){
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            order.setStatus("COMPLETED");
            orderRepository.save(order);
            log.info("Order with id {} COMPLETED", event.getOrderId());
        });
    }
}
