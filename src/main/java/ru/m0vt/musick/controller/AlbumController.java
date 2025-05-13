package ru.m0vt.musick.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.m0vt.musick.model.*;
import ru.m0vt.musick.service.AlbumService;

import java.util.List;

@RestController
@RequestMapping("/albums")
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    @GetMapping
    public List<Album> getAllAlbums() {
        return albumService.getAllAlbums();
    }

    @GetMapping("/{id}")
    public Album getAlbumById(@PathVariable Long id) {
        return albumService.getAlbumById(id);
    }

    @PostMapping
    public Album createAlbum(@RequestBody Album album) {
        return albumService.createAlbum(album);
    }

    @PutMapping("/{id}")
    public Album updateAlbum(@PathVariable Long id, @RequestBody Album album) {
        return albumService.updateAlbum(id, album);
    }

    @DeleteMapping("/{id}")
    public void deleteAlbum(@PathVariable Long id) {
        albumService.deleteAlbum(id);
    }

    @PostMapping("/{albumId}/purchase")
    public Purchase purchaseAlbum(@PathVariable Long albumId, @RequestBody Long userId) {
        return albumService.purchaseAlbum(albumId, userId);
    }

    @PostMapping("/{albumId}/tags")
    public AlbumTag addTagToAlbum(@PathVariable Long albumId, @RequestBody String tag) {
        return albumService.addTagToAlbum(albumId, tag);
    }

    @DeleteMapping("/{albumId}/tags/{tagId}")
    public void removeTagFromAlbum(@PathVariable Long albumId, @PathVariable Long tagId) {
        albumService.removeTagFromAlbum(albumId, tagId);
    }

    @GetMapping("/{albumId}/reviews")
    public List<Review> getReviewsForAlbum(@PathVariable Long albumId) {
        return albumService.getReviewsForAlbum(albumId);
    }

    @PostMapping("/{albumId}/reviews")
    public Review addReviewToAlbum(@PathVariable Long albumId, @RequestBody Review review) {
        return albumService.addReviewToAlbum(albumId, review);
    }

    @GetMapping("/{albumId}/tracks")
    public List<Track> getTracksForAlbum(@PathVariable Long albumId) {
        return albumService.getTracksForAlbum(albumId);
    }

    @PostMapping("/{albumId}/tracks")
    public Track addTrackToAlbum(@PathVariable Long albumId, @RequestBody Track track) {
        return albumService.addTrackToAlbum(albumId, track);
    }
}
