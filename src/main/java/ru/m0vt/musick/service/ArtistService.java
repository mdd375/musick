package ru.m0vt.musick.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.m0vt.musick.dto.ArtistCreateDTO;
import ru.m0vt.musick.model.Album;
import ru.m0vt.musick.model.Artist;
import ru.m0vt.musick.repository.AlbumRepository;
import ru.m0vt.musick.repository.ArtistRepository;
import ru.m0vt.musick.repository.UserRepository;

import java.util.List;

@Service
public class ArtistService {
    @Autowired
    private ArtistRepository artistRepository;
    @Autowired
    private AlbumRepository albumRepository;
    @Autowired
    private UserRepository userRepository;

    public List<Artist> getAllArtists() {
        return artistRepository.findAll();
    }

    public Artist getArtistById(Long id) {
        return artistRepository.findById(id).orElse(null);
    }

    public Artist createArtist(ArtistCreateDTO artistDTO) {
        var user = userRepository.findById(artistDTO.getUserId()).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        Artist artist = new Artist();
        artist.setUser(user);
        artist.setName(artistDTO.getName());
        artist.setBio(artistDTO.getBio());
        artist.setPhotoUrl(artistDTO.getPhotoUrl());
        
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
