package com.earthone.earthone.rest;

import lombok.RequiredArgsConstructor;
import com.earthone.earthone.dto.AuthRequestDto;
import com.earthone.earthone.dto.AuthResponseDto;
import com.earthone.earthone.dto.UserDto;
//import com.earthone.earthone.entity.AuthenticationDataEntity;
import com.earthone.earthone.entity.UserEntity;
import com.earthone.earthone.mapper.UserMapper;
import com.earthone.earthone.security.CustomPrincipal;
import com.earthone.earthone.security.SecurityService;
//import com.earthone.earthone.service.AuthenticationService;
import com.earthone.earthone.service.UserService;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthRestControllerV1 {

    private final SecurityService securityService;
    private final UserService userService;
    //private final AuthenticationService authenticationService;
    private final UserMapper userMapper;


    @PostMapping("/register")
    public Mono<UserDto> register(@RequestBody UserDto dto) {
        UserEntity entity = userMapper.map(dto);
        return userService.registerUser(entity)
                .map(userMapper::map);
    }

    // @PostMapping("/processeXToken")
    // public Mono<AuthResponseDto> processedXToken(String xToken, ServerHttpRequest request) {
    //     return authenticationService.xTokenCheck(AuthenticationDataEntity.builder()
    //                 .authEntity(xToken)
    //                 .build(), request);
    // }

    @PostMapping("/login")
    public Mono<AuthResponseDto> login(@RequestBody AuthRequestDto dto, ServerHttpRequest request) {
        
        return securityService.authenticate(dto.getUsername(), dto.getPassword(), request)
                .flatMap(tokenDetails -> Mono.just(
                        AuthResponseDto.builder()
                                .userId(tokenDetails.getUserId())
                                .token(tokenDetails.getToken())
                                .issuedAt(tokenDetails.getIssuedAt())
                                .expiresAt(tokenDetails.getExpiresAt())
                                .build()
                ));
    }

    @GetMapping("/info")
    public Mono<UserDto> getUserInfo(Authentication authentication) {
        CustomPrincipal customPrincipal = (CustomPrincipal) authentication.getPrincipal();

        return userService.getUserById(customPrincipal.getId())
                .map(userMapper::map);
    }
}
