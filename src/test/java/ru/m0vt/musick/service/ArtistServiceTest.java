package ru.m0vt.musick.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
import ru.m0vt.musick.dto.AlbumBriefDTO;
import ru.m0vt.musick.dto.ArtistCreateDTO;
import ru.m0vt.musick.dto.ArtistCreateResponseDTO;
import ru.m0vt.musick.dto.ArtistUpdateDTO;
import ru.m0vt.musick.model.Album;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ArtistServiceTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AlbumService albumService;

    @Mock
    private Authentication authentication;

    @Mock
    private JwtUserDetailsService jwtUserDetailsService;

    @InjectMocks
    private ArtistService artistService;

    private Artist testArtist;
    private User testUser;
    private User subscriberUser;
    private ArtistCreateDTO createDTO;
    private ArtistUpdateDTO updateDTO;
    private Album testAlbum;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        // Setup subscriber user
        subscriberUser = new User();
        subscriberUser.setId(2L);
        subscriberUser.setUsername("subscriber");
        subscriberUser.setEmail("subscriber@example.com");

        // Setup test artist
        testArtist = new Artist();
        testArtist.setId(1L);
        testArtist.setName("Test Artist");
        testArtist.setBio("Test artist bio");
        testArtist.setUser(testUser);

        // Setup test album
        testAlbum = new Album();
        testAlbum.setId(1L);
        testAlbum.setTitle("Test Album");
        testAlbum.setArtist(testArtist);

        // Setup create DTO
        createDTO = new ArtistCreateDTO();
        createDTO.setName("New Artist");
        createDTO.setBio("New artist bio");

        // Setup update DTO
        updateDTO = new ArtistUpdateDTO();
        updateDTO.setName("Updated Artist");
        updateDTO.setBio("Updated artist bio");

        // Mock authentication
        when(authentication.getName()).thenReturn("testuser");
    }

    @Test
    void testGetAllArtists() {
        // Arrange
        when(artistRepository.findAll()).thenReturn(Arrays.asList(testArtist));

        // Act
        List<Artist> result = artistService.getAllArtists();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Artist", result.get(0).getName());
        assertEquals("Test artist bio", result.get(0).getBio());
    }

    @Test
    void testGetArtistById() {
        // Arrange
        when(artistRepository.findById(1L)).thenReturn(Optional.of(testArtist));

        // Act
        Artist result = artistService.getArtistById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Test Artist", result.getName());
        assertEquals("Test artist bio", result.getBio());
    }

    @Test
    void testGetArtistById_NotFound() {
        // Arrange
        when(artistRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertNull(artistService.getArtistById(999L));
    }

    @Test
    void testCreateArtist() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(
            Optional.of(testUser)
        );
        when(artistRepository.save(any(Artist.class))).thenReturn(testArtist);
        when(jwtService.generateToken(any())).thenReturn("test-jwt-token");

        // Act
        ArtistCreateResponseDTO result = artistService.createArtist(
            createDTO,
            authentication
        );

        // Assert
        assertNotNull(result);
        assertEquals("Test Artist", result.getArtist().getName());
        assertEquals("test-jwt-token", result.getToken());
        verify(artistRepository).save(any(Artist.class));
    }

    @Test
    void testUpdateArtistProfile() {
        // Arrange
        when(artistRepository.findById(1L)).thenReturn(Optional.of(testArtist));
        when(artistRepository.save(any(Artist.class))).thenReturn(testArtist);

        // Act
        Artist result = artistService.updateArtistProfile(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(artistRepository).save(testArtist);
    }

    @Test
    void testDeleteArtist() {
        // Arrange
        when(artistRepository.findById(1L)).thenReturn(Optional.of(testArtist));
        doNothing().when(artistRepository).deleteById(1L);

        // Act
        artistService.deleteArtist(1L);

        // Assert
        verify(artistRepository).deleteById(1L);
    }

    @Test
    void testGetArtistAlbums() {
        // Arrange
        when(artistRepository.findById(1L)).thenReturn(Optional.of(testArtist));
        when(albumRepository.findByArtist(testArtist)).thenReturn(
            Arrays.asList(testAlbum)
        );

        // Act
        List<AlbumBriefDTO> result = artistService.getArtistAlbums(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetArtistSubscribers() {
        // Arrange
        Subscription subscription = new Subscription();
        subscription.setId(1L);
        subscription.setUser(subscriberUser);
        subscription.setArtist(testArtist);

        when(artistRepository.findById(1L)).thenReturn(Optional.of(testArtist));
        when(
            subscriptionRepository.findByArtistId(testArtist.getId())
        ).thenReturn(Arrays.asList(subscription));

        // Act
        List<Subscription> result = artistService.getArtistSubscribers(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(subscriberUser, result.get(0).getUser());
        assertEquals(testArtist, result.get(0).getArtist());
    }

    @Test
    void testGetAlbumReviews() {
        // Arrange
        Review review = new Review();
        review.setId(1L);
        review.setText("Great album!");
        review.setUser(subscriberUser);
        review.setAlbum(testAlbum);

        when(albumRepository.findById(1L)).thenReturn(Optional.of(testAlbum));
        when(reviewRepository.findByAlbumId(testAlbum.getId())).thenReturn(
            Arrays.asList(review)
        );

        // Act
        List<Review> result = artistService.getAlbumReviews(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Great album!", result.get(0).getText());
        assertEquals(subscriberUser, result.get(0).getUser());
    }

    @Test
    void testSubscribeToArtist() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(
            Optional.of(subscriberUser)
        );
        when(artistRepository.findById(1L)).thenReturn(Optional.of(testArtist));
        when(
            subscriptionRepository.findByUserIdAndArtistId(
                subscriberUser.getId(),
                testArtist.getId()
            )
        ).thenReturn(Optional.empty());

        Subscription newSubscription = new Subscription();
        newSubscription.setId(1L);
        newSubscription.setUser(subscriberUser);
        newSubscription.setArtist(testArtist);

        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(
            newSubscription
        );

        // Act
        Subscription result = artistService.subscribeToArtist(
            1L,
            authentication
        );

        // Assert
        assertNotNull(result);
        assertEquals(subscriberUser, result.getUser());
        assertEquals(testArtist, result.getArtist());
    }

    @Test
    void testUnsubscribeFromArtist() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(
            Optional.of(subscriberUser)
        );
        when(artistRepository.findById(1L)).thenReturn(Optional.of(testArtist));

        Subscription subscription = new Subscription();
        subscription.setId(1L);
        subscription.setUser(subscriberUser);
        subscription.setArtist(testArtist);

        when(
            subscriptionRepository.findByUserIdAndArtistId(
                subscriberUser.getId(),
                testArtist.getId()
            )
        ).thenReturn(Optional.of(subscription));
        doNothing().when(subscriptionRepository).delete(subscription);

        // Act
        boolean result = artistService.unsubscribeFromArtist(
            1L,
            authentication
        );

        // Assert
        assertTrue(result);
        verify(subscriptionRepository).delete(subscription);
    }
}
