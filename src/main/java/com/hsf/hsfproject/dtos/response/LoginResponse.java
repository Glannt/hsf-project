package com.hsf.hsfproject.dtos.response;

import com.hsf.hsfproject.model.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginResponse {
    User user;
    String token;
}
