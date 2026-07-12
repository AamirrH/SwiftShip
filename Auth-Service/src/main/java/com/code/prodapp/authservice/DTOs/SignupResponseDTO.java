package com.code.prodapp.authservice.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponseDTO {

    private Long userId;
    private String username;
    private String email;
    private String role;
    private String message;
}
