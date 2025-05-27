package ru.m0vt.musick.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ru.m0vt.musick.dto.AlbumCreateDTO;
import ru.m0vt.musick.dto.ReviewCreateDTO;
import ru.m0vt.musick.dto.TrackCreateDTO;
import ru.m0vt.musick.model.Album;
import ru.m0vt.musick.model.AlbumTag;
import ru.m0vt.musick.model.Purchase;
import ru.m0vt.musick.model.Review;
import ru.m0vt.musick.model.Track;
import ru.m0vt.musick.repository.*;

@Service
public class AlbumService {

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private AlbumTagRepository albumTagRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private ArtistRepository artistRepository;

    public List<Album> getAllAlbums() {
        return albumRepository.findAll();
    }

    public Album getAlbumById(Long id) {
        return albumRepository.findById(id).orElse(null);
    }

    public Album createAlbum(AlbumCreateDTO albumDTO, Authentication authentication) {
        Album album = new Album();
        album.setTitle(albumDTO.getTitle());
        album.setCoverUrl(albumDTO.getCoverUrl());
        album.setPrice(albumDTO.getPrice());
        album.setReleaseDate(albumDTO.getReleaseDate());

        // Получаем пользователя из токена
        String username = authentication.getName();
        var user = userRepository.findByUsername(username).orElseThrow(() -> 
            new RuntimeException("User not found"));
        
        // Проверяем, что пользователь имеет профиль артиста
        if (user.getArtistProfile() == null) {
            throw new RuntimeException("User does not have an artist profile");
        }
        
        album.setArtist(user.getArtistProfile());

        return albumRepository.save(album);
    }

    public Album updateAlbum(Long id, Album album) {
        Album existingAlbum = albumRepository.findById(id).orElse(null);
        if (existingAlbum != null) {
            existingAlbum.setTitle(album.getTitle());
            existingAlbum.setCoverUrl(album.getCoverUrl());
            existingAlbum.setPrice(album.getPrice());
            existingAlbum.setReleaseDate(album.getReleaseDate());
            return albumRepository.save(existingAlbum);
        }
        return null;
    }

    public void deleteAlbum(Long id) {
        albumRepository.deleteById(id);
    }

    public Purchase purchaseAlbum(Long albumId, Authentication authentication) {
        // Получаем пользователя из токена
        String username = authentication.getName();
        var user = userRepository.findByUsername(username).orElseThrow(() -> 
            new RuntimeException("User not found"));
            
        var album = albumRepository.findById(albumId).orElseThrow(() -> 
            new RuntimeException("Album not found"));
            
        var purchase = new Purchase();
        purchase.setAlbum(album);
        purchase.setUser(user);
        purchase.setAmount(album.getPrice());
        return purchaseRepository.save(purchase);
    }

    public AlbumTag addTagToAlbum(Long albumId, String tagName) {
        var album = albumRepository.findById(albumId).orElse(null);
        if (album == null) {
            throw new RuntimeException("Album not found");
        }
        var tag = tagRepository.findByName(tagName);
        var albumTag = new AlbumTag();
        albumTag.setTag(tag);
        albumTag.setAlbum(album);
        return albumTagRepository.save(albumTag);
    }

    public void removeTagFromAlbum(Long albumId, Long tagId) {
        var albumTag = albumTagRepository.findByAlbumIdAndTagId(albumId, tagId);
        if (albumTag != null) {
            albumTagRepository.delete(albumTag);
        }
    }

    public List<Review> getReviewsForAlbum(Long albumId) {
        var album = albumRepository.findById(albumId).orElse(null);
        if (album == null) {
            throw new RuntimeException("Album not found");
        }
        return album.getReviews();
    }

    public Review addReviewToAlbum(Long albumId, ReviewCreateDTO reviewDTO, Authentication authentication) {
        var album = albumRepository.findById(albumId).orElseThrow(() -> 
            new RuntimeException("Album not found"));

        // Получаем пользователя из токена
        String username = authentication.getName();
        var user = userRepository.findByUsername(username).orElseThrow(() -> 
            new RuntimeException("User not found"));

        Review review = new Review();
        review.setAlbum(album);
        review.setUser(user);
        review.setText(reviewDTO.getText());
        review.setFavoriteTracks(reviewDTO.getFavoriteTracks());

        return reviewRepository.save(review);
    }

    public List<Track> getTracksForAlbum(Long albumId) {
        var album = albumRepository.findById(albumId).orElse(null);
        if (album == null) {
            throw new RuntimeException("Album not found");
        }
        return album.getTracks();
    }

    public Track addTrackToAlbum(Long albumId, TrackCreateDTO trackDTO) {
        var album = albumRepository.findById(albumId).orElse(null);
        if (album != null) {
            Track track = new Track();
            track.setAlbum(album);
            track.setTitle(trackDTO.getTitle());
            track.setDurationSec(trackDTO.getDurationSec());
            track.setTrackNumber(trackDTO.getTrackNumber());
            track.setAudioUrl(trackDTO.getAudioUrl());
            return trackRepository.save(track);
        }
        return null;
    }
}
