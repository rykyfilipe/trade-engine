package com.tradeengine.engine.persistence.entity;

import com.tradeengine.engine.core.model.OrderSide;
import com.tradeengine.engine.core.model.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String symbol;

    @Enumerated(EnumType.STRING)
    private OrderSide side;

    @Column(precision = 20, scale = 8)
    private BigDecimal price;

    @Column(precision = 20, scale = 8)
    private BigDecimal quantity;

    @Column(precision = 20, scale = 8)
    private BigDecimal remainingQuantity;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime timestamp;

    @OneToMany(mappedBy = "sellerOrder")
    private List<Trade> tradesAsMaker;

    @OneToMany(mappedBy = "buyerOrder")
    private List<Trade> tradesAsTaker;

    protected void onCreate(){
        this.timestamp = LocalDateTime.now();

        if(this.remainingQuantity == null){
            this.remainingQuantity = this.quantity;
        }
    }


}
