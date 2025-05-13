package ru.m0vt.musick.service;

import org.springframework.stereotype.Service;
import ru.m0vt.musick.model.*;
import ru.m0vt.musick.repository.*;

import java.util.List;

@Service
public class AlbumService {
    private AlbumRepository albumRepository;
    private UserRepository userRepository;
    private PurchaseRepository purchaseRepository;
    private TagRepository tagRepository;
    private AlbumTagRepository albumTagRepository;
    private ReviewRepository reviewRepository;
    private TrackRepository trackRepository;

    public List<Album> getAllAlbums() {
        return albumRepository.findAll();
    }

    public Album getAlbumById(Long id) {
        return albumRepository.findById(id).orElse(null);
    }

    public Album createAlbum(Album album) {
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

    public Purchase purchaseAlbum(Long albumId, Long userId) {
        var user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        var album = albumRepository.findById(albumId).orElse(null);
        if (album == null) {
            throw new RuntimeException("Album not found");
        }
        var purchase = new Purchase();
        purchase.setAlbum(album);
        purchase.setUser(user);
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

    public Review addReviewToAlbum(Long albumId, Review review) {
        var album = albumRepository.findById(albumId).orElse(null);
        if (album == null) {
            throw new RuntimeException("Album not found");
        }
        review.setAlbum(album);
        return reviewRepository.save(review);
    }

    public List<Track> getTracksForAlbum(Long albumId) {
        var album = albumRepository.findById(albumId).orElse(null);
        if (album == null) {
            throw new RuntimeException("Album not found");
        }
        return album.getTracks();
    }

    public Track addTrackToAlbum(Long albumId, Track track) {
        var album = albumRepository.findById(albumId).orElse(null);
        if (album != null) {
            track.setAlbum(album);
            return trackRepository.save(track);
        }
        return null;
    }
}
