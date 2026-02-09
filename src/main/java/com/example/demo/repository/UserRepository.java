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
    @Query("""
    select count(u) > 0
    from User u
    where u.username = :value or u.email = :value
""")
    boolean existsByUsernameOrEmail(@Param("value") String value);

    Optional<User> findByUsername(String username);
}
