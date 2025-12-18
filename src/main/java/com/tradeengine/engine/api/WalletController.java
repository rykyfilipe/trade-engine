package com.tradeengine.engine.api;

import com.tradeengine.engine.persistence.entity.User;
import com.tradeengine.engine.persistence.entity.Wallet;
import com.tradeengine.engine.persistence.repository.UserRepository;
import com.tradeengine.engine.persistence.repository.WalletRepository;
import com.tradeengine.engine.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/wallet")
public class WalletController {

    private final WalletRepository walletRepository;

    public WalletController(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @PostMapping
    public ResponseEntity<Wallet> addUser(@RequestBody Wallet wallet){



        return  ResponseEntity.status(HttpStatus.CREATED).body(walletRepository.save(wallet));
    }

    @GetMapping
    public ResponseEntity<List<Wallet>> getAllWallets(){
        return ResponseEntity.status(HttpStatus.FOUND).body(walletRepository.findAll());
    }
}
