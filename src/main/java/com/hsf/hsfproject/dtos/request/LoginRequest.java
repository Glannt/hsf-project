package com.hsf.hsfproject.dtos.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginRequest {
    private String username;
    private String password;

    // You can add additional fields if needed, such as rememberMe, etc.
}
