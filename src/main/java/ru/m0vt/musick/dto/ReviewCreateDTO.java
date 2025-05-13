package ru.m0vt.musick.dto;

/**
 * DTO for creating a new review
 */
public class ReviewCreateDTO {
    private Long userId;
    private String text;
    private String favoriteTracks;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFavoriteTracks() {
        return favoriteTracks;
    }

    public void setFavoriteTracks(String favoriteTracks) {
        this.favoriteTracks = favoriteTracks;
    }
}