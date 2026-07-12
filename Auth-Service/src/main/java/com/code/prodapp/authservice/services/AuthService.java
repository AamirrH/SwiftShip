package com.code.prodapp.authservice.services;


import com.code.prodapp.authservice.DTOs.LoginRequestDTO;
import com.code.prodapp.authservice.DTOs.LoginResponseDTO;
import com.code.prodapp.authservice.DTOs.SignupRequestDTO;
import com.code.prodapp.authservice.DTOs.SignupResponseDTO;
import com.code.prodapp.authservice.entities.UserEntity;
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

        // Username is unique
        String username = signupRequestDTO.getUsername();
        if(userRepository.existsByUsername(username) || userRepository.existsByEmail(signupRequestDTO.getEmail())) {
            throw new UserAlreadyExistsException("User with username " + username + " already exists");
        }
        String hashedPassword = bCryptPasswordEncoder.encode(signupRequestDTO.getPassword());
        String email = signupRequestDTO.getEmail();
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword(hashedPassword);
        user.setEmail(email);
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
        return userRepository.save(user);
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

}
