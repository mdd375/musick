package ru.m0vt.musick.controller;

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
public class AuthController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO request
    ) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> authenticate(
            @Valid @RequestBody AuthRequestDTO request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
}