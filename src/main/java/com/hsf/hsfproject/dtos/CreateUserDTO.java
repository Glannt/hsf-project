package com.hsf.hsfproject.dtos;


import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserDTO {
    private String username;
    private String password;
    private String email;
    private String phoneNumber;
}
