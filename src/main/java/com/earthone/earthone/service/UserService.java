package com.earthone.earthone.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.earthone.earthone.entity.AuthenticationDataEntity;
import com.earthone.earthone.entity.UserEntity;
import com.earthone.earthone.entity.UserRole;
import com.earthone.earthone.repository.AuthenticationRepository;
import com.earthone.earthone.repository.UserRepository;
import com.earthone.earthone.security.TokenDetails;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final AuthenticationRepository authenticationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<UserEntity> registerUser(UserEntity user) {
        return userRepository.save(
                user.toBuilder()
                .password(passwordEncoder.encode(user.getPassword()))
                .role(UserRole.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()
        ).doOnSuccess(u -> {
            log.info("IN registerUser - user: {} created", u);
        });
    }

    public Mono<TokenDetails> saveAuthData(TokenDetails tokenData, ServerHttpRequest request) {
        @SuppressWarnings("null")
        String ip = request.getRemoteAddress().getAddress().getHostAddress();
        if(ip == null) {
            ip = "unknown";
        }
        log.info("user was logedin with id - {}, and ip - {}", tokenData.getUserId(), ip);
        
        return authenticationRepository.save(AuthenticationDataEntity.builder()
                .userId(tokenData.getUserId())
                .authEntity(tokenData.getXToken())
                .expiresAt(tokenData.getXTokenExpiresAt()
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime())
                .ip(ip)
                .build()
            ).flatMap(data -> Mono.just(TokenDetails.builder()
                .userId(data.getUserId())
                .token(tokenData.getToken())
                .xToken(data.getAuthEntity())
                .issuedAt(tokenData.getIssuedAt())
                .expiresAt(tokenData.getExpiresAt())
                .build())
            );
    }

    public Mono<UserEntity> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Mono<UserEntity> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
