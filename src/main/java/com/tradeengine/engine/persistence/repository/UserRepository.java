package com.tradeengine.engine.persistence.repository;

import com.tradeengine.engine.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository  extends JpaRepository<User,Long> {
    Optional<User> findByUsername(String username);
}
