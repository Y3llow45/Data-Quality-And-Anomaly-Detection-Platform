package com.example.demo.repository;

import com.example.demo.domain.entity.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.example.demo.domain.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);

    Optional<User> findByUsername(String username);
}
