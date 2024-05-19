package com.gifthub.login.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gifthub.login.DTO.*;
import com.gifthub.login.Entity.UserEntity;
import com.gifthub.login.Jwt.JwtTokenProvider;
import com.gifthub.login.Jwt.Token;
import com.gifthub.login.Repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class OAuth2Service extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes;
        String userNameAttributeName = userRequest
                .getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        if (registrationId.contains("google")) {
            OAuth2User oAuth2User = delegate.loadUser(userRequest);
            attributes = oAuth2User.getAttributes();
        }
        else if (registrationId.contains("apple")) {
            String idToken = userRequest.getAdditionalParameters().get("id_token").toString();
            attributes = decodeJwtTokenPayload(idToken);
            attributes.put("id_token", idToken);
        }
        else {
            return null;
        }

        OAuthAttributes authAttributes = OAuthAttributes.of(registrationId, userNameAttributeName, attributes);
        String identifier = authAttributes.getIdentifier();
        UserEntity existUser = userRepository.findByIdentifier(identifier);


        if (existUser == null) {
            UserEntity newUser = authAttributes.toEntity();
            userRepository.save(newUser);
        }
        else {
            existUser.update(authAttributes.getName(), authAttributes.getEmail());
            userRepository.save(existUser);
        }

        UserOAuthDTO userOAuthDTO = UserOAuthDTO.builder()
                .identifier(identifier)
                .name(authAttributes.getName())
                .build();

        return new CustomOAuth2User(userOAuthDTO);

//        return new DefaultOAuth2User(
//                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
//                authAttributes.getAttributes(),
//                authAttributes.getNameAttributeKey());

    }

    public Map<String, Object> decodeJwtTokenPayload(String jwtToken) {
        Map<String, Object> jwtClaims = new HashMap<>();
        try {
            String[] parts = jwtToken.split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();

            byte[] decodedBytes = decoder.decode(parts[1].getBytes(StandardCharsets.UTF_8));
            String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> map = mapper.readValue(decodedString, Map.class);
            jwtClaims.putAll(map);

        } catch (JsonProcessingException e) {
//        logger.error("decodeJwtToken: {}-{} / jwtToken : {}", e.getMessage(), e.getCause(), jwtToken);
        }
        return jwtClaims;
    }

}
