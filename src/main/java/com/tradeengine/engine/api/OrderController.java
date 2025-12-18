package com.tradeengine.engine.api;

import com.tradeengine.engine.core.engine.Engine;
import com.tradeengine.engine.core.model.OrderStatus;
import com.tradeengine.engine.persistence.entity.Order;
import com.tradeengine.engine.persistence.repository.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final Engine engine;

    public OrderController(OrderRepository orderRepository, Engine engine) {
        this.orderRepository = orderRepository;
        this.engine = engine;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {

        order.setRemainingQuantity(order.getQuantity());
        order.setStatus(OrderStatus.PENDING);

        Order savedOrder = orderRepository.save(order);

        engine.processOrder(savedOrder);

        orderRepository.save(savedOrder);

        return ResponseEntity.ok(savedOrder);
    }
}
