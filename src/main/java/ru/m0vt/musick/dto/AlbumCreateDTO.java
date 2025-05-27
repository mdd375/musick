package ru.m0vt.musick.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating a new album
 */
public class AlbumCreateDTO {
    private String title;
    private String coverUrl;
    private BigDecimal price;
    private LocalDate releaseDate;


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