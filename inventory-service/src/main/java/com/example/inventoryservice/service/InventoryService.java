package com.example.inventoryservice.service;

import com.example.basedomains.event.InventoryFailedEvent;
import com.example.basedomains.event.InventorySuccessEvent;
import com.example.basedomains.event.PaymentFailedEvent;
import com.example.inventoryservice.config.KafkaConfig;
import com.example.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import com.example.basedomains.event.OrderCreatedEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "order-created", groupId = KafkaConfig.CONSUMER_GROUP_ID)
    public void reserveInventory(OrderCreatedEvent event){
        inventoryRepository.findByProduct(event.getProduct()).ifPresentOrElse(
                inventory -> {
                    if (inventory.getQuantity()>=event.getQuantity()){
                        inventory.setQuantity(inventory.getQuantity() - event.getQuantity());
                        inventoryRepository.save(inventory);
                        InventorySuccessEvent inventorySuccessEvent =
                                new InventorySuccessEvent(event.getOrderId(), event.getProduct(), event.getQuantity());
                        Message<InventorySuccessEvent> message = MessageBuilder.withPayload(inventorySuccessEvent)
                                .setHeader(KafkaHeaders.TOPIC, "inventory-success").build();
                        kafkaTemplate.send("inventory-success", inventorySuccessEvent);
                        log.info("-----INVENTORY SUCCESS SENT-----");
                    }
                    else {
                        InventoryFailedEvent inventoryFailedEvent =
                                new InventoryFailedEvent(event.getOrderId(), "Not enough stock");
                        Message<InventoryFailedEvent> message = MessageBuilder.withPayload(inventoryFailedEvent)
                                .setHeader(KafkaHeaders.TOPIC, "inventory-success").build();
                        kafkaTemplate.send("inventory-failed", inventoryFailedEvent);
                        log.info("----- INVENTORY FAILED SENT------");
                    }
                },()-> {
                    InventoryFailedEvent inventoryFailedEvent =
                            new InventoryFailedEvent(event.getOrderId(), "Not enough stock");
                    Message<InventoryFailedEvent> message = MessageBuilder.withPayload(inventoryFailedEvent)
                            .setHeader(KafkaHeaders.TOPIC, "inventory-success").build();
                    kafkaTemplate.send("inventory-failed", inventoryFailedEvent);
                    log.info("----- INVENTORY FAILED SENT ------");
                }
        );
    }

    @KafkaListener(topics = "payment-failed", groupId = KafkaConfig.CONSUMER_GROUP_ID)
    public void handlePaymentFailed(PaymentFailedEvent event){
        inventoryRepository.findByProduct(event.getProduct()).ifPresent(inventory -> {
            inventory.setQuantity(inventory.getQuantity() + event.getQuantity());
            inventoryRepository.save(inventory);
        });
    }
}
