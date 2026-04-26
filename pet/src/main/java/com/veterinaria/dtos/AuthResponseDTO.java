package com.veterinaria.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {
    private String token;
    private String refreshToken;
    private String email;
    private List<String> roles; // ROLE_ADMIN, ROLE_VETERINARIO, etc.
}