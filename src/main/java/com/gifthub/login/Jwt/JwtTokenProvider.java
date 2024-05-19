package com.gifthub.login.Jwt;

import com.gifthub.login.DAO.RedisDAO;
import com.gifthub.login.Repository.TokenRepository;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${spring.jwt.secret}")
    private String secret;
    private SecretKey secretKey;
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60L;
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60L * 24 * 30 * 4;
    private final TokenRepository tokenRepository;

    private final RedisDAO redisDAO;

    @PostConstruct
    protected void init() {
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public String createAccessToken(String identifier) {
        return Jwts.builder()
                .claim("identifier", identifier)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE_TIME))
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(String identifier){
        String refreshToken = Jwts.builder()
                .claim("identifier", identifier)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(secretKey)
                .compact();

//        Token refreshtoken = new Token(identifier, refreshToken, REFRESH_TOKEN_EXPIRE_TIME);
//        tokenRepository.save(refreshtoken);
        redisDAO.setValues(identifier, refreshToken, Duration.ofMillis(REFRESH_TOKEN_EXPIRE_TIME));

        return refreshToken;
    }

    public String getIdentifierFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("identifier", String.class);
    }

    // 토큰의 만료 시간이 현재 이전 -> true, 현재 이후 -> false
    public Boolean isTokenExpired(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }

    public String reissueAccessToken(String accessToken) {
        if (StringUtils.hasText(accessToken)) {
            Object value = redisDAO.getValues(accessToken);
            if (value instanceof Token) {
                String refreshToken = ((Token) value).getRefreshToken();
                if (isTokenExpired(refreshToken)) {
                    String identifier = getIdentifierFromToken(refreshToken);
                    String reissueAccessToken = createAccessToken(identifier);
                    Token entityToken = new Token(identifier, reissueAccessToken, REFRESH_TOKEN_EXPIRE_TIME);
                    tokenRepository.save(entityToken);

                    return reissueAccessToken;
                }
            }
        }
        return null;
    }



}
