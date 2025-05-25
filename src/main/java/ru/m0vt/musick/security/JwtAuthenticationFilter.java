package ru.m0vt.musick.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final JwtUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, JwtUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Проверяем, есть ли заголовок Authorization и начинается ли он с "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Извлекаем токен из заголовка (без префикса "Bearer ")
        jwt = authHeader.substring(7);
        
        try {
            // Извлекаем имя пользователя из токена
            username = jwtService.extractUsername(jwt);
            
            // Если имя пользователя не пустое и нет текущей аутентификации
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Загружаем детали пользователя из базы данных
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                
                // Проверяем валидность токена
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Создаем объект аутентификации и устанавливаем его в контекст
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Ничего не делаем - пользователь не будет аутентифицирован
        }
        
        // Продолжаем цепочку фильтров
        filterChain.doFilter(request, response);
    }
}