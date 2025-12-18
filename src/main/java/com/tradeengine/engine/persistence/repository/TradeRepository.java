package com.tradeengine.engine.persistence.repository;

import com.tradeengine.engine.persistence.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<Trade,Long> {
}
