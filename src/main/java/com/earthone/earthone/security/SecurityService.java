package com.earthone.earthone.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import com.earthone.earthone.entity.UserEntity;
import com.earthone.earthone.exception.AuthException;
import com.earthone.earthone.service.AuthenticationService;
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
    //private final AuthenticationService authenticationService;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private Integer expirationInSeconds;
    @Value("${jwt.issuer}")
    private String issuer;


    private TokenDetails generateToken(UserEntity user) {
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

        return generateToken(expirationDate, claims, subject);
    }

    private TokenDetails generateToken(Date expirationDate, Map<String, Object> claims, String subject) {
        Date createdDate = new Date();
        return TokenDetails.builder()
                .token(getToken(expirationDate, claims, subject, createdDate))
                .xToken(getToken(new Date(expirationDate.getTime()*100L), claims, subject, createdDate))
                .issuedAt(createdDate)
                .expiresAt(expirationDate)
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

                    TokenDetails authData = generateToken(user).toBuilder()
                        .userId(user.getId())
                        .build();
                    
                    //authenticationService.saveAuthData(authData, request);

                    return Mono.just(authData);
                })
                .switchIfEmpty(Mono.error(new AuthException("Invalid username", "EARTHONE_INVALID_USERNAME")));
    }

    // public boolean checkXToken(String xToken, TokenDetails newTokenData) {
    //     Claims claims = Jwts.parser()
    //             .setSigningKey(Base64.getEncoder().encodeToString(secret.getBytes()))
    //             .parseClaimsJws(xToken)
    //             .getBody();

    //     final Date expirationDate = claims.getExpiration();

    //     if (expirationDate.before(new Date())) {
    //         return false;
    //     }

    //     newTokenData = generateToken(userService.getUserById(Long.parseLong(claims.get("userId").toString())).block());
        
    //     return true;
    // }

}
