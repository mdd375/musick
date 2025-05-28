package ru.m0vt.musick.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.m0vt.musick.dto.AddBalanceDTO;
import ru.m0vt.musick.dto.UserCreateDTO;
import ru.m0vt.musick.exception.BadRequestException;
import ru.m0vt.musick.exception.ResourceNotFoundException;
import ru.m0vt.musick.exception.SubscriptionConflictException;
import ru.m0vt.musick.exception.UserAlreadyExistsException;
import ru.m0vt.musick.model.*;
import ru.m0vt.musick.repository.*;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private ArtistRepository artistRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User createUser(UserCreateDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPasswordHash(passwordEncoder.encode(userDTO.getPassword())); // Хешируем пароль
        user.setRole(userDTO.getRole());
        user.setCreatedAt(LocalDateTime.now());
        user.setBalance(java.math.BigDecimal.ZERO); // Устанавливаем нулевой баланс

        return userRepository.save(user);
    }

    public User updateUser(Long id, User user) {
        User existingUser = userRepository.findById(id).orElse(null);
        if (existingUser != null) {
            existingUser.setUsername(user.getUsername());
            existingUser.setEmail(user.getEmail());
            existingUser.setPasswordHash(user.getPasswordHash());
            return userRepository.save(existingUser);
        }
        return null;
    }

    public List<Purchase> getUserPurchases(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return purchaseRepository.findByUserId(user.getId());
    }

    public List<Review> getUserReviews(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return reviewRepository.findByUserId(user.getId());
    }

    public List<Subscription> getUserSubscriptions(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return subscriptionRepository.findByUserId(user.getId());
    }

    public Subscription addUserSubscription(Long artistId, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Проверяем, что артист существует
        if (artistId == null) {
            throw new BadRequestException("Artist ID cannot be null");
        }
        
        Artist foundArtist = artistRepository.findById(artistId)
                .orElseThrow(() -> new ResourceNotFoundException("Artist not found"));
        
        // Проверяем, не пытается ли пользователь подписаться на себя
        if (foundArtist.getUser() != null && foundArtist.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Cannot subscribe to yourself");
        }
        
        // Проверяем, существует ли уже подписка
        boolean subscriptionExists = subscriptionRepository.existsByUserIdAndArtistId(user.getId(), artistId);
        if (subscriptionExists) {
            throw new SubscriptionConflictException("You are already subscribed to this artist");
        }
                
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setArtist(foundArtist);
        subscription.setCreatedAt(LocalDateTime.now());
        return subscriptionRepository.save(subscription);
    }
    
    /**
     * Пополняет баланс пользователя на указанную сумму
     * 
     * @param addBalanceDTO DTO с суммой для пополнения
     * @param authentication Объект аутентификации для получения текущего пользователя
     * @return Обновленный объект пользователя с новым балансом
     */
    @Transactional
    public User addBalance(AddBalanceDTO addBalanceDTO, Authentication authentication) {
        if (addBalanceDTO == null || addBalanceDTO.getAmount() == null) {
            throw new BadRequestException("Amount cannot be null");
        }
        
        if (addBalanceDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be positive");
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Обновляем баланс пользователя
        BigDecimal currentBalance = user.getBalance();
        if (currentBalance == null) {
            currentBalance = BigDecimal.ZERO;
        }
        
        BigDecimal newBalance = currentBalance.add(addBalanceDTO.getAmount());
        user.setBalance(newBalance);
        
        return userRepository.save(user);
    }
}
