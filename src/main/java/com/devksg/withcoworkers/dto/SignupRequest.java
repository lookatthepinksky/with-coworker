package com.devksg.withcoworker.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    private String loginId;
    private String name;
    private String email;
    private String password;
}
