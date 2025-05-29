package ru.m0vt.musick.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.m0vt.musick.dto.AddBalanceDTO;
import ru.m0vt.musick.dto.PasswordChangeDTO;
import ru.m0vt.musick.dto.UserInfoDTO;
import ru.m0vt.musick.dto.UserUpdateDTO;
import ru.m0vt.musick.model.Purchase;
import ru.m0vt.musick.model.Review;
import ru.m0vt.musick.model.Subscription;
import ru.m0vt.musick.model.User;
import ru.m0vt.musick.repository.PurchaseRepository;
import ru.m0vt.musick.repository.ReviewRepository;
import ru.m0vt.musick.repository.SubscriptionRepository;
import ru.m0vt.musick.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserUpdateDTO updateDTO;
    private PasswordChangeDTO passwordDTO;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("encodedPassword");
        testUser.setBalance(new BigDecimal("100.00"));

        // Setup update DTO
        updateDTO = new UserUpdateDTO();
        updateDTO.setUsername("updateduser");
        updateDTO.setEmail("updated@example.com");

        // Setup password change DTO
        passwordDTO = new PasswordChangeDTO();
        passwordDTO.setCurrentPassword("password");
        passwordDTO.setNewPassword("newPassword");
        passwordDTO.setConfirmPassword("newPassword");
        // Mock authentication
        when(authentication.getName()).thenReturn("testuser");
    }

    @Test
    void testGetAllUsers() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
    }

    @Test
    void testGetUserById() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void testUpdateUserProfile() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUserProfile(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(testUser);
    }

    @Test
    void testChangePassword() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(
            true
        );
        when(passwordEncoder.encode("newPassword")).thenReturn(
            "encodedNewPassword"
        );
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.changePassword(1L, passwordDTO);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(testUser);
    }

    @Test
    void testDeleteUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).deleteById(1L);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository).deleteById(1L);
    }

    @Test
    void testGetUserPurchases() {
        // Arrange
        Purchase purchase = new Purchase();
        purchase.setId(1L);
        purchase.setUser(testUser);

        when(userRepository.findByUsername("testuser")).thenReturn(
            Optional.of(testUser)
        );
        when(purchaseRepository.findByUserId(testUser.getId())).thenReturn(
            Arrays.asList(purchase)
        );

        // Act
        List<Purchase> result = userService.getUserPurchases(authentication);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0).getUser());
    }

    @Test
    void testGetUserReviews() {
        // Arrange
        Review review = new Review();
        review.setId(1L);
        review.setUser(testUser);

        when(userRepository.findByUsername("testuser")).thenReturn(
            Optional.of(testUser)
        );
        when(reviewRepository.findByUserId(testUser.getId())).thenReturn(
            Arrays.asList(review)
        );

        // Act
        List<Review> result = userService.getUserReviews(authentication);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0).getUser());
    }

    @Test
    void testGetCurrentUserInfo() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(
            Optional.of(testUser)
        );

        // Act
        UserInfoDTO result = userService.getCurrentUserInfo(authentication);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(new BigDecimal("100.00"), result.getBalance());
    }

    @Test
    void testGetUserSubscriptions() {
        // Arrange
        Subscription subscription = new Subscription();
        subscription.setId(1L);
        subscription.setUser(testUser);

        when(userRepository.findByUsername("testuser")).thenReturn(
            Optional.of(testUser)
        );
        when(subscriptionRepository.findByUserId(testUser.getId())).thenReturn(
            Arrays.asList(subscription)
        );

        // Act
        List<Subscription> result = userService.getUserSubscriptions(
            authentication
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0).getUser());
    }

    @Test
    void testAddBalance() {
        // Arrange
        AddBalanceDTO balanceDTO = new AddBalanceDTO();
        balanceDTO.setAmount(new BigDecimal("50.00"));

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("testuser");
        updatedUser.setEmail("test@example.com");
        updatedUser.setBalance(new BigDecimal("150.00"));

        when(userRepository.findByUsername("testuser")).thenReturn(
            Optional.of(testUser)
        );
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        User result = userService.addBalance(balanceDTO, authentication);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("150.00"), result.getBalance());
        verify(userRepository).save(testUser);
    }
}
