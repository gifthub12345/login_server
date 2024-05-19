package com.gifthub.login.DTO;

import com.gifthub.login.Entity.UserEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class OAuthAttributes {

    private Map<String, Object> attributes;
    private String name;
    private String email;
    private String identifier;
    private String nameAttributeKey;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes,
                           String nameAttributeKey, String name,
                           String email, String identifier) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.identifier = identifier;
    }

    public static OAuthAttributes of(String registrationId, String userNameAttributeName,
                                     Map<String, Object> attributes) {

        if (registrationId.contains("google")) {
            return ofGoogle(userNameAttributeName, attributes);
        }
        else if (registrationId.contains("apple")) {
            return ofApple(userNameAttributeName, attributes);
        }
        else {
            return null;
        }
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .identifier("google" + attributes.get("sub").toString())
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    private static OAuthAttributes ofApple(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .name("tempName")
                .email(attributes.get("email").toString())
                .identifier("apple" + attributes.get("sub").toString())
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    public UserEntity toEntity() {
        return UserEntity.builder()
                .identifier(identifier)
                .name(name)
                .email(email)
                .build();
    }
}
