package ru.m0vt.musick.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.m0vt.musick.dto.ArtistCreateDTO;
import ru.m0vt.musick.model.Album;
import ru.m0vt.musick.model.Artist;
import ru.m0vt.musick.model.Review;
import ru.m0vt.musick.model.Subscription;
import ru.m0vt.musick.service.ArtistService;

@RestController
@RequestMapping("/artists")
public class ArtistController {

    @Autowired
    private ArtistService artistService;

    @GetMapping
    public List<Artist> getAllArtists() {
        return artistService.getAllArtists();
    }

    @GetMapping("/{id}")
    public Artist getArtistById(@PathVariable Long id) {
        return artistService.getArtistById(id);
    }

    @PostMapping
    @PreAuthorize(
        "@securityService.canCreateArtistFor(authentication, #artistDTO.userId)"
    )
    public Artist createArtist(@RequestBody ArtistCreateDTO artistDTO) {
        return artistService.createArtist(artistDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize(
        "@securityService.isSameUser(authentication, #artist.user.id)"
    )
    public Artist updateArtist(
        @PathVariable Long id,
        @RequestBody Artist artist
    ) {
        return artistService.updateArtist(id, artist);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isSameUser(authentication, #id)")
    public void deleteArtist(@PathVariable Long id) {
        artistService.deleteArtist(id);
    }

    @GetMapping("/{artistId}/albums")
    public List<Album> getArtistAlbums(@PathVariable Long artistId) {
        return artistService.getArtistAlbums(artistId);
    }

    @GetMapping("/{artistId}/subscribers")
    @PreAuthorize("@securityService.isSameUser(authentication, #artistId)")
    public List<Subscription> getArtistSubscribers(
        @PathVariable Long artistId
    ) {
        return artistService.getArtistSubscribers(artistId);
    }

    @GetMapping("/{artistId}/albums/{albumId}/reviews")
    public List<Review> getAlbumReviews(
        @PathVariable Long artistId,
        @PathVariable Long albumId
    ) {
        return artistService.getAlbumReviews(albumId);
    }
}
