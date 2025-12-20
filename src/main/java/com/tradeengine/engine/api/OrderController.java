package com.tradeengine.engine.api;

import com.tradeengine.engine.core.engine.Engine;
import com.tradeengine.engine.core.model.OrderSide;
import com.tradeengine.engine.core.model.OrderStatus;
import com.tradeengine.engine.persistence.entity.Order;
import com.tradeengine.engine.persistence.repository.OrderRepository;
import com.tradeengine.engine.service.OrderService;
import com.tradeengine.engine.service.WalletService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final Engine engine;

    private final WalletService walletService;
    private final OrderService orderService;

    public OrderController(OrderRepository orderRepository, Engine engine, WalletService walletService, OrderService orderService) {
        this.orderRepository = orderRepository;
        this.engine = engine;
        this.walletService = walletService;
        this.orderService = orderService;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {

        String[] currencies = order.getSymbol().split("/");
        String baseCurrency = currencies[0];  // BTC
        String quoteCurrency = currencies[1]; // USDT

        if (order.getSide() == OrderSide.BUY) {
            // CUMPĂR BTC: trebuie să am USDT (quote)
            // Suma de blocat = Preț * Cantitate
            BigDecimal totalCost = order.getPrice().multiply(order.getQuantity());
            walletService.lockFunds(order.getUser().getId(), quoteCurrency, totalCost);
        } else {
            // VÂND BTC: trebuie să am BTC (base)
            // Suma de blocat = Cantitatea de BTC pe care o ofer
            walletService.lockFunds(order.getUser().getId(), baseCurrency, order.getQuantity());
        }

        Order savedOrder = orderService.createOrder(order);
        engine.processOrder(savedOrder);

        return ResponseEntity.ok(savedOrder);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> cancelOrder(@PathVariable Long id) {

        Order order;

        try{
            order = orderService.findOrderById(id)
                    .orElseThrow(() -> new RuntimeException("Order not found!") );

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found!");
        }

        orderService.cancelOrder(order);

        return ResponseEntity.status(HttpStatus.OK).body("Order with id : " + id.toString() + " deleted");
    }

}
