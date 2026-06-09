package com.code.prodapp.authservice.controllers;

import com.code.prodapp.authservice.DTOs.LoginRequestDTO;
import com.code.prodapp.authservice.DTOs.LoginResponseDTO;
import com.code.prodapp.authservice.DTOs.SignupRequestDTO;
import com.code.prodapp.authservice.DTOs.SignupResponseDTO;
import com.code.prodapp.authservice.services.AuthService;
import com.code.prodapp.authservice.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthControllers {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDTO> signup(@RequestBody SignupRequestDTO signupRequestDTO) {
        return ResponseEntity.ok(authService.signup(signupRequestDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
       LoginResponseDTO loginResponseDTO = authService.login(loginRequestDTO);
       return ResponseEntity.ok(loginResponseDTO);
    }




}

