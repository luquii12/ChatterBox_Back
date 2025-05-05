package com.chatterbox.api_rest.controller;

import com.chatterbox.api_rest.dto.LoginDto;
import com.chatterbox.api_rest.dto.UsuarioRequestDto;
import com.chatterbox.api_rest.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    // Faltaría crear el token en login/register y devolverlo

    @PostMapping("/auth/login")
    public ResponseEntity<?> autenticarUsuario(@RequestBody LoginDto login) {
        return authService.login(login);
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> registrarUsuario(@RequestBody UsuarioRequestDto nuevoUsuario) {
        return authService.register(nuevoUsuario);
    }
}
