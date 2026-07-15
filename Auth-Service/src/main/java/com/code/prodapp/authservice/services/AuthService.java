package com.code.prodapp.authservice.services;


import com.code.prodapp.authservice.DTOs.LoginRequestDTO;
import com.code.prodapp.authservice.DTOs.LoginResponseDTO;
import com.code.prodapp.authservice.DTOs.SignupRequestDTO;
import com.code.prodapp.authservice.DTOs.SignupResponseDTO;
import com.code.prodapp.authservice.entities.UserEntity;
import com.code.prodapp.authservice.entities.UserRole;
import com.code.prodapp.authservice.exceptions.TokenException;
import com.code.prodapp.authservice.exceptions.UserAlreadyExistsException;
import com.code.prodapp.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {


    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final UserRepository userRepository;

    public SignupResponseDTO signup(SignupRequestDTO signupRequestDTO) {

        String username = normalizeUsername(signupRequestDTO.getUsername());
        String email = normalizeEmail(signupRequestDTO.getEmail());

        if (!hasText(username)) {
            throw new IllegalArgumentException("Username is required");
        }
        if (!hasText(email)) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!hasText(signupRequestDTO.getPassword())) {
            throw new IllegalArgumentException("Password is required");
        }

        UserEntity existingEmailUser = userRepository.findByEmail(email).orElse(null);

        if(userRepository.existsByUsername(username)
                && (existingEmailUser == null || !username.equals(existingEmailUser.getUsername()))) {
            throw new UserAlreadyExistsException("This username is already taken");
        }

        if(existingEmailUser != null && !"GOOGLE".equalsIgnoreCase(existingEmailUser.getAuthProvider())) {
            throw new UserAlreadyExistsException("This email is already linked to an account");
        }

        String hashedPassword = bCryptPasswordEncoder.encode(signupRequestDTO.getPassword());
        UserEntity user = existingEmailUser != null ? existingEmailUser : new UserEntity();
        user.setUsername(username);
        user.setPassword(hashedPassword);
        user.setEmail(email);
        user.setAuthProvider(existingEmailUser != null ? "PASSWORD_GOOGLE" : null);
        user.setRole(resolveSignupRole(signupRequestDTO.getRole()));
        UserEntity savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, SignupResponseDTO.class);

    }

    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        // Takes an Unauthenticated Object and returns an Authenticated Object
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken
                        (loginRequestDTO.getUsername(),loginRequestDTO.getPassword())
        );
        UserEntity user = (UserEntity) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new LoginResponseDTO(user.getUsername(), accessToken, refreshToken);
    }

    public LoginResponseDTO refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new TokenException("Refresh token cookie is missing, You might need to login again");
        }

        UserEntity user;
        try {
            Long userId = jwtService.getUserIdFromToken(refreshToken);
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new TokenException("The Refresh Token is invalid, You might need to login again"));
        } catch (Exception e) {
            throw new TokenException("The Refresh Token is invalid, You might need to login again");
        }

        String newAccessToken = jwtService.generateAccessToken(user);
        return new LoginResponseDTO(user.getUsername(), newAccessToken, refreshToken);
    }

    public LoginResponseDTO oauthLogin(String email, String name, String providerId) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseGet(() -> createOauthUser(email, name, providerId));

        user.setAuthProvider("GOOGLE");
        user.setProviderId(providerId);
        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new LoginResponseDTO(user.getUsername(), accessToken, refreshToken);
    }

    private UserEntity createOauthUser(String email, String name, String providerId) {
        String username = buildOauthUsername(email, name, providerId);

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(bCryptPasswordEncoder.encode(UUID.randomUUID().toString()));
        user.setAuthProvider("GOOGLE");
        user.setProviderId(providerId);
        user.setRole(UserRole.CUSTOMER);
        return userRepository.save(user);
    }

    private UserRole resolveSignupRole(String requestedRole) {
        if (requestedRole == null || requestedRole.isBlank()) {
            return UserRole.CUSTOMER;
        }
        return UserRole.valueOf(requestedRole.toUpperCase());
    }

    private String buildOauthUsername(String email, String name, String providerId) {
        String baseUsername = email != null && email.contains("@")
                ? email.substring(0, email.indexOf("@"))
                : name;

        if (baseUsername == null || baseUsername.isBlank()) {
            baseUsername = "google_user";
        }

        String username = baseUsername.replaceAll("[^a-zA-Z0-9_]", "_");
        if (!userRepository.existsByUsername(username)) {
            return username;
        }

        String suffix = providerId == null || providerId.length() < 6
                ? UUID.randomUUID().toString().substring(0, 8)
                : providerId.substring(providerId.length() - 6);
        return username + "_" + suffix;
    }

    private String normalizeUsername(String username) {
        return username == null ? null : username.trim();
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

}
