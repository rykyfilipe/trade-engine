package com.tradeengine.engine.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "wallets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "currency"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties("wallets")
    private User user;

    private String currency; // BTC, USDT, etc.
    @Column(precision = 20, scale = 8)
    private BigDecimal availableAmount; // Banii pe care îi poate folosi
    @Column(precision = 20, scale = 8)
    private BigDecimal blockedAmount;
}
