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
import java.util.Optional;

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
        updateBalance(buyer.getUser().getId(), quoteCurrency, totalMoney.negate(), true); // -USDT blocked
        updateBalance(buyer.getUser().getId(), baseCurrency, quantity, false);            // +BTC available

        // --- LOGICA PENTRU SELLER ---
        // A avut marfa (BTC) blocată. Acum o pierde din "blocked",
        // dar primește banii (USDT) în "available".
        updateBalance(seller.getUser().getId(), baseCurrency, quantity.negate(), true); // -BTC blocked
        updateBalance(seller.getUser().getId(), quoteCurrency, totalMoney, false);       // +USDT available

        System.out.println("💰 Settlement finalizat: User " + buyer.getUser().getId() + " a cumpărat de la " + seller.getUser().getId());
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

    public Wallet createWallet(Wallet wallet) {
        return walletRepository.save(wallet);
    }

    public List<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }

    @Transactional
    public void releaseFunds(Long userId, String currency, BigDecimal amount) {
        // 1. Căutăm portofelul utilizatorului pentru moneda respectivă
        Wallet wallet = walletRepository.findByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new RuntimeException("Wallet not found for currency: " + currency));

        // 2. Verificare de siguranță: nu putem debloca mai mult decât avem blocat
        // (Deși teoretic nu ar trebui să se întâmple dacă restul logicii e ok)
        if (wallet.getBlockedAmount().compareTo(amount) < 0) {
            throw new RuntimeException("Inconsistent state: Trying to release more funds than locked.");
        }

        // 3. Mutăm banii
        // Scădem din balanța blocată
        wallet.setBlockedAmount(wallet.getBlockedAmount().subtract(amount));
        // Adăugăm înapoi în balanța disponibilă
        wallet.setAvailableAmount(wallet.getAvailableAmount().add(amount));

        // 4. Salvăm modificările
        walletRepository.save(wallet);
    }

    public Optional<Wallet> findWalletById(Long id) {
        return walletRepository.findById(id);
    }

    public void deleteWallet(Wallet wallet) {
        walletRepository.delete(wallet);
    }

    public void addFunds(Long userId, String currency, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new RuntimeException("Wallet not found for currency: " + currency));

        wallet.setAvailableAmount(wallet.getAvailableAmount().add(amount));
        walletRepository.save(wallet);
    }
}
