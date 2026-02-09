package com.example.demo.service.impl;

import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.dto.RegisterRequestDTO;
import com.example.demo.dto.RegisterResponseDTO;
import com.example.demo.repository.UserRepository;
import com.example.demo.domain.entity.User;
import com.example.demo.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public abstract class AuthService implements com.example.demo.service.AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public RegisterResponseDTO register(RegisterRequestDTO request) {
        if(userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already in use");
        }if(userRepository.existsByUsername(request.username())) {
            throw new RuntimeException("Username already in use");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getUsername());

        return new RegisterResponseDTO(LocalDateTime.now(), token);
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        if(userRepository.existsByUsernameOrEmail(request.usernameOrEmail())){
            User user = userRepository.findByUsername(request.usernameOrEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if(passwordEncoder.matches(request.password(), user.getPassword())) {
                String token = jwtTokenProvider.generateToken(user.getUsername());
                return new LoginResponseDTO(token);
            } else {
                throw new RuntimeException("Invalid password");
            }
        } else {
            throw new RuntimeException("User not found");
        }
    }
}
