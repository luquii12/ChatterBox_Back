package com.chatterbox.api_rest.controller;

import com.chatterbox.api_rest.dto.LoginDto;
import com.chatterbox.api_rest.dto.UsuarioRequestDto;
import com.chatterbox.api_rest.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> autenticarUsuario(@RequestBody LoginDto login) {
        return authService.login(login);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registrarUsuario(@RequestBody UsuarioRequestDto nuevoUsuario) {
        return authService.register(nuevoUsuario);
    }
}
