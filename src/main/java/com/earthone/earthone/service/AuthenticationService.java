package com.earthone.earthone.service;

// import java.time.ZoneId;

// import org.springframework.http.server.reactive.ServerHttpRequest;
// import org.springframework.stereotype.Service;

// import com.earthone.earthone.dto.AuthResponseDto;
// import com.earthone.earthone.entity.AuthenticationDataEntity;
// import com.earthone.earthone.exception.AuthException;
// import com.earthone.earthone.repository.AuthenticationRepository;
// import com.earthone.earthone.security.SecurityService;
// import com.earthone.earthone.security.TokenDetails;

// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import reactor.core.publisher.Mono;

// @Slf4j
// @Service
// @RequiredArgsConstructor
public class AuthenticationService {

    // private final AuthenticationRepository authenticationRepository;
    // //private final SecurityService securityService;

    // public Mono<AuthenticationDataEntity> saveAuthData(TokenDetails tokenData, ServerHttpRequest request) {
    //     @SuppressWarnings("null")
    //     String ip = request.getRemoteAddress().getAddress().getHostAddress();
    //     if(ip == null) {
    //         ip = "unknown";
    //     }
    //     log.info("user was logedin with id - {}, and ip - {}", tokenData.getUserId(), ip);
    //     return authenticationRepository.save(AuthenticationDataEntity.builder()
    //         .userId(tokenData.getUserId())
    //         .authEntity(tokenData.getXToken())
    //         .ip(ip)
    //         .expiresAt(tokenData.getXTokenExpiresAt().toInstant()
    //             .atZone(ZoneId.systemDefault())
    //             .toLocalDateTime())
    //         .build());
    // }

    // public Mono<AuthResponseDto> xTokenCheck(AuthenticationDataEntity tokenData, ServerHttpRequest request) {
    //     TokenDetails newTokenData = new TokenDetails();
    //     authenticationRepository.findByAuthEntity(tokenData.getAuthEntity()).flatMap(token -> {
    //         // if(!securityService.checkXToken(token.getAuthEntity(), newTokenData)) {
    //         //     authenticationRepository.delete(token);
    //         //     return Mono.error(new AuthException("xtoken expired", "0001"));
    //         // }
    //         return Mono.just(token);
    //     }).switchIfEmpty(Mono.error(new AuthException("Invalid token", "EARTHONE_INVALID_TOKEN"))).block();
    //     saveAuthData(newTokenData, request);
    //     return Mono.just(new AuthResponseDto().toBuilder()
    //                 .userId(newTokenData.getUserId())
    //                 .token(newTokenData.getToken())
    //                 .xToken(newTokenData.getXToken())
    //                 .issuedAt(newTokenData.getIssuedAt())
    //                 .expiresAt(newTokenData.getXTokenExpiresAt())
    //                 .build());
    // }
    
}
