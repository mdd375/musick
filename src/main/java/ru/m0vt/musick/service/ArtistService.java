package ru.m0vt.musick.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ru.m0vt.musick.dto.ArtistCreateDTO;
import ru.m0vt.musick.exception.ResourceNotFoundException;
import ru.m0vt.musick.exception.UserValidationException;
import ru.m0vt.musick.model.Album;
import ru.m0vt.musick.model.Artist;
import ru.m0vt.musick.model.Review;
import ru.m0vt.musick.model.Subscription;
import ru.m0vt.musick.repository.AlbumRepository;
import ru.m0vt.musick.repository.ArtistRepository;
import ru.m0vt.musick.repository.ReviewRepository;
import ru.m0vt.musick.repository.SubscriptionRepository;
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
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    public List<Artist> getAllArtists() {
        return artistRepository.findAll();
    }

    public Artist getArtistById(Long id) {
        return artistRepository.findById(id).orElse(null);
    }

    public Artist createArtist(ArtistCreateDTO artistDTO, Authentication authentication) {
        String username = authentication.getName();
        var user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }
        
        // Проверка, что у пользователя еще нет профиля артиста
        if (user.getArtistProfile() != null) {
            throw new UserValidationException("User already has artist profile");
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

    public List<Album> getArtistAlbums(Long artistId) {
        Artist artist = artistRepository.findById(artistId).orElse(null);
        if (artist != null) {
            return albumRepository.findByArtist(artist);
        }
        return null;
    }
    
    public List<Subscription> getArtistSubscribers(Long artistId) {
        Artist artist = artistRepository.findById(artistId).orElse(null);
        if (artist != null) {
            return subscriptionRepository.findByArtistId(artistId);
        }
        return null;
    }
    
    public List<Review> getAlbumReviews(Long albumId) {
        return reviewRepository.findByAlbumId(albumId);
    }
}
