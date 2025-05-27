package ru.m0vt.musick.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    public Album createAlbum(
        @RequestBody AlbumCreateDTO albumDTO,
        Authentication authentication
    ) {
        return albumService.createAlbum(albumDTO, authentication);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isAlbumOwner(authentication, #id)")
    public Album updateAlbum(@PathVariable Long id, @RequestBody Album album) {
        return albumService.updateAlbum(id, album);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isAlbumOwner(authentication, #id)")
    public void deleteAlbum(@PathVariable Long id) {
        albumService.deleteAlbum(id);
    }

    @PostMapping("/{albumId}/purchase")
    @PreAuthorize("@securityService.canPurchaseAlbum(authentication)")
    public Purchase purchaseAlbum(
        @PathVariable Long albumId,
        Authentication authentication
    ) {
        return albumService.purchaseAlbum(albumId, authentication);
    }

    @PostMapping("/{albumId}/tags")
    @PreAuthorize("@securityService.isAlbumOwner(authentication, #albumId)")
    public AlbumTag addTagToAlbum(
        @PathVariable Long albumId,
        @RequestBody String tag
    ) {
        return albumService.addTagToAlbum(albumId, tag);
    }

    @DeleteMapping("/{albumId}/tags/{tagId}")
    @PreAuthorize("@securityService.isAlbumOwner(authentication, #albumId)")
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
    @PreAuthorize("@securityService.canWriteReview(authentication)")
    public Review addReviewToAlbum(
        @PathVariable Long albumId,
        @RequestBody ReviewCreateDTO reviewDTO,
        Authentication authentication
    ) {
        return albumService.addReviewToAlbum(
            albumId,
            reviewDTO,
            authentication
        );
    }

    @GetMapping("/{albumId}/tracks")
    public List<Track> getTracksForAlbum(@PathVariable Long albumId) {
        return albumService.getTracksForAlbum(albumId);
    }

    @PostMapping("/{albumId}/tracks")
    @PreAuthorize("@securityService.isAlbumOwner(authentication, #albumId)")
    public Track addTrackToAlbum(
        @PathVariable Long albumId,
        @RequestBody TrackCreateDTO trackDTO
    ) {
        return albumService.addTrackToAlbum(albumId, trackDTO);
    }
}
