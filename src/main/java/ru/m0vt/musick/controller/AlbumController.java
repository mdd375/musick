package ru.m0vt.musick.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.m0vt.musick.dto.*;
import ru.m0vt.musick.model.*;
import ru.m0vt.musick.service.AlbumService;

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
    @PreAuthorize("hasAnyRole('ARTIST', 'ADMIN')")
    public Album createAlbum(@RequestBody AlbumCreateDTO albumDTO) {
        return albumService.createAlbum(albumDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isAlbumOwner(authentication, #id) or hasRole('ADMIN')")
    public Album updateAlbum(@PathVariable Long id, @RequestBody Album album) {
        return albumService.updateAlbum(id, album);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isAlbumOwner(authentication, #id) or hasRole('ADMIN')")
    public void deleteAlbum(@PathVariable Long id) {
        albumService.deleteAlbum(id);
    }

    @PostMapping("/{albumId}/purchase")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') and @securityService.isSameUser(authentication, #userId)")
    public Purchase purchaseAlbum(
        @PathVariable Long albumId,
        @RequestBody Long userId
    ) {
        return albumService.purchaseAlbum(albumId, userId);
    }

    @PostMapping("/{albumId}/tags")
    @PreAuthorize("@securityService.isAlbumOwner(authentication, #albumId) or hasRole('ADMIN')")
    public AlbumTag addTagToAlbum(
        @PathVariable Long albumId,
        @RequestBody String tag
    ) {
        return albumService.addTagToAlbum(albumId, tag);
    }

    @DeleteMapping("/{albumId}/tags/{tagId}")
    @PreAuthorize("@securityService.isAlbumOwner(authentication, #albumId) or hasRole('ADMIN')")
    public void removeTagFromAlbum(
        @PathVariable Long albumId,
        @PathVariable Long tagId
    ) {
        albumService.removeTagFromAlbum(albumId, tagId);
    }

    @GetMapping("/{albumId}/reviews")
    public List<Review> getReviewsForAlbum(@PathVariable Long albumId) {
        return albumService.getReviewsForAlbum(albumId);
    }

    @PostMapping("/{albumId}/reviews")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') and @securityService.isSameUser(authentication, #reviewDTO.userId)")
    public Review addReviewToAlbum(
        @PathVariable Long albumId,
        @RequestBody ReviewCreateDTO reviewDTO
    ) {
        return albumService.addReviewToAlbum(albumId, reviewDTO);
    }

    @GetMapping("/{albumId}/tracks")
    public List<Track> getTracksForAlbum(@PathVariable Long albumId) {
        return albumService.getTracksForAlbum(albumId);
    }

    @PostMapping("/{albumId}/tracks")
    @PreAuthorize("@securityService.isAlbumOwner(authentication, #albumId) or hasRole('ADMIN')")
    public Track addTrackToAlbum(
        @PathVariable Long albumId,
        @RequestBody TrackCreateDTO trackDTO
    ) {
        return albumService.addTrackToAlbum(albumId, trackDTO);
    }
}
