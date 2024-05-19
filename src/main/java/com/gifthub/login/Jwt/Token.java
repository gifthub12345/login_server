package com.gifthub.login.Jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.UUID;

@RedisHash("token")
@ToString
@AllArgsConstructor
@Getter
@NoArgsConstructor
public class Token {

    @Id
    private String token_id;

    private String refreshToken;

    @TimeToLive
    private Long expiration;


}
