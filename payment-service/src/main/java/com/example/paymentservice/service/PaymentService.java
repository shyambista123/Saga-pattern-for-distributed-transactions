package com.example.paymentservice.service;

import com.example.basedomains.event.InventorySuccessEvent;
import com.example.basedomains.event.PaymentFailedEvent;
import com.example.basedomains.event.PaymentSuccessEvent;
import com.example.paymentservice.config.KafkaConfig;
import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "inventory-success", groupId = KafkaConfig.CONSUMER_GROUP_ID)
    public void handleInventorySuccess(InventorySuccessEvent event){
        simulatePendingPayment(event);
        log.info("Received inventory-success for orderId: {}", event.getOrderId());
    }

    public void simulateSuccessPayment(Long id) {
        Payment payment = paymentRepository.findByOrderId(id)
                .orElse(new Payment());

        payment.setOrderId(id);
        payment.setStatus("COMPLETED");
        paymentRepository.save(payment);
        PaymentSuccessEvent paymentSuccessEvent = new PaymentSuccessEvent(id);
        Message<PaymentSuccessEvent> message = MessageBuilder.withPayload(paymentSuccessEvent)
                .setHeader(KafkaHeaders.TOPIC, "payment-success").build();
        kafkaTemplate.send(message);
        log.info("Sent payment success for orderId: {}", id);
    }

    public void simulateFailurePayment(Long id) {
        Payment payment = paymentRepository.findByOrderId(id)
                .orElse(new Payment());

        payment.setOrderId(id);
        payment.setStatus("FAILED");
        paymentRepository.save(payment);
        PaymentFailedEvent paymentFailedEvent =
                new PaymentFailedEvent(payment.getOrderId(), payment.getProduct(), payment.getQuantity());

        Message<PaymentFailedEvent> message = MessageBuilder.withPayload(paymentFailedEvent)
                .setHeader(KafkaHeaders.TOPIC, "payment-failed").build();
        kafkaTemplate.send(message);
        log.info("Sent payment failed for orderId: {}", id);
    }

    public void simulatePendingPayment(InventorySuccessEvent event) {
        Payment payment = new Payment();
        payment.setOrderId(event.getOrderId());
        payment.setProduct(event.getProduct());
        payment.setQuantity(event.getQuantity());
        payment.setStatus("PENDING");
        paymentRepository.save(payment);
        log.info("Payment pending for orderId: {}", event.getOrderId());
    }
}
