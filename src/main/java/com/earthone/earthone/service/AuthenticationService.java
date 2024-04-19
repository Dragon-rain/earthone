package com.earthone.earthone.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.earthone.earthone.dto.AuthResponseDto;
import com.earthone.earthone.entity.AuthenticationDataEntity;
import com.earthone.earthone.exception.AuthException;
import com.earthone.earthone.repository.AuthenticationRepository;
import com.earthone.earthone.security.SecurityService;
import com.earthone.earthone.security.TokenDetails;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;
    private final AuthenticationRepository authenticationRepository;
    private final SecurityService securityService;

    @Value("${jwt.secret}")
    private String secret;

    public Mono<AuthResponseDto> xTokenCheck(AuthenticationDataEntity tokenData, ServerHttpRequest request) {
        return authenticationRepository.findByAuthEntity(tokenData.getAuthEntity())
        .flatMap(token -> checkXToken(token))
        .flatMap(newTokenData -> userService.saveAuthData(newTokenData, request))
        .flatMap(newTokenData -> Mono.just(AuthResponseDto.builder()
            .userId(newTokenData.getUserId())
            .token(newTokenData.getToken())
            .xToken(newTokenData.getXToken())
            .issuedAt(newTokenData.getIssuedAt())
            .build()
        ));
    }

    public Mono<TokenDetails> checkXToken(AuthenticationDataEntity token) {
        
        return userService.getUserById(token.getUserId())
                    .flatMap(user -> {
                        Claims claims = Jwts.parser()
                                .setSigningKey(Base64.getEncoder().encodeToString(secret.getBytes()))
                                .parseClaimsJws(token.getAuthEntity())
                                .getBody();

                        final Date expirationDate = claims.getExpiration();
                        if (expirationDate.before(new Date())) {
                            return Mono.error(new AuthException("xtoken expired", "0001"));
                        }
                        return authenticationRepository.delete(token).then(Mono.just(securityService.generateToken(user)));
                    });
    }
    
}
