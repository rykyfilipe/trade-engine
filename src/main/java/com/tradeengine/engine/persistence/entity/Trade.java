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

    private Long makerOrderId;
    private Long takerOrderId;

    private BigDecimal price;
    private BigDecimal quantity;

    private LocalDateTime executedAt;

    @PrePersist
    protected void onCreate(){
        this.executedAt = LocalDateTime.now();
    }

}
