package com.tradeengine.engine.service;

import com.tradeengine.engine.core.engine.Engine;
import com.tradeengine.engine.core.model.OrderSide;
import com.tradeengine.engine.core.model.OrderStatus;
import com.tradeengine.engine.persistence.entity.Order;
import com.tradeengine.engine.persistence.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final Engine engine;
    private final WalletService walletService;

    public OrderService(OrderRepository orderRepository, Engine engine, WalletService walletService) {
        this.orderRepository = orderRepository;
        this.engine = engine;
        this.walletService = walletService;
    }

    public Order createOrder(Order order){
        order.setRemainingQuantity(order.getQuantity());
        order.setStatus(OrderStatus.PENDING);

        Order savedOrder = orderRepository.save(order);

        return orderRepository.save(savedOrder);
    }

    public Optional<Order> findOrderById(Long id){
        return orderRepository.findById(id);
    }

    public void deleteOrderFromDB(Order order){
        orderRepository.delete(order);
    }

    @Transactional
    public void cancelOrder(Order order) {
        engine.deleteOrderFromBook(order);

        BigDecimal amountToRelease;
        String currency;

        String[] currencies = order.getSymbol().split("/");
        if (order.getSide() == OrderSide.BUY) {
            currency = currencies[1]; // USDT
            amountToRelease = order.getRemainingQuantity().multiply(order.getPrice());
        } else {
            currency = currencies[0]; // BTC
            amountToRelease = order.getRemainingQuantity();
        }

        walletService.releaseFunds(order.getUser().getId(), currency, amountToRelease);

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }


}
