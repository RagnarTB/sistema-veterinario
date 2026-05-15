package com.veterinaria.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthResponseDTO {
    private String token;
    private String refreshToken;
    private String email;
    private List<String> roles; // ROLE_ADMIN, ROLE_VETERINARIO, etc.
    private List<Long> sedeIds; // Sedes asignadas al empleado
    private boolean requiresRoleSelection;

    public AuthResponseDTO(String token, String refreshToken, String email, List<String> roles) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.email = email;
        this.roles = roles;
        this.requiresRoleSelection = false;
    }

    public AuthResponseDTO(String token, String refreshToken, String email, List<String> roles, List<Long> sedeIds) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.email = email;
        this.roles = roles;
        this.sedeIds = sedeIds;
        this.requiresRoleSelection = false;
    }

    public AuthResponseDTO(String token, String refreshToken, String email, List<String> roles, boolean requiresRoleSelection) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.email = email;
        this.roles = roles;
        this.requiresRoleSelection = requiresRoleSelection;
    }

    public AuthResponseDTO(String token, String refreshToken, String email, List<String> roles, List<Long> sedeIds, boolean requiresRoleSelection) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.email = email;
        this.roles = roles;
        this.sedeIds = sedeIds;
        this.requiresRoleSelection = requiresRoleSelection;
    }
}