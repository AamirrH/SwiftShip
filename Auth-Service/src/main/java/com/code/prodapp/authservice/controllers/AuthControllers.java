package com.code.prodapp.authservice.controllers;

import com.code.prodapp.authservice.DTOs.LoginRequestDTO;
import com.code.prodapp.authservice.DTOs.LoginResponseDTO;
import com.code.prodapp.authservice.DTOs.SignupRequestDTO;
import com.code.prodapp.authservice.DTOs.SignupResponseDTO;
import com.code.prodapp.authservice.exceptions.TokenException;
import com.code.prodapp.authservice.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthControllers {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDTO> signup(@RequestBody SignupRequestDTO signupRequestDTO) {
        log.info("Auth signup request username={} email={} role={}",
                signupRequestDTO.getUsername(),
                signupRequestDTO.getEmail(),
                signupRequestDTO.getRole());
        SignupResponseDTO signupResponseDTO = authService.signup(signupRequestDTO);
        log.info("Auth signup completed username={} email={}",
                signupRequestDTO.getUsername(),
                signupRequestDTO.getEmail());
        return ResponseEntity.ok(signupResponseDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO,
                                                  HttpServletResponse response) {
       log.info("Auth login request username={}", loginRequestDTO.getUsername());
       LoginResponseDTO loginResponseDTO = authService.login(loginRequestDTO);
       log.info("Auth login completed username={} responseUsername={}",
               loginRequestDTO.getUsername(),
               loginResponseDTO.getUsername());
       // Creating a Cookie
        Cookie cookie = new Cookie("RefreshToken", loginResponseDTO.getRefreshToken());
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        // Spring Merges the Explicit Response with the cookie and ResponseEntity Body and returns a single response.
       // Every subsequent request will contain this cookie.
       return ResponseEntity.ok(loginResponseDTO);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(HttpServletRequest request) {
        log.info("Auth refresh request received");
        String refreshToken = null;
        Cookie[] requestCookies = request.getCookies();
        if (requestCookies == null) {
            throw new TokenException("Refresh token cookie is missing, You might need to login again");
        }

        for (Cookie requestCookie : requestCookies) {
            if (requestCookie.getName().equals("RefreshToken")) {
                refreshToken = requestCookie.getValue();
                break;
            }
        }

        LoginResponseDTO loginResponseDTO = authService.refreshAccessToken(refreshToken);
        log.info("Auth refresh completed username={}", loginResponseDTO.getUsername());
        return ResponseEntity.ok(loginResponseDTO);

    }




}
