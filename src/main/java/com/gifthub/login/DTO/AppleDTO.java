package com.gifthub.login.DTO;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "apple")
public class AppleDTO {
    private String keyId;
    private String clientId;
    private String teamId;
}
