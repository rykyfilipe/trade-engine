package com.tradeengine.engine.persistence.entity;

import com.tradeengine.engine.core.model.OrderSide;
import com.tradeengine.engine.core.model.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String symbol;

    @Enumerated(EnumType.STRING)
    private OrderSide side;

    private BigDecimal price;

    private BigDecimal quantity;

    private BigDecimal remainingQuantity;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime timestamp;

    protected void onCreate(){
        this.timestamp = LocalDateTime.now();

        if(this.remainingQuantity == null){
            this.remainingQuantity = this.quantity;
        }
    }


}
