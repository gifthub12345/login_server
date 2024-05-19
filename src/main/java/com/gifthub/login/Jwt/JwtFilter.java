package com.gifthub.login.Jwt;

import com.gifthub.login.DTO.CustomOAuth2User;
import com.gifthub.login.DTO.UserOAuthDTO;
import com.gifthub.login.Entity.UserEntity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String token = request.getHeader("Authorization");

        if (ObjectUtils.isEmpty(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (jwtTokenProvider.isTokenExpired(token)) {
            String reissueAccessToken = jwtTokenProvider.reissueAccessToken(token);
            setAuthentication(reissueAccessToken);
            response.setHeader("Authorization", reissueAccessToken);
        }
        else {
            setAuthentication(token);
        }
        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String accessToken) {
        String identifier = jwtTokenProvider.getIdentifierFromToken(accessToken);
        UserOAuthDTO userOAuthDTO = UserOAuthDTO.builder()
                .identifier(identifier)
                .build();
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userOAuthDTO);
        Authentication auth = new UsernamePasswordAuthenticationToken(customOAuth2User, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

    }

}
