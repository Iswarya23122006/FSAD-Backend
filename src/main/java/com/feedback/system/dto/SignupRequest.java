package com.feedback.system.dto;

import com.feedback.system.entity.User;
import lombok.Data;

@Data
public class SignupRequest {
    private String name;
    private String email;
    private String password;
    private User.Role role;
}
