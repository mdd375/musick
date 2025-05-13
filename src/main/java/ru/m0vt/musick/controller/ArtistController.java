package ru.m0vt.musick.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.m0vt.musick.dto.ArtistCreateDTO;
import ru.m0vt.musick.model.Artist;
import ru.m0vt.musick.model.Album;
import ru.m0vt.musick.service.ArtistService;

import java.util.List;

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
    public Artist createArtist(@RequestBody ArtistCreateDTO artistDTO) {
        return artistService.createArtist(artistDTO);
    }

    @PutMapping("/{id}")
    public Artist updateArtist(@PathVariable Long id, @RequestBody Artist artist) {
        return artistService.updateArtist(id, artist);
    }

    @DeleteMapping("/{id}")
    public void deleteArtist(@PathVariable Long id) {
        artistService.deleteArtist(id);
    }

    @GetMapping("/{artistId}/albums")
    public List<Album> getAlbumsByArtist(@PathVariable Long artistId) {
        return artistService.getAlbumsByArtist(artistId);
    }
}
