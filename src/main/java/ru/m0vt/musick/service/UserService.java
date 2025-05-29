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
import ru.m0vt.musick.dto.PasswordChangeDTO;
import ru.m0vt.musick.dto.UserCreateDTO;
import ru.m0vt.musick.dto.UserInfoDTO;
import ru.m0vt.musick.dto.UserUpdateDTO;
import ru.m0vt.musick.exception.BadRequestException;
import ru.m0vt.musick.exception.ResourceNotFoundException;
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

    /**
     * Обновляет профиль пользователя (имя и email)
     *
     * @param id ID пользователя
     * @param userUpdateDTO DTO с новыми данными пользователя
     * @return Обновленный объект пользователя
     */
    @Transactional
    public User updateUserProfile(Long id, UserUpdateDTO userUpdateDTO) {
        User existingUser = userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Проверяем, не занято ли имя пользователя другим пользователем
        if (
            !existingUser.getUsername().equals(userUpdateDTO.getUsername()) &&
            userRepository.existsByUsername(userUpdateDTO.getUsername())
        ) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        // Проверяем, не занят ли email другим пользователем
        if (
            !existingUser.getEmail().equals(userUpdateDTO.getEmail()) &&
            userRepository.existsByEmail(userUpdateDTO.getEmail())
        ) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        existingUser.setUsername(userUpdateDTO.getUsername());
        existingUser.setEmail(userUpdateDTO.getEmail());

        return userRepository.save(existingUser);
    }

    /**
     * Изменяет пароль пользователя
     *
     * @param userId ID пользователя
     * @param passwordChangeDTO DTO с текущим и новым паролем
     * @return Обновленный объект пользователя
     */
    @Transactional
    public User changePassword(
        Long userId,
        PasswordChangeDTO passwordChangeDTO
    ) {
        User user = userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Проверяем, совпадает ли текущий пароль
        if (
            !passwordEncoder.matches(
                passwordChangeDTO.getCurrentPassword(),
                user.getPasswordHash()
            )
        ) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Проверяем, совпадают ли новый пароль и его подтверждение
        if (
            !passwordChangeDTO
                .getNewPassword()
                .equals(passwordChangeDTO.getConfirmPassword())
        ) {
            throw new BadRequestException(
                "New password and confirmation do not match"
            );
        }

        // Обновляем пароль
        user.setPasswordHash(
            passwordEncoder.encode(passwordChangeDTO.getNewPassword())
        );

        return userRepository.save(user);
    }

    public List<Purchase> getUserPurchases(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return purchaseRepository.findByUserId(user.getId());
    }

    public List<Review> getUserReviews(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return reviewRepository.findByUserId(user.getId());
    }

    public List<Subscription> getUserSubscriptions(
        Authentication authentication
    ) {
        String username = authentication.getName();
        User user = userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return subscriptionRepository.findByUserId(user.getId());
    }

    /**
     * Получает информацию о текущем пользователе
     *
     * @param authentication Объект аутентификации для получения текущего пользователя
     * @return UserInfoDTO с информацией о пользователе
     */
    public UserInfoDTO getCurrentUserInfo(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return convertToUserInfoDTO(user);
    }

    /**
     * Преобразует объект User в UserInfoDTO
     *
     * @param user Объект пользователя
     * @return UserInfoDTO с данными пользователя
     */
    public UserInfoDTO convertToUserInfoDTO(User user) {
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setId(user.getId());
        userInfoDTO.setUsername(user.getUsername());
        userInfoDTO.setEmail(user.getEmail());
        userInfoDTO.setBalance(user.getBalance());
        userInfoDTO.setArtist(user.getArtistProfile() != null);

        return userInfoDTO;
    }

    /**
     * Пополняет баланс пользователя на указанную сумму
     *
     * @param addBalanceDTO DTO с суммой для пополнения
     * @param authentication Объект аутентификации для получения текущего пользователя
     * @return Обновленный объект пользователя с новым балансом
     */
    @Transactional
    public User addBalance(
        AddBalanceDTO addBalanceDTO,
        Authentication authentication
    ) {
        if (addBalanceDTO == null || addBalanceDTO.getAmount() == null) {
            throw new BadRequestException("Amount cannot be null");
        }

        if (addBalanceDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be positive");
        }

        String username = authentication.getName();
        User user = userRepository
            .findByUsername(username)
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
