package ru.m0vt.musick.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
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
import ru.m0vt.musick.dto.AlbumCreateDTO;
import ru.m0vt.musick.dto.AlbumDetailDTO;
import ru.m0vt.musick.dto.AlbumUpdateDTO;
import ru.m0vt.musick.dto.ReviewCreateDTO;
import ru.m0vt.musick.dto.TrackCreateDTO;
import ru.m0vt.musick.model.Album;
import ru.m0vt.musick.model.AlbumTag;
import ru.m0vt.musick.model.Artist;
import ru.m0vt.musick.model.Review;
import ru.m0vt.musick.model.Tag;
import ru.m0vt.musick.model.Track;
import ru.m0vt.musick.model.User;
import ru.m0vt.musick.repository.AlbumRepository;
import ru.m0vt.musick.repository.AlbumTagRepository;
import ru.m0vt.musick.repository.ArtistRepository;
import ru.m0vt.musick.repository.PurchaseRepository;
import ru.m0vt.musick.repository.ReviewRepository;
import ru.m0vt.musick.repository.TagRepository;
import ru.m0vt.musick.repository.TrackRepository;
import ru.m0vt.musick.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AlbumServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private AlbumTagRepository albumTagRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private TrackRepository trackRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AlbumService albumService;

    private Album testAlbum;
    private User testUser;
    private Artist testArtist;
    private AlbumCreateDTO createDTO;
    private AlbumUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setBalance(new BigDecimal("100.00"));

        // Setup test artist
        testArtist = new Artist();
        testArtist.setId(1L);
        testArtist.setName("Test Artist");
        testArtist.setUser(testUser);

        // Link artist back to user
        testUser.setArtistProfile(testArtist);

        // Setup test album
        testAlbum = new Album();
        testAlbum.setId(1L);
        testAlbum.setTitle("Test Album");
        testAlbum.setArtist(testArtist);
        testAlbum.setPrice(new BigDecimal("9.99"));
        testAlbum.setReleaseDate(LocalDate.of(2023, 1, 1));
        testAlbum.setCoverUrl("cover-url.jpg");
        testAlbum.setTracks(Collections.emptyList());
        testAlbum.setReviews(Collections.emptyList());
        testAlbum.setTags(Collections.emptyList());

        // Setup create DTO
        createDTO = new AlbumCreateDTO();
        createDTO.setTitle("New Album");
        createDTO.setPrice(new BigDecimal("14.99"));
        createDTO.setReleaseDate(LocalDate.of(2023, 5, 5));
        createDTO.setCoverUrl("new-cover-url.jpg");

        // Setup update DTO
        updateDTO = new AlbumUpdateDTO();
        updateDTO.setTitle("Updated Album");
        updateDTO.setPrice(new BigDecimal("19.99"));
        updateDTO.setCoverUrl("updated-cover-url.jpg");
        updateDTO.setReleaseDate(LocalDate.of(2023, 6, 6));

        // Mock authentication
        when(authentication.getName()).thenReturn("testuser");
    }

    @Test
    void testGetAllAlbums() {
        // Arrange
        when(albumRepository.findAll()).thenReturn(Arrays.asList(testAlbum));

        // Act
        List<AlbumBriefDTO> result = albumService.getAllAlbums();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Album", result.get(0).getTitle());
        assertEquals("Test Artist", result.get(0).getArtistName());
    }

    @Test
    void testGetAlbumById() {
        // Arrange
        when(albumRepository.findById(1L)).thenReturn(Optional.of(testAlbum));

        // Act
        AlbumDetailDTO result = albumService.getAlbumById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Test Album", result.getTitle());
        assertEquals("Test Artist", result.getArtistName());
        assertEquals(new BigDecimal("9.99"), result.getPrice());
    }

    @Test
    void testCreateAlbum() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(
            Optional.of(testUser)
        );
        when(albumRepository.save(any(Album.class))).thenReturn(testAlbum);

        // Act
        AlbumDetailDTO result = albumService.createAlbum(
            createDTO,
            authentication
        );

        // Assert
        assertNotNull(result);
        assertEquals("Test Album", result.getTitle());
        assertEquals("Test Artist", result.getArtistName());

        verify(albumRepository).save(any(Album.class));
    }

    @Test
    void testUpdateAlbum() {
        // Arrange
        when(albumRepository.findById(1L)).thenReturn(Optional.of(testAlbum));
        when(albumRepository.save(any(Album.class))).thenReturn(testAlbum);

        // Act
        AlbumDetailDTO result = albumService.updateAlbum(1L, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Album", result.getTitle());

        verify(albumRepository).save(testAlbum);
    }

    @Test
    void testDeleteAlbum() {
        // Arrange
        when(albumRepository.findById(1L)).thenReturn(Optional.of(testAlbum));
        doNothing().when(albumRepository).deleteById(1L);

        // Set up empty tracks collection to avoid NPE
        testAlbum.setTracks(Collections.emptyList());

        // Act
        albumService.deleteAlbum(1L);

        // Assert
        verify(albumRepository).deleteById(1L);
    }

    @Test
    void testAddTagToAlbum() {
        // Arrange
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("rock");

        AlbumTag albumTag = new AlbumTag();
        albumTag.setAlbum(testAlbum);
        albumTag.setTag(tag);

        when(albumRepository.findById(1L)).thenReturn(Optional.of(testAlbum));
        when(tagRepository.findByName("rock")).thenReturn(tag);
        when(albumTagRepository.save(any(AlbumTag.class))).thenReturn(albumTag);

        // Act
        AlbumTag result = albumService.addTagToAlbum(1L, "rock");

        // Assert
        assertNotNull(result);
        assertEquals(tag, result.getTag());
        assertEquals(testAlbum, result.getAlbum());
    }

    @Test
    void testRemoveTagFromAlbum() {
        // Arrange
        AlbumTag albumTag = new AlbumTag();
        albumTag.setAlbum(testAlbum);

        when(albumTagRepository.findByAlbumIdAndTagId(1L, 1L)).thenReturn(
            albumTag
        );
        doNothing().when(albumTagRepository).delete(albumTag);

        // Act
        albumService.removeTagFromAlbum(1L, 1L);

        // Assert
        verify(albumTagRepository).delete(albumTag);
    }

    @Test
    void testGetReviewsForAlbum() {
        // Arrange
        Review review = new Review();
        review.setId(1L);
        review.setText("Great album!");
        review.setUser(testUser);
        review.setAlbum(testAlbum);

        testAlbum.setReviews(Arrays.asList(review));

        when(albumRepository.findById(1L)).thenReturn(Optional.of(testAlbum));

        // Act
        List<Review> result = albumService.getReviewsForAlbum(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Great album!", result.get(0).getText());
    }

    @Test
    void testAddReviewToAlbum() {
        // Arrange
        ReviewCreateDTO reviewDTO = new ReviewCreateDTO();
        reviewDTO.setText("Amazing album!");
        reviewDTO.setFavoriteTracks("Track 1, Track 3");

        Review review = new Review();
        review.setId(1L);
        review.setText("Amazing album!");
        review.setUser(testUser);
        review.setAlbum(testAlbum);

        when(albumRepository.findById(1L)).thenReturn(Optional.of(testAlbum));
        when(userRepository.findByUsername(eq("testuser"))).thenReturn(
            Optional.of(testUser)
        );
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // Act
        Review result = albumService.addReviewToAlbum(
            1L,
            reviewDTO,
            authentication
        );

        // Assert
        assertNotNull(result);
        assertEquals("Amazing album!", result.getText());
        assertEquals(testUser, result.getUser());
        assertEquals(testAlbum, result.getAlbum());
    }

    @Test
    void testGetTracksForAlbum() {
        // Arrange
        Track track = new Track();
        track.setId(1L);
        track.setTitle("Track 1");
        track.setTrackNumber(1);
        track.setDurationSec(180);
        track.setAlbum(testAlbum);

        testAlbum.setTracks(Arrays.asList(track));

        when(albumRepository.findById(1L)).thenReturn(Optional.of(testAlbum));

        // Act
        List<Track> result = albumService.getTracksForAlbum(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Track 1", result.get(0).getTitle());
    }

    @Test
    void testAddTrackToAlbum() {
        // Arrange
        TrackCreateDTO trackDTO = new TrackCreateDTO();
        trackDTO.setTitle("New Track");
        trackDTO.setDurationSec(240);
        trackDTO.setAudioUrl("audio-url.mp3");

        Track track = new Track();
        track.setId(1L);
        track.setTitle("New Track");
        track.setTrackNumber(1);
        track.setDurationSec(240);
        track.setAudioUrl("audio-url.mp3");
        track.setAlbum(testAlbum);

        when(albumRepository.findById(1L)).thenReturn(Optional.of(testAlbum));
        when(trackRepository.save(any(Track.class))).thenReturn(track);

        // Act
        Track result = albumService.addTrackToAlbum(1L, trackDTO);

        // Assert
        assertNotNull(result);
        assertEquals("New Track", result.getTitle());
        assertEquals(240, result.getDurationSec());
        assertEquals(1, result.getTrackNumber());
    }
}
