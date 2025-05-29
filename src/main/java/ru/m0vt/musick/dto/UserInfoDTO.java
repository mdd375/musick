package ru.m0vt.musick.dto;

/**
 * DTO для представления информации о пользователе
 */
public class UserInfoDTO {
    private Long id;
    private String username;
    private String email;
    private java.math.BigDecimal balance;
    private boolean isArtist;
    
    // Конструктор по умолчанию
    public UserInfoDTO() {
    }
    
    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public java.math.BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(java.math.BigDecimal balance) {
        this.balance = balance;
    }
    
    public boolean isArtist() {
        return isArtist;
    }
    
    public void setArtist(boolean isArtist) {
        this.isArtist = isArtist;
    }
}