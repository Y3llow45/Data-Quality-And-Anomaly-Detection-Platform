package com.example.demo.service.impl;

import com.example.demo.domain.entity.Role;
import com.example.demo.domain.enums.RoleName;
import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.dto.RegisterRequestDTO;
import com.example.demo.dto.RegisterResponseDTO;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.domain.entity.User;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RoleRepository roleRepository;
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Override
    @Transactional
    public RegisterResponseDTO register(RegisterRequestDTO request) {
        try {
            if (userRepository.existsByEmail(request.email())) {
                throw new ApiException("Invalid credentials!");
            }
            if (userRepository.existsByUsername(request.username())) {
                throw new ApiException("Invalid credentials!");
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
        }catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected registration error", e);
            throw new ApiException("Server error during registration!");
        }
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        try {
            User user = userRepository
                    .findByUsernameOrEmail(request.usernameOrEmail(), request.usernameOrEmail())
                    .orElseThrow(() -> new ApiException("Invalid credentials"));

            if (!passwordEncoder.matches(request.password(), user.getPassword())) {
                throw new ApiException("Invalid credentials");
            }

            String token = jwtTokenProvider.generateToken(user.getUsername());
            return new LoginResponseDTO(token);

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected login error", e);
            throw new ApiException("Server error during login!");
        }
    }
}
