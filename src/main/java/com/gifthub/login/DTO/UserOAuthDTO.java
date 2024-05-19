package com.gifthub.login.DTO;

import lombok.Builder;
import lombok.Data;

@Data
public class UserOAuthDTO {
    private String identifier;
    private String name;

    @Builder
    public UserOAuthDTO(String identifier, String name) {
        this.identifier = identifier;
        this.name = name;
    }
}
