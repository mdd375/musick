package ru.m0vt.musick.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.m0vt.musick.dto.AlbumBriefDTO;
import ru.m0vt.musick.dto.ArtistCreateDTO;
import ru.m0vt.musick.dto.ArtistCreateResponseDTO;
import ru.m0vt.musick.dto.ArtistUpdateDTO;
import ru.m0vt.musick.exception.BadRequestException;
import ru.m0vt.musick.exception.ResourceNotFoundException;
import ru.m0vt.musick.exception.SubscriptionConflictException;
import ru.m0vt.musick.exception.UserValidationException;
import ru.m0vt.musick.model.Artist;
import ru.m0vt.musick.model.Review;
import ru.m0vt.musick.model.Subscription;
import ru.m0vt.musick.model.User;
import ru.m0vt.musick.repository.AlbumRepository;
import ru.m0vt.musick.repository.ArtistRepository;
import ru.m0vt.musick.repository.ReviewRepository;
import ru.m0vt.musick.repository.SubscriptionRepository;
import ru.m0vt.musick.repository.UserRepository;
import ru.m0vt.musick.security.JwtService;
import ru.m0vt.musick.security.JwtUserDetailsService;

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

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Autowired
    private AlbumService albumService;

    public List<Artist> getAllArtists() {
        return artistRepository.findAll();
    }

    public Artist getArtistById(Long id) {
        return artistRepository.findById(id).orElse(null);
    }

    @Transactional
    public ArtistCreateResponseDTO createArtist(
        ArtistCreateDTO artistDTO,
        Authentication authentication
    ) {
        String username = authentication.getName();
        User user = userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Проверка, что у пользователя еще нет профиля артиста
        if (user.getArtistProfile() != null) {
            throw new UserValidationException(
                "User already has artist profile"
            );
        }

        // Создаем профиль артиста
        Artist artist = new Artist();
        artist.setUser(user);
        artist.setName(artistDTO.getName());
        artist.setBio(artistDTO.getBio());
        artist.setPhotoUrl(artistDTO.getPhotoUrl());

        // Обновляем роль пользователя на ARTIST
        user.setRole("ARTIST");
        userRepository.save(user);

        Artist savedArtist = artistRepository.save(artist);

        // Генерируем новый JWT токен с обновленной ролью
        UserDetails userDetails = userDetailsService.loadUserByUsername(
            username
        );
        String token = jwtService.generateToken(userDetails);

        // Возвращаем информацию об артисте и новый токен
        return new ArtistCreateResponseDTO(
            savedArtist,
            token,
            user.getUsername(),
            user.getRole()
        );
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

    public List<AlbumBriefDTO> getArtistAlbums(Long artistId) {
        Artist artist = artistRepository.findById(artistId).orElse(null);
        if (artist != null) {
            return albumRepository
                .findByArtist(artist)
                .stream()
                .map(albumService::convertToAlbumBriefDTO)
                .collect(Collectors.toList());
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

    /**
     * Обновляет профиль артиста
     *
     * @param artistId ID артиста
     * @param artistUpdateDTO DTO с новыми данными артиста
     * @return Обновленный объект артиста
     */
    @Transactional
    public Artist updateArtistProfile(
        Long artistId,
        ArtistUpdateDTO artistUpdateDTO
    ) {
        Artist artist = artistRepository
            .findById(artistId)
            .orElseThrow(() -> new ResourceNotFoundException("Artist not found")
            );

        artist.setName(artistUpdateDTO.getName());
        artist.setBio(artistUpdateDTO.getBio());
        artist.setPhotoUrl(artistUpdateDTO.getPhotoUrl());

        return artistRepository.save(artist);
    }

    /**
     * Создает подписку на артиста
     *
     * @param artistId ID артиста
     * @param authentication Объект аутентификации
     * @return Объект подписки
     */
    @Transactional
    public Subscription subscribeToArtist(
        Long artistId,
        Authentication authentication
    ) {
        String username = authentication.getName();
        User user = userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Проверяем, что артист существует
        if (artistId == null) {
            throw new BadRequestException("Artist ID cannot be null");
        }

        Artist foundArtist = artistRepository
            .findById(artistId)
            .orElseThrow(() -> new ResourceNotFoundException("Artist not found")
            );

        // Проверяем, не пытается ли пользователь подписаться на себя
        if (
            foundArtist.getUser() != null &&
            foundArtist.getUser().getId().equals(user.getId())
        ) {
            throw new BadRequestException("Cannot subscribe to yourself");
        }

        // Проверяем, существует ли уже подписка
        boolean subscriptionExists =
            subscriptionRepository.existsByUserIdAndArtistId(
                user.getId(),
                artistId
            );
        if (subscriptionExists) {
            throw new SubscriptionConflictException(
                "You are already subscribed to this artist"
            );
        }

        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setArtist(foundArtist);
        subscription.setCreatedAt(LocalDateTime.now());
        return subscriptionRepository.save(subscription);
    }

    /**
     * Удаляет подписку на артиста
     *
     * @param artistId ID артиста
     * @param authentication Объект аутентификации
     * @return true если подписка успешно удалена
     */
    @Transactional
    public boolean unsubscribeFromArtist(
        Long artistId,
        Authentication authentication
    ) {
        String username = authentication.getName();
        User user = userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Проверяем, что артист существует
        artistRepository
            .findById(artistId)
            .orElseThrow(() -> new ResourceNotFoundException("Artist not found")
            );

        // Проверяем существование подписки
        Subscription subscription = subscriptionRepository
            .findByUserIdAndArtistId(user.getId(), artistId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Subscription not found")
            );

        // Удаляем подписку
        subscriptionRepository.delete(subscription);

        return true;
    }
}
