package com.feedback.system.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
