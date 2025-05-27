package ru.m0vt.musick.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.m0vt.musick.dto.AuthRequestDTO;
import ru.m0vt.musick.dto.AuthResponseDTO;
import ru.m0vt.musick.dto.RegisterRequestDTO;
import ru.m0vt.musick.security.AuthenticationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "Аутентификация", description = "API для регистрации и входа в систему")
public class AuthController {

    @Autowired
    private AuthenticationService authenticationService;

    @Operation(
        summary = "Регистрация нового пользователя",
        description = "Создает нового пользователя в системе и возвращает JWT токен для аутентификации"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Пользователь успешно зарегистрирован",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Некорректные данные для регистрации"),
        @ApiResponse(responseCode = "409", description = "Пользователь с таким email или username уже существует")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO request
    ) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @Operation(
        summary = "Вход в систему",
        description = "Аутентифицирует пользователя и возвращает JWT токен для доступа к защищенным ресурсам"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Успешная аутентификация",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDTO.class))
        ),
        @ApiResponse(responseCode = "401", description = "Неверное имя пользователя или пароль")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> authenticate(
            @Valid @RequestBody AuthRequestDTO request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
}