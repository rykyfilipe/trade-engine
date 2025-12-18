package com.tradeengine.engine.api;

import com.tradeengine.engine.persistence.entity.User;
import com.tradeengine.engine.persistence.repository.UserRepository;
import com.tradeengine.engine.service.UserService;
import com.tradeengine.engine.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/users")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final WalletService walletService;

    public UserController(UserRepository userRepository, UserService userService, WalletService walletService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.walletService = walletService;
    }

    @PostMapping
    public ResponseEntity<?> addUser(@RequestBody User user) {
        if (userService.findUserByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body("Eroare: Username-ul '" + user.getUsername() + "' este deja ocupat.");
        }

        User savedUser = userRepository.save(user);

        walletService.createDefaultWallets(savedUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(){
        return ResponseEntity.status(HttpStatus.FOUND).body(userRepository.findAll());
    }
}
