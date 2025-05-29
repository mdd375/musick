package ru.m0vt.musick.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import java.math.BigDecimal;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.m0vt.musick.dto.AddBalanceDTO;
import ru.m0vt.musick.dto.PasswordChangeDTO;
import ru.m0vt.musick.dto.UserInfoDTO;
import ru.m0vt.musick.dto.UserUpdateDTO;
import ru.m0vt.musick.model.Purchase;
import ru.m0vt.musick.model.Review;
import ru.m0vt.musick.model.Subscription;
import ru.m0vt.musick.model.User;
import ru.m0vt.musick.security.SecurityService;
import ru.m0vt.musick.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;
    
    @MockBean
    private SecurityService securityService;

    private User testUser;
    private UserInfoDTO userInfoDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        
        userInfoDTO = new UserInfoDTO();
        userInfoDTO.setId(1L);
        userInfoDTO.setUsername("testuser");
        userInfoDTO.setEmail("test@example.com");
        userInfoDTO.setBalance(new BigDecimal("100.00"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllUsers() throws Exception {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userService.getAllUsers()).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[0].email").value("test@example.com"));
    }

    @Test
    @WithMockUser
    void testGetUserById() throws Exception {
        // Arrange
        when(securityService.isSameUser(any(), eq(1L))).thenReturn(true);
        when(userService.getUserById(1L)).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser
    void testUpdateUser() throws Exception {
        // Arrange
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setUsername("johndoe");
        updateDTO.setEmail("john.doe@example.com");
        
        when(securityService.isSameUser(any(), eq(1L))).thenReturn(true);
        when(userService.updateUserProfile(eq(1L), any(UserUpdateDTO.class))).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(put("/users/1")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testChangePassword() throws Exception {
        // Arrange
        PasswordChangeDTO passwordDTO = new PasswordChangeDTO();
        passwordDTO.setCurrentPassword("oldPassword");
        passwordDTO.setNewPassword("newPassword");
        passwordDTO.setConfirmPassword("newPassword");
        
        when(securityService.isSameUser(any(), eq(1L))).thenReturn(true);
        when(userService.changePassword(eq(1L), any(PasswordChangeDTO.class))).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(put("/users/1/password")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testDeleteUser() throws Exception {
        // Arrange
        when(securityService.isSameUser(any(), eq(1L))).thenReturn(true);
        doNothing().when(userService).deleteUser(1L);

        // Act & Assert
        mockMvc.perform(delete("/users/1")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void testGetUserPurchases() throws Exception {
        // Arrange
        List<Purchase> purchases = Arrays.asList(new Purchase());
        when(userService.getUserPurchases(any())).thenReturn(purchases);

        // Act & Assert
        mockMvc.perform(get("/users/me/purchases"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetUserReviews() throws Exception {
        // Arrange
        List<Review> reviews = Arrays.asList(new Review());
        when(userService.getUserReviews(any())).thenReturn(reviews);

        // Act & Assert
        mockMvc.perform(get("/users/me/reviews"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetCurrentUserInfo() throws Exception {
        // Arrange
        when(userService.getCurrentUserInfo(any())).thenReturn(userInfoDTO);

        // Act & Assert
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.balance").value(100.0));
    }

    @Test
    @WithMockUser
    void testGetUserSubscriptions() throws Exception {
        // Arrange
        List<Subscription> subscriptions = Arrays.asList(new Subscription());
        when(userService.getUserSubscriptions(any())).thenReturn(subscriptions);

        // Act & Assert
        mockMvc.perform(get("/users/me/subscriptions"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testAddBalance() throws Exception {
        // Arrange
        AddBalanceDTO balanceDTO = new AddBalanceDTO();
        balanceDTO.setAmount(new BigDecimal("50.00"));
        
        when(userService.addBalance(any(AddBalanceDTO.class), any())).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/users/me/balance")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(balanceDTO)))
                .andExpect(status().isOk());
    }
}