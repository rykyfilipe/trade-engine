package com.tradeengine.engine.api;

import com.tradeengine.engine.core.engine.Engine;
import com.tradeengine.engine.core.model.OrderSide;
import com.tradeengine.engine.core.model.OrderStatus;
import com.tradeengine.engine.persistence.entity.Order;
import com.tradeengine.engine.persistence.repository.OrderRepository;
import com.tradeengine.engine.service.WalletService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final Engine engine;
    private final WalletService walletService;

    public OrderController(OrderRepository orderRepository, Engine engine, WalletService walletService) {
        this.orderRepository = orderRepository;
        this.engine = engine;
        this.walletService = walletService;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {

        String[] currencies = order.getSymbol().split("/");
        String baseCurrency = currencies[0];  // BTC
        String quoteCurrency = currencies[1]; // USDT

        //  Decidem ce blocăm
        if (order.getSide() == OrderSide.BUY) {
            // CUMPĂR BTC: trebuie să am USDT (quote)
            // Suma de blocat = Preț * Cantitate
            BigDecimal totalCost = order.getPrice().multiply(order.getQuantity());
            walletService.lockFunds(order.getUserId(), quoteCurrency, totalCost);
        } else {
            // VÂND BTC: trebuie să am BTC (base)
            // Suma de blocat = Cantitatea de BTC pe care o ofer
            walletService.lockFunds(order.getUserId(), baseCurrency, order.getQuantity());
        }
        order.setRemainingQuantity(order.getQuantity());
        order.setStatus(OrderStatus.PENDING);

        Order savedOrder = orderRepository.save(order);

        engine.processOrder(savedOrder);

        orderRepository.save(savedOrder);

        return ResponseEntity.ok(savedOrder);
    }
}
