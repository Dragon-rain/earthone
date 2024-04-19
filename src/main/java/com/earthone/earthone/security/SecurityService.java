package com.earthone.earthone.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import com.earthone.earthone.entity.UserEntity;
import com.earthone.earthone.exception.AuthException;
import com.earthone.earthone.service.UserService;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SecurityService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private Integer expirationInSeconds;
    @Value("${jwt.issuer}")
    private String issuer;


    public TokenDetails generateToken(UserEntity user) {
        Map<String, Object> claims = new HashMap<>() {{
            put("role", user.getRole());
            put("userId", user.getId());
            put("username", user.getUsername());
        }};
        return generateToken(claims, user.getId().toString());
    }

    private TokenDetails generateToken(Map<String, Object> claims, String subject) {
        //Long expirationTimeInMillis = expirationInSeconds * 1000L;
        Long expirationTimeInMillis = Long.valueOf(expirationInSeconds);
        Date expirationDate = new Date(new Date().getTime() + expirationTimeInMillis);
        Date xExpirationDate = new Date(new Date().getTime() + expirationTimeInMillis*100L);

        return generateToken(expirationDate, xExpirationDate, claims, subject);
    }

    private TokenDetails generateToken(Date expirationDate, Date xExpirationDate, Map<String, Object> claims, String subject) {
        Date createdDate = new Date();
        return TokenDetails.builder()
                .userId(Long.valueOf(subject))
                .token(getToken(expirationDate, claims, subject, createdDate))
                .xToken(getToken(xExpirationDate, claims, subject, createdDate))
                .issuedAt(createdDate)
                .expiresAt(expirationDate)
                .xTokenExpiresAt(xExpirationDate)
                .build();
    }

    private String getToken(Date expirationDate, Map<String, Object> claims, String subject, Date createdDate) {
        return Jwts.builder()
            .setClaims(claims)
            .setIssuer(issuer)
            .setSubject(subject)
            .setIssuedAt(createdDate)
            .setId(UUID.randomUUID().toString())
            .setExpiration(expirationDate)
            .signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString(secret.getBytes()))
            .compact();
    }

    public Mono<TokenDetails> authenticate(String username, String password, ServerHttpRequest request) {
        return userService.getUserByUsername(username)
                .flatMap(user -> {
                    if (!user.isEnabled()) {
                        return Mono.error(new AuthException("Account disabled", "EARTHONE_USER_ACCOUNT_DISABLED"));
                    }

                    if (!passwordEncoder.matches(password, user.getPassword())) {
                        return Mono.error(new AuthException("Invalid password", "EARTHONE_INVALID_PASSWORD"));
                    }
                    return Mono.just(generateToken(user).toBuilder()
                                .userId(user.getId())
                                .build());
                })
                .flatMap(tokenData -> userService.saveAuthData(tokenData, request))
                .switchIfEmpty(Mono.error(new AuthException("Invalid username", "EARTHONE_INVALID_USERNAME")));
    }

}
