package ru.m0vt.musick.dto;

/**
 * DTO для ответа на аутентификацию пользователя
 */
public class AuthResponseDTO {
    
    private String token;
    private String username;
    private String role;

    // Constructors
    public AuthResponseDTO() {
    }

    public AuthResponseDTO(String token, String username, String role) {
        this.token = token;
        this.username = username;
        this.role = role;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}