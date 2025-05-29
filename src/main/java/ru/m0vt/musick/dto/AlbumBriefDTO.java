package ru.m0vt.musick.dto;

/**
 * DTO для представления краткой информации об альбоме
 */
public class AlbumBriefDTO {
    private Long id;
    private String title;
    private String artistName;
    private Long artistId;
    private Integer trackCount;
    private Integer totalDurationSec;
    private String coverUrl;
    
    // Конструктор по умолчанию
    public AlbumBriefDTO() {
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
    
    public Integer getTrackCount() {
        return trackCount;
    }
    
    public void setTrackCount(Integer trackCount) {
        this.trackCount = trackCount;
    }
    
    public Integer getTotalDurationSec() {
        return totalDurationSec;
    }
    
    public void setTotalDurationSec(Integer totalDurationSec) {
        this.totalDurationSec = totalDurationSec;
    }
    
    public String getCoverUrl() {
        return coverUrl;
    }
    
    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }
}