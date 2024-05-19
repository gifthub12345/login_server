package com.gifthub.login.Config;

import com.gifthub.login.DTO.CustomOAuth2User;
import com.gifthub.login.Jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private static final String URI = "/main";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String identifier = customOAuth2User.getIdentifier();

        String accessToken = jwtTokenProvider.createAccessToken(identifier);
        String refreshToken = jwtTokenProvider.createRefreshToken(identifier);

        response.setHeader("Authorization", accessToken);
        response.setHeader("RefreshToken", refreshToken);

        response.sendRedirect(URI);
    }
}
