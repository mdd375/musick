package ru.m0vt.musick.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import ru.m0vt.musick.dto.AuthRequestDTO;
import ru.m0vt.musick.dto.AuthResponseDTO;
import ru.m0vt.musick.dto.RegisterRequestDTO;
import ru.m0vt.musick.model.User;
import ru.m0vt.musick.repository.UserRepository;

import java.time.LocalDateTime;

@Service
public class AuthenticationService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUserDetailsService userDetailsService;
    
    public AuthResponseDTO register(RegisterRequestDTO request) {
        // Проверяем, существует ли пользователь
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }
        
        // Если роль - ADMIN, проверяем права текущего пользователя
        if ("ADMIN".equalsIgnoreCase(request.getRole())) {
            // Получаем текущего пользователя, если он есть
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = false;
            
            if (authentication != null && authentication.isAuthenticated() && 
                    !"anonymousUser".equals(authentication.getPrincipal())) {
                isAdmin = authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            }
            
            // Если текущий пользователь не админ, отказываем в создании админа
            if (!isAdmin) {
                throw new RuntimeException("Only administrators can create users with ADMIN role");
            }
        }
        
        // Создаем нового пользователя
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        // Для не-админов разрешаем только роли USER и ARTIST
        user.setRole("ADMIN".equalsIgnoreCase(request.getRole()) ? "ADMIN" : request.getRole());
        user.setCreatedAt(LocalDateTime.now());
        // Устанавливаем начальный баланс
        user.setBalance(java.math.BigDecimal.ZERO);
        
        // Сохраняем пользователя
        userRepository.save(user);
        
        // Загружаем данные пользователя для JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        
        // Генерируем JWT токен
        String token = jwtService.generateToken(userDetails);
        
        // Возвращаем ответ
        return new AuthResponseDTO(token, user.getUsername(), user.getRole());
    }
    
    public AuthResponseDTO authenticate(AuthRequestDTO request) {
        try {
            // Проверяем аутентификацию
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );
            
            // Устанавливаем аутентификацию в контекст
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Получаем данные пользователя
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Генерируем JWT токен
            String token = jwtService.generateToken(userDetails);
            
            // Возвращаем ответ
            return new AuthResponseDTO(token, user.getUsername(), user.getRole());
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid username or password");
        }
    }
}