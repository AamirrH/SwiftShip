package com.code.prodapp.authservice.services;

import com.code.prodapp.authservice.DTOs.LoginResponseDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;

    @Value("${oauth2.success-redirect-url:http://localhost:3000/oauth/success}")
    private String successRedirectUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub");
        log.info("OAuth2 login success from provider uri={} email={} name={} providerIdPresent={}",
                request.getRequestURI(),
                email,
                name,
                providerId != null && !providerId.isBlank());

        LoginResponseDTO loginResponseDTO = authService.oauthLogin(email, name, providerId);
        log.info("OAuth2 login mapped to SwiftShip user username={} redirectBase={}",
                loginResponseDTO.getUsername(),
                successRedirectUrl);

        Cookie refreshTokenCookie = new Cookie("RefreshToken", loginResponseDTO.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        response.addCookie(refreshTokenCookie);

        String redirectUrl = UriComponentsBuilder.fromUriString(successRedirectUrl)
                .queryParam("accessToken", loginResponseDTO.getAccessToken())
                .queryParam("username", loginResponseDTO.getUsername())
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();

        log.info("OAuth2 login redirecting browser to frontend success url");
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
