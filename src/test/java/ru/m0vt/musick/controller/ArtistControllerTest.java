package ru.m0vt.musick.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import ru.m0vt.musick.dto.ArtistCreateDTO;
import ru.m0vt.musick.dto.ArtistCreateResponseDTO;
import ru.m0vt.musick.dto.ArtistUpdateDTO;
import ru.m0vt.musick.model.Artist;
import ru.m0vt.musick.model.Review;
import ru.m0vt.musick.model.Subscription;
import ru.m0vt.musick.security.SecurityService;
import ru.m0vt.musick.service.ArtistService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ArtistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ArtistService artistService;

    @MockBean
    private SecurityService securityService;

    private Artist testArtist;
    private ArtistCreateResponseDTO artistCreateResponseDTO;
    private ArtistCreateDTO artistCreateDTO;
    private ArtistUpdateDTO artistUpdateDTO;

    @BeforeEach
    void setUp() {
        testArtist = new Artist();
        testArtist.setId(1L);
        testArtist.setName("Test Artist");
        testArtist.setBio("Test bio");

        artistCreateResponseDTO = new ArtistCreateResponseDTO();
        artistCreateResponseDTO.setArtist(testArtist);
        artistCreateResponseDTO.setToken("jwt-token-example");

        artistCreateDTO = new ArtistCreateDTO();
        artistCreateDTO.setName("New Artist");
        artistCreateDTO.setBio("New artist bio");

        artistUpdateDTO = new ArtistUpdateDTO();
        artistUpdateDTO.setName("Updated Artist");
        artistUpdateDTO.setBio("Updated bio");

        // Setup securityService for authenticated tests
        when(securityService.isSameUser(any(), eq(1L))).thenReturn(true);
    }

    @Test
    void testGetAllArtists() throws Exception {
        // Arrange
        List<Artist> artists = Arrays.asList(testArtist);
        when(artistService.getAllArtists()).thenReturn(artists);

        // Act & Assert
        mockMvc
            .perform(get("/artists"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Test Artist"))
            .andExpect(jsonPath("$[0].bio").value("Test bio"));
    }

    @Test
    void testGetArtistById() throws Exception {
        // Arrange
        when(artistService.getArtistById(1L)).thenReturn(testArtist);

        // Act & Assert
        mockMvc
            .perform(get("/artists/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Test Artist"))
            .andExpect(jsonPath("$.bio").value("Test bio"));
    }

    @Test
    void testGetArtistByIdNotFound() throws Exception {
        // Arrange
        when(artistService.getArtistById(999L)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/artists/999")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testCreateArtist() throws Exception {
        // Arrange
        when(
            artistService.createArtist(any(ArtistCreateDTO.class), any())
        ).thenReturn(artistCreateResponseDTO);

        // Act & Assert
        mockMvc
            .perform(
                post("/artists")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(artistCreateDTO))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.artist.name").value("Test Artist"))
            .andExpect(jsonPath("$.token").value("jwt-token-example"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testSubscribeToArtistWithMap() throws Exception {
        // Arrange
        Map<String, Boolean> result = new HashMap<>();
        result.put("success", true);
        when(artistService.subscribeToArtist(eq(1L), any())).thenReturn(
            new Subscription()
        );

        // Act & Assert
        mockMvc
            .perform(
                post("/artists/1/subscribe").contentType(
                    MediaType.APPLICATION_JSON
                )
            )
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testUpdateArtist() throws Exception {
        // Arrange
        when(securityService.getArtistUserId(1L)).thenReturn(1L);
        when(securityService.isSameUser(any(), eq(1L))).thenReturn(true);
        when(
            artistService.updateArtistProfile(
                eq(1L),
                any(ArtistUpdateDTO.class)
            )
        ).thenReturn(testArtist);

        // Act & Assert
        mockMvc
            .perform(
                put("/artists/1")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(artistUpdateDTO))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Test Artist"));
    }

    @Test
    @WithMockUser
    void testDeleteArtist() throws Exception {
        // Arrange
        when(securityService.isSameUser(any(), eq(1L))).thenReturn(true);
        doNothing().when(artistService).deleteArtist(1L);

        // Act & Assert
        mockMvc
            .perform(
                delete("/artists/1").with(
                    SecurityMockMvcRequestPostProcessors.csrf()
                )
            )
            .andExpect(status().isNoContent());
    }

    @Test
    void testGetArtistAlbums() throws Exception {
        // Arrange
        List<AlbumBriefDTO> albums = Arrays.asList(new AlbumBriefDTO());
        when(artistService.getArtistAlbums(1L)).thenReturn(albums);

        // Act & Assert
        mockMvc.perform(get("/artists/1/albums")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetArtistSubscribers() throws Exception {
        // Arrange
        when(securityService.isSameUser(any(), eq(1L))).thenReturn(true);
        List<Subscription> subscriptions = Arrays.asList(new Subscription());
        when(artistService.getArtistSubscribers(1L)).thenReturn(subscriptions);

        // Act & Assert
        mockMvc
            .perform(get("/artists/1/subscribers"))
            .andExpect(status().isOk());
    }

    @Test
    void testGetAlbumReviews() throws Exception {
        // Arrange
        List<Review> reviews = Arrays.asList(new Review());
        when(artistService.getAlbumReviews(1L)).thenReturn(reviews);

        // Act & Assert
        mockMvc
            .perform(get("/artists/1/albums/1/reviews"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testSubscribeToArtist() throws Exception {
        // Arrange
        when(artistService.subscribeToArtist(eq(1L), any())).thenReturn(
            new Subscription()
        );

        // Act & Assert
        mockMvc
            .perform(
                post("/artists/1/subscribe").with(
                    SecurityMockMvcRequestPostProcessors.csrf()
                )
            )
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUnsubscribeFromArtist() throws Exception {
        // Arrange
        when(artistService.unsubscribeFromArtist(eq(1L), any())).thenReturn(
            true
        );

        // Act & Assert
        mockMvc
            .perform(
                delete("/artists/1/unsubscribe").with(
                    SecurityMockMvcRequestPostProcessors.csrf()
                )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
}
