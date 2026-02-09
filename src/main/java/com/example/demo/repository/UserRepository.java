package com.example.demo.repository;

import com.example.demo.domain.entity.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.example.demo.domain.entity.User;

public interface UserRepository extends JpaRepository {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsernameOrEmail(String usernameOrEmail);
    Optional<User> findByUsername(String username);
    Optional<Dataset> findByOwner(User owner);
}
