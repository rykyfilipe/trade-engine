package com.tradeengine.engine.api;

import com.tradeengine.engine.core.model.WalletRequest;
import com.tradeengine.engine.persistence.entity.Order;
import com.tradeengine.engine.persistence.entity.User;
import com.tradeengine.engine.persistence.entity.Wallet;
import com.tradeengine.engine.persistence.repository.UserRepository;
import com.tradeengine.engine.persistence.repository.WalletRepository;
import com.tradeengine.engine.service.UserService;
import com.tradeengine.engine.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService1) {
        this.walletService = walletService1;
    }

    @PostMapping
    public ResponseEntity<Wallet> addWallet(@RequestBody Wallet wallet){
        Wallet newWallet = walletService.createWallet(wallet);

        return  ResponseEntity.status(HttpStatus.CREATED).body(newWallet);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteWallet(@PathVariable Long id) {

        Wallet wallet;

        try{
            wallet = walletService.findWalletById(id)
                    .orElseThrow(() -> new RuntimeException("Wallet not found!") );

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Wallet not found!");
        }

        walletService.deleteWallet(wallet);

        return ResponseEntity.status(HttpStatus.OK).body("Wallet with id : " + id.toString() + " deleted");
    }

    @GetMapping
    public ResponseEntity<List<Wallet>> getAllWallets(){
        List<Wallet> wallets = walletService.getAllWallets();

        return ResponseEntity.status(HttpStatus.FOUND).body(wallets);
    }

    @PostMapping("/deposit")
    public ResponseEntity<String> depositFunds(@RequestBody WalletRequest request){
        walletService.addFunds(request.getUserId(),request.getCurrency(),request.getAmount());

        return ResponseEntity.ok("Deposit successful for " + request.getCurrency());    }
}
