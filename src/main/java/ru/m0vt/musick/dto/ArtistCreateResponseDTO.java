package ru.m0vt.musick.dto;

import ru.m0vt.musick.model.Artist;

/**
 * DTO для ответа после создания профиля артиста с обновленным токеном
 */
public class ArtistCreateResponseDTO {
    private Artist artist;
    private String token;
    private String username;
    private String role;
    
    // Конструктор по умолчанию
    public ArtistCreateResponseDTO() {
    }
    
    // Конструктор с параметрами
    public ArtistCreateResponseDTO(Artist artist, String token, String username, String role) {
        this.artist = artist;
        this.token = token;
        this.username = username;
        this.role = role;
    }
    
    // Геттеры и сеттеры
    public Artist getArtist() {
        return artist;
    }
    
    public void setArtist(Artist artist) {
        this.artist = artist;
    }
    
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