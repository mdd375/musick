package ru.m0vt.musick.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.m0vt.musick.dto.AuthRequestDTO;
import ru.m0vt.musick.dto.AuthResponseDTO;
import ru.m0vt.musick.dto.RegisterRequestDTO;
import ru.m0vt.musick.security.AuthenticationService;
import ru.m0vt.musick.security.SecurityService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private SecurityService securityService;

    private AuthResponseDTO mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = new AuthResponseDTO(
            "jwt-token-example",
            "testuser",
            "USER"
        );
    }

    @Test
    void testRegisterSuccess() throws Exception {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(
            authenticationService.register(any(RegisterRequestDTO.class))
        ).thenReturn(mockResponse);

        // Act & Assert
        mockMvc
            .perform(
                post("/auth/register")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token-example"))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void testRegisterWithInvalidInput() throws Exception {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername(""); // Invalid: blank username
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // Act & Assert
        mockMvc
            .perform(
                post("/auth/register")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginSuccess() throws Exception {
        // Arrange
        AuthRequestDTO request = new AuthRequestDTO("testuser", "password123");

        when(
            authenticationService.authenticate(any(AuthRequestDTO.class))
        ).thenReturn(mockResponse);

        // Act & Assert
        mockMvc
            .perform(
                post("/auth/login")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token-example"))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void testLoginWithInvalidInput() throws Exception {
        // Arrange
        AuthRequestDTO request = new AuthRequestDTO("", ""); // Invalid: blank username and password

        // Act & Assert
        mockMvc
            .perform(
                post("/auth/login")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());
    }
}
