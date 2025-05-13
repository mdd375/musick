package ru.m0vt.musick.service;

import org.springframework.stereotype.Service;
import ru.m0vt.musick.model.Album;
import ru.m0vt.musick.model.Artist;
import ru.m0vt.musick.repository.AlbumRepository;
import ru.m0vt.musick.repository.ArtistRepository;

import java.util.List;

@Service
public class ArtistService {
    private ArtistRepository artistRepository;
    private AlbumRepository albumRepository;

    public List<Artist> getAllArtists() {
        return artistRepository.findAll();
    }

    public Artist getArtistById(Long id) {
        return artistRepository.findById(id).orElse(null);
    }

    public Artist createArtist(Artist artist) {
        return artistRepository.save(artist);
    }

    public Artist updateArtist(Long id, Artist artist) {
        var existingArtist = artistRepository.findById(id).orElse(null);
        if (existingArtist != null) {
            existingArtist.setName(artist.getName());
            existingArtist.setBio(artist.getBio());
            existingArtist.setPhotoUrl(artist.getPhotoUrl());
            return artistRepository.save(existingArtist);
        }
        return null;
    }

    public void deleteArtist(Long id) {
        artistRepository.deleteById(id);
    }

    public List<Album> getAlbumsByArtist(Long artistId) {
        Artist artist = artistRepository.findById(artistId).orElse(null);
        if (artist != null) {
            return albumRepository.findByArtist(artist);
        }
        return null;
    }
}
