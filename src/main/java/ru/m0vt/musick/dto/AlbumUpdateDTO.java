package ru.m0vt.musick.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO для обновления информации об альбоме
 */
public class AlbumUpdateDTO {
    
    @NotBlank(message = "Название альбома не может быть пустым")
    @Size(max = 255, message = "Название альбома не может превышать 255 символов")
    private String title;
    
    @NotNull(message = "URL обложки не может быть пустым")
    private String coverUrl;
    
    @NotNull(message = "Цена не может быть пустой")
    @Positive(message = "Цена должна быть положительной")
    private BigDecimal price;
    
    @NotNull(message = "Дата выпуска не может быть пустой")
    private LocalDate releaseDate;
    
    // Конструктор по умолчанию
    public AlbumUpdateDTO() {
    }
    
    // Геттеры и сеттеры
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getCoverUrl() {
        return coverUrl;
    }
    
    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public LocalDate getReleaseDate() {
        return releaseDate;
    }
    
    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }
}