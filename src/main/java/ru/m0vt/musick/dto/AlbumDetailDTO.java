package ru.m0vt.musick.dto;

import java.time.LocalDate;
import java.util.List;
import ru.m0vt.musick.model.Tag;
import ru.m0vt.musick.model.Track;

/**
 * DTO для представления детальной информации об альбоме
 */
public class AlbumDetailDTO {
    private Long id;
    private String title;
    private String artistName;
    private Long artistId;
    private String coverUrl;
    private LocalDate releaseDate;
    private java.math.BigDecimal price;
    private List<Track> tracks;
    private List<Tag> tags;
    
    // Конструктор по умолчанию
    public AlbumDetailDTO() {
    }
    
    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getArtistName() {
        return artistName;
    }
    
    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }
    
    public Long getArtistId() {
        return artistId;
    }
    
    public void setArtistId(Long artistId) {
        this.artistId = artistId;
    }
    
    public String getCoverUrl() {
        return coverUrl;
    }
    
    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }
    
    public LocalDate getReleaseDate() {
        return releaseDate;
    }
    
    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }
    
    public java.math.BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(java.math.BigDecimal price) {
        this.price = price;
    }
    
    public List<Track> getTracks() {
        return tracks;
    }
    
    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
    }
    
    public List<Tag> getTags() {
        return tags;
    }
    
    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
}