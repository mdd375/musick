package ru.m0vt.musick.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.m0vt.musick.dto.AlbumBriefDTO;
import ru.m0vt.musick.dto.AlbumCreateDTO;
import ru.m0vt.musick.dto.AlbumDetailDTO;
import ru.m0vt.musick.dto.AlbumUpdateDTO;
import ru.m0vt.musick.dto.ReviewCreateDTO;
import ru.m0vt.musick.dto.TrackCreateDTO;
import ru.m0vt.musick.model.AlbumTag;
import ru.m0vt.musick.model.Purchase;
import ru.m0vt.musick.model.Review;
import ru.m0vt.musick.model.Track;
import ru.m0vt.musick.security.SecurityService;
import ru.m0vt.musick.service.AlbumService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AlbumControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AlbumService albumService;

    @MockBean
    private SecurityService securityService;

    private AlbumBriefDTO albumBriefDTO;
    private AlbumDetailDTO albumDetailDTO;
    private AlbumCreateDTO albumCreateDTO;
    private AlbumUpdateDTO albumUpdateDTO;

    @BeforeEach
    void setUp() {
        albumBriefDTO = new AlbumBriefDTO();
        albumBriefDTO.setId(1L);
        albumBriefDTO.setTitle("Test Album");
        albumBriefDTO.setArtistName("Test Artist");

        albumDetailDTO = new AlbumDetailDTO();
        albumDetailDTO.setId(1L);
        albumDetailDTO.setTitle("Test Album");
        albumDetailDTO.setArtistName("Test Artist");
        albumDetailDTO.setPrice(new BigDecimal("9.99"));
        albumDetailDTO.setReleaseDate(LocalDate.of(2023, 1, 1));

        albumCreateDTO = new AlbumCreateDTO();
        albumCreateDTO.setTitle("New Album");
        albumCreateDTO.setPrice(new BigDecimal("14.99"));
        albumCreateDTO.setReleaseDate(LocalDate.of(2023, 5, 5));

        albumUpdateDTO = new AlbumUpdateDTO();
        albumUpdateDTO.setTitle("Updated Album");
        albumUpdateDTO.setPrice(new BigDecimal("19.99"));
        albumUpdateDTO.setCoverUrl("cover-url.jpg");
        albumUpdateDTO.setReleaseDate(LocalDate.of(2023, 6, 6));

        // Setup securityService for authenticated tests
        when(securityService.isAlbumOwner(any(), anyLong())).thenReturn(true);
        when(securityService.canPurchaseAlbum(any())).thenReturn(true);
        when(securityService.canWriteReview(any())).thenReturn(true);
    }

    @Test
    void testGetAllAlbums() throws Exception {
        // Arrange
        List<AlbumBriefDTO> albums = Arrays.asList(albumBriefDTO);
        when(albumService.getAllAlbums()).thenReturn(albums);

        // Act & Assert
        mockMvc
            .perform(get("/albums"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("Test Album"))
            .andExpect(jsonPath("$[0].artistName").value("Test Artist"));
    }

    @Test
    void testGetAlbumById() throws Exception {
        // Arrange
        when(albumService.getAlbumById(1L)).thenReturn(albumDetailDTO);

        // Act & Assert
        mockMvc
            .perform(get("/albums/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Test Album"))
            .andExpect(jsonPath("$.artistName").value("Test Artist"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateAlbum() throws Exception {
        // Arrange
        when(
            albumService.createAlbum(any(AlbumCreateDTO.class), any())
        ).thenReturn(albumDetailDTO);

        // Act & Assert
        mockMvc
            .perform(
                post("/albums")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(albumCreateDTO))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Test Album"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateAlbum() throws Exception {
        // Arrange
        when(
            albumService.updateAlbum(eq(1L), any(AlbumUpdateDTO.class))
        ).thenReturn(albumDetailDTO);

        // Act & Assert
        mockMvc
            .perform(
                put("/albums/1")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(albumUpdateDTO))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Test Album"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteAlbum() throws Exception {
        // Arrange
        doNothing().when(albumService).deleteAlbum(1L);

        // Act & Assert
        mockMvc
            .perform(
                delete("/albums/1").with(
                    SecurityMockMvcRequestPostProcessors.csrf()
                )
            )
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void testPurchaseAlbum() throws Exception {
        // Arrange
        when(albumService.purchaseAlbum(eq(1L), any())).thenReturn(
            new Purchase()
        );

        // Act & Assert
        mockMvc
            .perform(
                post("/albums/1/purchase").with(
                    SecurityMockMvcRequestPostProcessors.csrf()
                )
            )
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddTagToAlbum() throws Exception {
        // Arrange
        when(albumService.addTagToAlbum(eq(1L), anyString())).thenReturn(
            new AlbumTag()
        );

        // Act & Assert
        mockMvc
            .perform(
                post("/albums/1/tags")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .content("rock")
            )
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRemoveTagFromAlbum() throws Exception {
        // Arrange
        doNothing().when(albumService).removeTagFromAlbum(eq(1L), anyLong());

        // Act & Assert
        mockMvc
            .perform(
                delete("/albums/1/tags/1").with(
                    SecurityMockMvcRequestPostProcessors.csrf()
                )
            )
            .andExpect(status().isNoContent());
    }

    @Test
    void testGetReviewsForAlbum() throws Exception {
        // Arrange
        List<Review> reviews = Arrays.asList(new Review());
        when(albumService.getReviewsForAlbum(1L)).thenReturn(reviews);

        // Act & Assert
        mockMvc.perform(get("/albums/1/reviews")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddReviewToAlbum() throws Exception {
        // Arrange
        ReviewCreateDTO reviewDTO = new ReviewCreateDTO();
        reviewDTO.setText("Great album!");
        reviewDTO.setFavoriteTracks("Track 1, Track 3");

        when(
            albumService.addReviewToAlbum(
                eq(1L),
                any(ReviewCreateDTO.class),
                any()
            )
        ).thenReturn(new Review());

        // Act & Assert
        mockMvc
            .perform(
                post("/albums/1/reviews")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewDTO))
            )
            .andExpect(status().isOk());
    }

    @Test
    void testGetTracksForAlbum() throws Exception {
        // Arrange
        List<Track> tracks = Arrays.asList(new Track());
        when(albumService.getTracksForAlbum(1L)).thenReturn(tracks);

        // Act & Assert
        mockMvc.perform(get("/albums/1/tracks")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddTrackToAlbum() throws Exception {
        // Arrange
        TrackCreateDTO trackDTO = new TrackCreateDTO();
        trackDTO.setTitle("New Track");
        trackDTO.setDurationSec(240);
        trackDTO.setAudioUrl("track-url.mp3");

        when(
            albumService.addTrackToAlbum(eq(1L), any(TrackCreateDTO.class))
        ).thenReturn(new Track());

        // Act & Assert
        mockMvc
            .perform(
                post("/albums/1/tracks")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(trackDTO))
            )
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRemoveTrackFromAlbum() throws Exception {
        // Arrange
        when(albumService.removeTrackFromAlbum(eq(1L), eq(1))).thenReturn(
            Arrays.asList(new Track())
        );

        // Act & Assert
        mockMvc
            .perform(
                delete("/albums/1/tracks/1").with(
                    SecurityMockMvcRequestPostProcessors.csrf()
                )
            )
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testMoveTrackPosition() throws Exception {
        // Arrange
        when(albumService.moveTrackPosition(eq(1L), eq(1), eq(2))).thenReturn(
            Arrays.asList(new Track())
        );

        // Act & Assert
        mockMvc
            .perform(
                put("/albums/1/tracks/1/move/2").with(
                    SecurityMockMvcRequestPostProcessors.csrf()
                )
            )
            .andExpect(status().isOk());
    }
}
