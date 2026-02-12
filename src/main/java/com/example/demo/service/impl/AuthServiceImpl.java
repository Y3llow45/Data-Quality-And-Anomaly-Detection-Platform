package com.example.demo.service.impl;

import com.example.demo.domain.entity.Role;
import com.example.demo.domain.enums.RoleName;
import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.dto.RegisterRequestDTO;
import com.example.demo.dto.RegisterResponseDTO;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.domain.entity.User;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RoleRepository roleRepository;

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
        Role userRole = roleRepository.findByRole(RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        user.setRole(userRole);
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
