package ru.m0vt.musick.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.m0vt.musick.dto.AlbumBriefDTO;
import ru.m0vt.musick.dto.AlbumCreateDTO;
import ru.m0vt.musick.dto.AlbumDetailDTO;
import ru.m0vt.musick.dto.AlbumUpdateDTO;
import ru.m0vt.musick.dto.ReviewCreateDTO;
import ru.m0vt.musick.dto.TrackCreateDTO;
import ru.m0vt.musick.exception.BadRequestException;
import ru.m0vt.musick.exception.NotEnoughMoney;
import ru.m0vt.musick.exception.PurchaseAlreadyExists;
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

    public List<AlbumBriefDTO> getAllAlbums() {
        return albumRepository
            .findAll()
            .stream()
            .map(this::convertToAlbumBriefDTO)
            .collect(Collectors.toList());
    }

    public AlbumDetailDTO getAlbumById(Long id) {
        Album album = albumRepository.findById(id).orElse(null);
        return album != null ? convertToAlbumDetailDTO(album) : null;
    }

    /**
     * Преобразует объект Album в AlbumBriefDTO
     *
     * @param album Объект альбома
     * @return AlbumBriefDTO с данными альбома
     */
    public AlbumBriefDTO convertToAlbumBriefDTO(Album album) {
        AlbumBriefDTO briefDTO = new AlbumBriefDTO();
        briefDTO.setId(album.getId());
        briefDTO.setTitle(album.getTitle());
        briefDTO.setCoverUrl(album.getCoverUrl());

        if (album.getArtist() != null) {
            briefDTO.setArtistId(album.getArtist().getId());
            briefDTO.setArtistName(album.getArtist().getName());
        }

        List<Track> tracks = album.getTracks();
        briefDTO.setTrackCount(tracks != null ? tracks.size() : 0);

        // Вычисляем общую продолжительность альбома
        int totalDuration = 0;
        if (tracks != null) {
            totalDuration = tracks
                .stream()
                .mapToInt(track ->
                    track.getDurationSec() != null ? track.getDurationSec() : 0
                )
                .sum();
        }
        briefDTO.setTotalDurationSec(totalDuration);

        return briefDTO;
    }

    /**
     * Преобразует объект Album в AlbumDetailDTO
     *
     * @param album Объект альбома
     * @return AlbumDetailDTO с данными альбома
     */
    public AlbumDetailDTO convertToAlbumDetailDTO(Album album) {
        AlbumDetailDTO detailDTO = new AlbumDetailDTO();
        detailDTO.setId(album.getId());
        detailDTO.setTitle(album.getTitle());
        detailDTO.setCoverUrl(album.getCoverUrl());
        detailDTO.setReleaseDate(album.getReleaseDate());
        detailDTO.setPrice(album.getPrice());

        if (album.getArtist() != null) {
            detailDTO.setArtistId(album.getArtist().getId());
            detailDTO.setArtistName(album.getArtist().getName());
        }

        // Получаем треки и сортируем их по номеру
        List<Track> tracks = album.getTracks();
        if (tracks != null) {
            detailDTO.setTracks(
                tracks
                    .stream()
                    .sorted(Comparator.comparing(Track::getTrackNumber))
                    .collect(Collectors.toList())
            );
        }

        // Добавляем теги
        detailDTO.setTags(album.getTags());

        return detailDTO;
    }

    public AlbumDetailDTO createAlbum(
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

        Album savedAlbum = albumRepository.save(album);
        return convertToAlbumDetailDTO(savedAlbum);
    }

    public AlbumDetailDTO updateAlbum(Long id, AlbumUpdateDTO albumUpdateDTO) {
        Album existingAlbum = albumRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Album not found")
            );

        existingAlbum.setTitle(albumUpdateDTO.getTitle());
        existingAlbum.setCoverUrl(albumUpdateDTO.getCoverUrl());
        existingAlbum.setPrice(albumUpdateDTO.getPrice());
        existingAlbum.setReleaseDate(albumUpdateDTO.getReleaseDate());

        Album updatedAlbum = albumRepository.save(existingAlbum);
        return convertToAlbumDetailDTO(updatedAlbum);
    }

    public void deleteAlbum(Long id) {
        var album = albumRepository.findById(id).orElse(null);
        if (album == null) {
            throw new ResourceNotFoundException("Album not found");
        }
        var tracks = album.getTracks();
        for (var track : tracks) {
            trackRepository.delete(track);
        }
        albumRepository.deleteById(id);
    }

    @Transactional
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

        // Проверяем, существует ли уже покупка
        if (
            purchaseRepository.findByUser_IdAndAlbum_Id(
                user.getId(),
                album.getId()
            ) !=
            null
        ) {
            throw new PurchaseAlreadyExists("Purchase already exists");
        }

        // Проверяем баланс пользователя
        if (album.getPrice().compareTo(user.getBalance()) > 0) {
            throw new NotEnoughMoney("Not enough money for payment");
        }

        // Получаем артиста альбома
        var artist = album.getArtist();
        if (artist == null) {
            throw new ResourceNotFoundException("Album's artist not found");
        }

        // Проверка, не пытается ли артист купить свой собственный альбом
        if (
            artist.getUser() != null &&
            artist.getUser().getId().equals(user.getId())
        ) {
            throw new BadRequestException(
                "Artist cannot purchase their own album"
            );
        }

        var artist_user = artist.getUser();

        // Обновляем балансы
        artist_user.setBalance(artist_user.getBalance().add(album.getPrice()));
        user.setBalance(user.getBalance().subtract(album.getPrice()));

        // Сохраняем изменения пользователей
        userRepository.save(artist_user);
        userRepository.save(user);

        // Создаем и сохраняем запись о покупке
        var purchase = new Purchase();
        purchase.setAlbum(album);
        purchase.setUser(user);
        purchase.setAmount(album.getPrice());
        purchase.setPurchaseDate(LocalDateTime.now());
        return purchaseRepository.save(purchase);
    }

    public AlbumTag addTagToAlbum(Long albumId, String tagName) {
        var album = albumRepository.findById(albumId).orElse(null);
        if (album == null) {
            throw new ResourceNotFoundException("Album not found");
        }
        var tag = tagRepository.findByName(tagName);
        if (tag == null) {
            var newTag = new ru.m0vt.musick.model.Tag();
            newTag.setName(tagName);
            tag = tagRepository.save(newTag);
        }
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
        review.setCreatedAt(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    public List<Track> getTracksForAlbum(Long albumId) {
        var album = albumRepository.findById(albumId).orElse(null);
        if (album == null) {
            throw new ResourceNotFoundException("Album not found");
        }

        // Сортируем треки по номеру перед возвратом
        return album.getTracks() != null
            ? album
                .getTracks()
                .stream()
                .sorted(Comparator.comparing(Track::getTrackNumber))
                .collect(Collectors.toList())
            : null;
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
