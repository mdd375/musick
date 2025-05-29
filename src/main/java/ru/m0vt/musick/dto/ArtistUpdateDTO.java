package ru.m0vt.musick.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating artist profile information
 */
public class ArtistUpdateDTO {
    @NotBlank(message = "Artist name cannot be blank")
    @Size(max = 100, message = "Artist name must be at most 100 characters")
    private String name;

    @Size(max = 2000, message = "Biography must be at most 2000 characters")
    private String bio;

    private String photoUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}