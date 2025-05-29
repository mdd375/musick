package ru.m0vt.musick.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.m0vt.musick.dto.AlbumBriefDTO;
import ru.m0vt.musick.dto.AlbumDetailDTO;
import ru.m0vt.musick.dto.AuthRequestDTO;
import ru.m0vt.musick.dto.AuthResponseDTO;
import ru.m0vt.musick.model.Artist;
import ru.m0vt.musick.security.AuthenticationService;
import ru.m0vt.musick.security.SecurityService;
import ru.m0vt.musick.service.AlbumService;
import ru.m0vt.musick.service.ArtistService;
import ru.m0vt.musick.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private AlbumService albumService;

    @MockBean
    private ArtistService artistService;

    @MockBean
    private UserService userService;

    @MockBean
    private SecurityService securityService;

    @Test
    public void testAuthLogin() throws Exception {
        // Arrange
        AuthRequestDTO request = new AuthRequestDTO("testuser", "password123");
        AuthResponseDTO response = new AuthResponseDTO("jwt-token-123", "testuser", "USER");
        
        when(authenticationService.authenticate(any(AuthRequestDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    public void testGetAllAlbums() throws Exception {
        // Arrange
        AlbumBriefDTO album = new AlbumBriefDTO();
        album.setId(1L);
        album.setTitle("Test Album");
        album.setArtistName("Test Artist");
        
        List<AlbumBriefDTO> albums = Arrays.asList(album);
        
        when(albumService.getAllAlbums()).thenReturn(albums);

        // Act & Assert
        mockMvc.perform(get("/albums"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Album"))
                .andExpect(jsonPath("$[0].artistName").value("Test Artist"));
    }

    @Test
    public void testGetAlbumById() throws Exception {
        // Arrange
        AlbumDetailDTO album = new AlbumDetailDTO();
        album.setId(1L);
        album.setTitle("Test Album");
        album.setArtistName("Test Artist");
        album.setPrice(new BigDecimal("9.99"));
        album.setReleaseDate(LocalDate.of(2023, 1, 1));
        
        when(albumService.getAlbumById(eq(1L))).thenReturn(album);

        // Act & Assert
        mockMvc.perform(get("/albums/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Album"))
                .andExpect(jsonPath("$.artistName").value("Test Artist"));
    }

    @Test
    public void testGetAllArtists() throws Exception {
        // Arrange
        Artist artist = new Artist();
        artist.setId(1L);
        artist.setName("Test Artist");
        
        List<Artist> artists = Arrays.asList(artist);
        
        when(artistService.getAllArtists()).thenReturn(artists);

        // Act & Assert
        mockMvc.perform(get("/artists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Artist"));
    }

    @Test
    @WithMockUser
    public void testGetCurrentUserInfo() throws Exception {
        // This just tests if the endpoint is accessible when authenticated
        // The actual service implementation is mocked
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk());
    }
}