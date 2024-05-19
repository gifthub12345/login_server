package com.gifthub.login.Util;

import com.gifthub.login.DTO.AppleDTO;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class CustomRequestEntityConverter implements Converter<OAuth2AuthorizationCodeGrantRequest, RequestEntity<?>> {

    private OAuth2AuthorizationCodeGrantRequestEntityConverter defaultConverter;

    public CustomRequestEntityConverter() {
        defaultConverter = new OAuth2AuthorizationCodeGrantRequestEntityConverter();
    }

    @Autowired
    private AppleDTO appleDTO;

    private final String APPLE_URL = "https://appleid.apple.com";


    @Override
    public RequestEntity<?> convert(OAuth2AuthorizationCodeGrantRequest req) {
        RequestEntity<?> entity = defaultConverter.convert(req);
        String registrationId = req.getClientRegistration().getRegistrationId();
        MultiValueMap<String, String> params = (MultiValueMap<String,String>) entity.getBody();
        if (registrationId.contains("apple")) {
            try {
                params.set("client_secret", createClientSecret(appleDTO.getKeyId(), appleDTO.getClientId(), appleDTO.getTeamId()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return new RequestEntity<>(params, entity.getHeaders(),
                entity.getMethod(), entity.getUrl());
    }

    public PrivateKey getPrivateKey(String KeyId) throws IOException {
        String APPLE_KEY_PATH = "/home/ec2-user/AuthKey_" + KeyId + ".p8";
        File file = new File(APPLE_KEY_PATH);

        InputStreamReader in = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
        PEMParser pemParser = new PEMParser(in);
        PrivateKeyInfo object = (PrivateKeyInfo) pemParser.readObject();
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
        return converter.getPrivateKey(object);
    }

    public String createClientSecret(String KeyId, String ClientId, String TeamId) throws IOException {
        Date expirationDate = Date.from(LocalDateTime.now().plusDays(30).atZone(ZoneId.systemDefault()).toInstant());
        Map<String, Object> jwtHeader = new HashMap<>();
        jwtHeader.put("kid", KeyId);
        jwtHeader.put("alg", "ES256");

        return Jwts.builder()
                .setHeaderParams(jwtHeader)
                .setIssuer(TeamId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expirationDate)
                .setAudience(APPLE_URL)
                .setSubject(ClientId)
                .signWith(SignatureAlgorithm.ES256, getPrivateKey(KeyId))
                .compact();
    }

}
