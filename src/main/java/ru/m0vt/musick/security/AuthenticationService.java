package ru.m0vt.musick.security;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.m0vt.musick.dto.AuthRequestDTO;
import ru.m0vt.musick.dto.AuthResponseDTO;
import ru.m0vt.musick.dto.RegisterRequestDTO;
import ru.m0vt.musick.exception.AccessDeniedException;
import ru.m0vt.musick.exception.AuthenticationFailedException;
import ru.m0vt.musick.exception.UserAlreadyExistsException;
import ru.m0vt.musick.model.User;
import ru.m0vt.musick.repository.UserRepository;

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
            throw new UserAlreadyExistsException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email is already in use");
        }

        // Если роль - ADMIN, проверяем права текущего пользователя
        if ("ADMIN".equalsIgnoreCase(request.getRole())) {
            // Получаем текущего пользователя, если он есть
            Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
            boolean isAdmin = false;

            if (
                authentication != null &&
                authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())
            ) {
                isAdmin = authentication
                    .getAuthorities()
                    .stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            }

            // Если текущий пользователь не админ, отказываем в создании админа
            if (!isAdmin) {
                throw new AccessDeniedException(
                    "Only administrators can create users with ADMIN role"
                );
            }
        }

        // Создаем нового пользователя
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        // Для не-админов разрешаем только роли USER и ARTIST
        user.setRole(
            "ADMIN".equalsIgnoreCase(request.getRole())
                ? "ADMIN"
                : request.getRole()
        );
        user.setCreatedAt(LocalDateTime.now());
        // Устанавливаем начальный баланс
        user.setBalance(java.math.BigDecimal.ZERO);

        // Сохраняем пользователя
        userRepository.save(user);

        // Загружаем данные пользователя для JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(
            user.getUsername()
        );

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
            SecurityContextHolder.getContext()
                .setAuthentication(authentication);

            // Получаем данные пользователя
            UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();
            User user = userRepository
                .findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new AuthenticationFailedException("User not found"));

            // Генерируем JWT токен
            String token = jwtService.generateToken(userDetails);

            // Возвращаем ответ
            return new AuthResponseDTO(
                token,
                user.getUsername(),
                user.getRole()
            );
        } catch (AuthenticationException e) {
            throw new AuthenticationFailedException("Invalid username or password");
        }
    }
}
