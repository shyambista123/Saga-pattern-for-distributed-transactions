package com.example.orderservice.controller;


import com.example.basedomains.event.OrderCreatedEvent;
import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.entity.Order;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping
    public ResponseEntity<String> placeOrder(@RequestBody OrderDto orderDto){
        Order newOrder = new Order();
        newOrder.setStatus("PENDING");
        newOrder.setProduct(orderDto.getProduct());
        newOrder.setQuantity(orderDto.getQuantity());
        orderRepository.save(newOrder);
        OrderCreatedEvent event = new OrderCreatedEvent(newOrder.getId(), orderDto.getProduct(), orderDto.getQuantity());
        Message<OrderCreatedEvent> message = MessageBuilder.withPayload(event)
                .setHeader(KafkaHeaders.TOPIC,"order-created").build();
        kafkaTemplate.send(message);

        return ResponseEntity.ok("Order created with id: " + newOrder.getId());
    }
}
