package com.chatterbox.api_rest.service;

import com.chatterbox.api_rest.dto.LoginDto;
import com.chatterbox.api_rest.dto.UsuarioDto;
import com.chatterbox.api_rest.dto.UsuarioSinPasswordDto;
import com.chatterbox.api_rest.repository.ChatterboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ChatterboxService {
    private final ChatterboxRepository chatterboxRepository;
    private final PasswordEncoder passwordEncoder;

//    Mirar cómo añadir los try-catch

    public ResponseEntity<?> getUsuarioById(Long idUsuario) {
        Optional<UsuarioDto> usuarioOptional = chatterboxRepository.findUsuarioById(idUsuario);
        if (usuarioOptional.isPresent()) {
            return ResponseEntity.ok(usuarioOptional.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No existe el usuario buscado");
    }

    public ResponseEntity<?> login(LoginDto loginDto) {
        if (loginDto.getEmail() == null || loginDto.getEmail().isEmpty() || loginDto.getPassword() == null || loginDto.getPassword().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email y contraseña son requeridos");
        }

        try {
            Optional<UsuarioDto> usuarioOptional = chatterboxRepository.findUsuarioByEmail(loginDto.getEmail());
            if (usuarioOptional.isPresent()) {
                UsuarioDto usuario = usuarioOptional.get();
                if (passwordEncoder.matches(loginDto.getPassword(), usuario.getHash_password())) {
                    UsuarioSinPasswordDto usuarioSinPasswordDto = new UsuarioSinPasswordDto(usuario.getId_usuario(), usuario.getApodo(), usuario.getNombre_usuario(), usuario.getEmail(), usuario.isEs_admin_general(), usuario.getFoto_perfil());
                    return ResponseEntity.ok(usuarioSinPasswordDto);
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Contraseña incorrecta");
                }
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
