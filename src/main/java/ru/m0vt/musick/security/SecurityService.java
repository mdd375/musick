package ru.m0vt.musick.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import ru.m0vt.musick.model.Album;
import ru.m0vt.musick.model.Review;
import ru.m0vt.musick.model.Track;
import ru.m0vt.musick.model.User;
import ru.m0vt.musick.repository.AlbumRepository;
import ru.m0vt.musick.repository.ReviewRepository;
import ru.m0vt.musick.repository.TrackRepository;
import ru.m0vt.musick.repository.UserRepository;

import java.util.Optional;

/**
 * Централизованный сервис для проверки прав доступа.
 * Используется в аннотациях @PreAuthorize для проверки бизнес-правил авторизации.
 */
@Service
public class SecurityService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AlbumRepository albumRepository;
    
    @Autowired
    private TrackRepository trackRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;

    /**
     * Проверяет, является ли текущий пользователь владельцем альбома.
     * 
     * @param authentication Текущая аутентификация
     * @param albumId ID альбома для проверки
     * @return true, если пользователь является владельцем альбома
     */
    public boolean isAlbumOwner(Authentication authentication, Long albumId) {
        if (authentication == null || albumId == null) {
            return false;
        }
        
        // Проверка наличия роли ADMIN
        if (hasRole(authentication, "ADMIN")) {
            return true;
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || user.getArtistProfile() == null) {
            return false;
        }
        
        Album album = albumRepository.findById(albumId).orElse(null);
        if (album == null || album.getArtist() == null) {
            return false;
        }
        
        return album.getArtist().getUser().getId().equals(user.getId());
    }
    
    /**
     * Проверяет, имеет ли текущий пользователь право управлять треком.
     * 
     * @param authentication Текущая аутентификация
     * @param trackId ID трека для проверки
     * @return true, если пользователь имеет право управлять треком
     */
    public boolean canManageTrack(Authentication authentication, Long trackId) {
        if (authentication == null || trackId == null) {
            return false;
        }
        
        // Проверка наличия роли ADMIN
        if (hasRole(authentication, "ADMIN")) {
            return true;
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || user.getArtistProfile() == null) {
            return false;
        }
        
        // Получаем трек и проверяем, принадлежит ли он альбому пользователя
        Track track = trackRepository.findById(trackId).orElse(null);
        if (track == null || track.getAlbum() == null || track.getAlbum().getArtist() == null) {
            return false;
        }
        
        return track.getAlbum().getArtist().getUser().getId().equals(user.getId());
    }
    
    /**
     * Проверяет, может ли текущий пользователь управлять обзором.
     * 
     * @param authentication Текущая аутентификация
     * @param reviewId ID обзора для проверки
     * @return true, если пользователь может управлять обзором
     */
    public boolean canManageReview(Authentication authentication, Long reviewId) {
        if (authentication == null || reviewId == null) {
            return false;
        }
        
        // Проверка наличия роли ADMIN
        if (hasRole(authentication, "ADMIN")) {
            return true;
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return false;
        }
        
        // Проверяем, является ли пользователь автором обзора
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) {
            return false;
        }
        
        return review.getUser().getId().equals(user.getId());
    }
    
    /**
     * Проверяет, имеет ли текущий пользователь указанную роль.
     * 
     * @param authentication Текущая аутентификация
     * @param role Проверяемая роль (без префикса "ROLE_")
     * @return true, если пользователь имеет указанную роль
     */
    public boolean hasRole(Authentication authentication, String role) {
        if (authentication == null) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
    
    /**
     * Проверяет, является ли текущий пользователь пользователем с указанным ID.
     * 
     * @param authentication Текущая аутентификация
     * @param userId ID пользователя для проверки
     * @return true, если текущий пользователь имеет указанный ID
     */
    public boolean isSameUser(Authentication authentication, Long userId) {
        if (authentication == null || userId == null) {
            return false;
        }
        
        // Проверка наличия роли ADMIN
        if (hasRole(authentication, "ADMIN")) {
            return true;
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return false;
        }
        
        return user.getId().equals(userId);
    }
}