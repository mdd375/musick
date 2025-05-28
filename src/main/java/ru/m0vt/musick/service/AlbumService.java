package ru.m0vt.musick.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.m0vt.musick.dto.AlbumCreateDTO;
import ru.m0vt.musick.dto.ReviewCreateDTO;
import ru.m0vt.musick.dto.TrackCreateDTO;
import ru.m0vt.musick.exception.ResourceNotFoundException;
import ru.m0vt.musick.exception.UserValidationException;
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

    public List<Album> getAllAlbums() {
        return albumRepository.findAll();
    }

    public Album getAlbumById(Long id) {
        return albumRepository.findById(id).orElse(null);
    }

    public Album createAlbum(
        AlbumCreateDTO albumDTO,
        Authentication authentication
    ) {
        Album album = new Album();
        album.setTitle(albumDTO.getTitle());
        album.setCoverUrl(albumDTO.getCoverUrl());
        album.setPrice(albumDTO.getPrice());
        album.setReleaseDate(albumDTO.getReleaseDate());
        album.setCreatedAt(LocalDateTime.now());

        // Получаем пользователя из токена
        String username = authentication.getName();
        var user = userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Проверяем, что пользователь имеет профиль артиста
        if (user.getArtistProfile() == null) {
            throw new UserValidationException(
                "User does not have an artist profile"
            );
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
        var user = userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        var album = albumRepository
            .findById(albumId)
            .orElseThrow(() -> new ResourceNotFoundException("Album not found")
            );

        var purchase = new Purchase();
        purchase.setAlbum(album);
        purchase.setUser(user);
        purchase.setAmount(album.getPrice());
        return purchaseRepository.save(purchase);
    }

    public AlbumTag addTagToAlbum(Long albumId, String tagName) {
        var album = albumRepository.findById(albumId).orElse(null);
        if (album == null) {
            throw new ResourceNotFoundException("Album not found");
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
            throw new ResourceNotFoundException("Album not found");
        }
        return album.getReviews();
    }

    public Review addReviewToAlbum(
        Long albumId,
        ReviewCreateDTO reviewDTO,
        Authentication authentication
    ) {
        var album = albumRepository
            .findById(albumId)
            .orElseThrow(() -> new ResourceNotFoundException("Album not found")
            );

        // Получаем пользователя из токена
        String username = authentication.getName();
        var user = userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

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
            throw new ResourceNotFoundException("Album not found");
        }
        return album.getTracks();
    }

    public Track addTrackToAlbum(Long albumId, TrackCreateDTO trackDTO) {
        var album = albumRepository
            .findById(albumId)
            .orElseThrow(() -> new ResourceNotFoundException("Album not found")
            );

        // Получаем текущие треки альбома
        List<Track> tracks = album.getTracks();

        // Определяем номер для нового трека (последний номер + 1 или 1, если треков нет)
        int trackNumber = 1;
        if (tracks != null && !tracks.isEmpty()) {
            trackNumber = tracks.size() + 1;
        }

        // Создаем новый трек
        Track track = new Track();
        track.setAlbum(album);
        track.setTitle(trackDTO.getTitle());
        track.setDurationSec(trackDTO.getDurationSec());
        track.setTrackNumber(trackNumber);
        track.setAudioUrl(trackDTO.getAudioUrl());

        return trackRepository.save(track);
    }

    /**
     * Удаляет трек из альбома по номеру позиции и перестраивает нумерацию остальных треков
     *
     * @param albumId ID альбома
     * @param trackPosition Позиция трека (не ID, а порядковый номер в альбоме)
     * @return Обновленный список треков альбома
     */
    @Transactional
    public List<Track> removeTrackFromAlbum(
        Long albumId,
        Integer trackPosition
    ) {
        var album = albumRepository
            .findById(albumId)
            .orElseThrow(() -> new ResourceNotFoundException("Album not found")
            );

        List<Track> tracks = album.getTracks();
        if (tracks == null || tracks.isEmpty()) {
            throw new ResourceNotFoundException("Album has no tracks");
        }

        // Находим трек по позиции
        Track trackToRemove = null;
        for (Track t : tracks) {
            if (t.getTrackNumber().equals(trackPosition)) {
                trackToRemove = t;
                break;
            }
        }

        if (trackToRemove == null) {
            throw new ResourceNotFoundException(
                "Track at position " + trackPosition + " not found"
            );
        }

        // Удаляем трек
        trackRepository.delete(trackToRemove);

        // Обновляем позиции оставшихся треков
        for (Track t : tracks) {
            if (t.getTrackNumber() > trackPosition) {
                t.setTrackNumber(t.getTrackNumber() - 1);
                trackRepository.save(t);
            }
        }

        // Возвращаем обновленный список треков
        return albumRepository
            .findById(albumId)
            .orElseThrow(() -> new ResourceNotFoundException("Album not found"))
            .getTracks()
            .stream()
            .sorted(Comparator.comparing(Track::getTrackNumber))
            .collect(Collectors.toList());
    }

    /**
     * Перемещает трек в альбоме на новую позицию
     *
     * @param albumId ID альбома
     * @param currentPosition Текущая позиция трека
     * @param newPosition Новая позиция трека
     * @return Обновленный список треков альбома
     */
    @Transactional
    public List<Track> moveTrackPosition(
        Long albumId,
        Integer currentPosition,
        Integer newPosition
    ) {
        var album = albumRepository
            .findById(albumId)
            .orElseThrow(() -> new ResourceNotFoundException("Album not found")
            );

        List<Track> tracks = album.getTracks();
        if (tracks == null || tracks.isEmpty()) {
            throw new ResourceNotFoundException("Album has no tracks");
        }

        // Проверка валидности позиций
        if (
            currentPosition < 1 ||
            newPosition < 1 ||
            currentPosition > tracks.size() ||
            newPosition > tracks.size()
        ) {
            throw new IllegalArgumentException("Invalid track position");
        }

        if (currentPosition.equals(newPosition)) {
            // Если позиция не меняется, просто возвращаем текущие треки
            return tracks;
        }

        // Находим перемещаемый трек
        Track trackToMove = null;
        for (Track t : tracks) {
            if (t.getTrackNumber().equals(currentPosition)) {
                trackToMove = t;
                break;
            }
        }

        if (trackToMove == null) {
            throw new ResourceNotFoundException(
                "Track at position " + currentPosition + " not found"
            );
        }

        // Обновляем позиции треков
        if (currentPosition < newPosition) {
            // Перемещение вниз - уменьшаем позиции треков между currentPosition и newPosition
            for (Track t : tracks) {
                int pos = t.getTrackNumber();
                if (pos > currentPosition && pos <= newPosition) {
                    t.setTrackNumber(pos - 1);
                    trackRepository.save(t);
                }
            }
        } else {
            // Перемещение вверх - увеличиваем позиции треков между newPosition и currentPosition
            for (Track t : tracks) {
                int pos = t.getTrackNumber();
                if (pos >= newPosition && pos < currentPosition) {
                    t.setTrackNumber(pos + 1);
                    trackRepository.save(t);
                }
            }
        }

        // Устанавливаем новую позицию для перемещаемого трека
        trackToMove.setTrackNumber(newPosition);
        trackRepository.save(trackToMove);

        // Возвращаем обновленный список треков
        return albumRepository
            .findById(albumId)
            .orElseThrow(() -> new ResourceNotFoundException("Album not found"))
            .getTracks()
            .stream()
            .sorted(Comparator.comparing(Track::getTrackNumber))
            .collect(Collectors.toList());
    }
}
