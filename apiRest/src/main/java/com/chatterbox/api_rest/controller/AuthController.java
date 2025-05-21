package com.chatterbox.api_rest.controller;

import com.chatterbox.api_rest.dto.auth.LoginDto;
import com.chatterbox.api_rest.dto.usuario.UsuarioRequestDto;
import com.chatterbox.api_rest.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto login) {
        return authService.login(login);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UsuarioRequestDto nuevoUsuario) {
        return authService.register(nuevoUsuario);
    }
}
