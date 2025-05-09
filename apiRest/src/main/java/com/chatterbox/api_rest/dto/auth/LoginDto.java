package com.chatterbox.api_rest.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class LoginDto {
    private String email;
    private String password;
}