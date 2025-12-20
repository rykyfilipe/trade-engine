package com.tradeengine.engine.core.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletRequest {

    private Long userId;
    private String currency;
    private BigDecimal amount;
}
