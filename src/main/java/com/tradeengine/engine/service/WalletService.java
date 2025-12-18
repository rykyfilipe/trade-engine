package com.tradeengine.engine.service;

import com.tradeengine.engine.core.model.OrderSide;
import com.tradeengine.engine.persistence.entity.Order;
import com.tradeengine.engine.persistence.entity.User;
import com.tradeengine.engine.persistence.entity.Wallet;
import com.tradeengine.engine.persistence.repository.WalletRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional
    public void lockFunds(Long userId, String currency, BigDecimal amount){
        Wallet wallet = walletRepository.findByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if(wallet.getAvailableAmount().compareTo(amount) < 0){
            throw new RuntimeException("Insufficient funds!");
        }

        wallet.setAvailableAmount(wallet.getAvailableAmount().subtract(amount));
        wallet.setBlockedAmount(wallet.getBlockedAmount().add(amount));

        walletRepository.save(wallet);
    }

    @Transactional
    public void settleTrade(Order maker, Order taker, BigDecimal quantity, BigDecimal price) {
        //  Identificăm cine este Buyer și cine este Seller
        Order buyer = (taker.getSide() == OrderSide.BUY) ? taker : maker;
        Order seller = (taker.getSide() == OrderSide.SELL) ? taker : maker;

        //Extragem monedele (ex: BTC/USDT)
        String[] currencies = taker.getSymbol().split("/");
        String baseCurrency = currencies[0];  // BTC (Marfa)
        String quoteCurrency = currencies[1]; // USDT (Banii)

        BigDecimal totalMoney = quantity.multiply(price);

        // --- LOGICA PENTRU BUYER ---
        // A avut banii (USDT) blocați. Acum îi pierde definitiv din "blocked",
        // dar primește marfa (BTC) în "available".
        updateBalance(buyer.getUserId(), quoteCurrency, totalMoney.negate(), true); // -USDT blocked
        updateBalance(buyer.getUserId(), baseCurrency, quantity, false);            // +BTC available

        // --- LOGICA PENTRU SELLER ---
        // A avut marfa (BTC) blocată. Acum o pierde din "blocked",
        // dar primește banii (USDT) în "available".
        updateBalance(seller.getUserId(), baseCurrency, quantity.negate(), true); // -BTC blocked
        updateBalance(seller.getUserId(), quoteCurrency, totalMoney, false);       // +USDT available

        System.out.println("💰 Settlement finalizat: User " + buyer.getUserId() + " a cumpărat de la " + seller.getUserId());
    }

    private void updateBalance(Long userId, String currency, BigDecimal amount, boolean fromBlocked) {
        Wallet wallet = walletRepository.findByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new RuntimeException("Portofel inexistent pentru: " + currency));

        if (fromBlocked) {
            // Modificăm suma blocată (ex: -100 USDT)
            wallet.setBlockedAmount(wallet.getBlockedAmount().add(amount));
        } else {
            // Modificăm suma disponibilă (ex: +0.5 BTC)
            wallet.setAvailableAmount(wallet.getAvailableAmount().add(amount));
        }

        walletRepository.save(wallet);
    }

    public void createDefaultWallets(User savedUser) {
        List<String> currencies = List.of("BTC", "USDT");
        for (String curr : currencies) {
            Wallet wallet = new Wallet();
            wallet.setUser(savedUser);
            wallet.setCurrency(curr);
            wallet.setAvailableAmount(BigDecimal.ZERO);
            wallet.setBlockedAmount(BigDecimal.ZERO);
            walletRepository.save(wallet);
        }
    }
}
