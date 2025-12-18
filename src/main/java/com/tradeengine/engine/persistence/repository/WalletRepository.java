package com.tradeengine.engine.persistence.repository;

import com.tradeengine.engine.persistence.entity.User;
import com.tradeengine.engine.persistence.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet,Long> {
    Optional<Wallet> findByUserIdAndCurrency(Long userId, String currency);

    Long user(User user);
}
