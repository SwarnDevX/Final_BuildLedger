package com.buildledger.service.impl;

import com.buildledger.dto.request.LoginRequestDTO;
import com.buildledger.dto.response.LoginResponseDTO;
import com.buildledger.entity.User;
import com.buildledger.exception.ResourceNotFoundException;
import com.buildledger.repository.UserRepository;
import com.buildledger.security.JwtUtils;
import com.buildledger.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        log.info("Login attempt for user: {}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", request.getUsername()));

        String jwt = jwtUtils.generateToken(userDetails, user.getUserId(), user.getRole());

        log.info("User {} logged in successfully with role {}", user.getUsername(), user.getRole());

        return LoginResponseDTO.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000)
                .userId(user.getUserId())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }
}
