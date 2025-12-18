package com.tradeengine.engine.persistence.repository;

import com.tradeengine.engine.core.model.OrderStatus;
import com.tradeengine.engine.persistence.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findByStatusInOrderByIdAsc(List<OrderStatus> statuses);
}
