package com.tradeengine.engine.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trades")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maker_order_id", nullable = false)
    private Order sellerOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taker_order_id", nullable = false)
    private Order buyerOrder;

    @Column(precision = 20, scale = 8)
    private BigDecimal price;

    @Column(precision = 20, scale = 8)
    private BigDecimal quantity;

    private String symbol;

    private LocalDateTime executedAt;

    @PrePersist
    protected void onCreate(){
        this.executedAt = LocalDateTime.now();
    }

}
