package com.chatterbox.api_rest.service;

import com.chatterbox.api_rest.dto.LoginDto;
import com.chatterbox.api_rest.dto.UsuarioBdDto;
import com.chatterbox.api_rest.dto.UsuarioRequestDto;
import com.chatterbox.api_rest.dto.UsuarioResponseDto;
import com.chatterbox.api_rest.repository.ChatterboxRepository;
import com.chatterbox.api_rest.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthService {
    private final ChatterboxRepository chatterboxRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public ResponseEntity<?> login(LoginDto login) {
        if (!usuarioLoginValido(login)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Credenciales inválidas");
        }

        try {
            Optional<UsuarioBdDto> usuarioOptional = chatterboxRepository.findUsuarioByEmail(login.getEmail());
            if (usuarioOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado");
            }

            UsuarioBdDto usuarioBd = usuarioOptional.get();

            if (!passwordEncoder.matches(login.getPassword(), usuarioBd.getHash_password())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Contraseña incorrecta");
            }

            String token = jwtUtil.generateToken(usuarioBd);

            UsuarioResponseDto usuarioResponse = transformarUsuarioBdDtoAUsuarioResponseDto(usuarioBd);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("usuario", usuarioResponse);
            respuesta.put("token", token);

            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            log.error("Error inesperado durante el login", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    public ResponseEntity<?> register(UsuarioRequestDto nuevoUsuario) {
        if (!usuarioRegistroValido(nuevoUsuario)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Registro incorrecto");
        }

        try {
            Optional<UsuarioBdDto> usuarioOptional = chatterboxRepository.findUsuarioByApodoOrEmail(nuevoUsuario.getApodo(), nuevoUsuario.getEmail());
            if (usuarioOptional.isEmpty()) {
                Long id = chatterboxRepository.insertUser(nuevoUsuario);

                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(transformarUsuarioRequestDtoAUsuarioResponseDto(id, nuevoUsuario));
            }
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya existe un usuario con ese apodo o email");

        } catch (Exception e) {
            log.error("Error inesperado durante el registro", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    private boolean usuarioLoginValido(LoginDto usuario) {
        List<String> atributos = Arrays.asList(usuario.getEmail(), usuario.getPassword());
        return camposValidos(atributos);
    }

    private boolean usuarioRegistroValido(UsuarioRequestDto usuario) {
        List<String> atributos = Arrays.asList(usuario.getApodo(), usuario.getNombre_usuario(), usuario.getEmail(), usuario.getPassword());
        return camposValidos(atributos);
    }

    private boolean camposValidos(List<String> atributos) {
        return atributos.stream()
                .allMatch(atributo -> atributo != null && !atributo.isEmpty());
    }

    // Comprobarlos y mirar consulta para factorizar
    private UsuarioResponseDto transformarUsuarioBdDtoAUsuarioResponseDto(UsuarioBdDto usuario) {
        return new UsuarioResponseDto(usuario.getId_usuario(), usuario.getApodo(), usuario.getNombre_usuario(), usuario.getEmail(), usuario.isEs_admin_general(), usuario.getFoto_perfil());
    }

    private UsuarioResponseDto transformarUsuarioRequestDtoAUsuarioResponseDto(Long id, UsuarioRequestDto usuario) {
        return new UsuarioResponseDto(id, usuario.getApodo(), usuario.getNombre_usuario(), usuario.getEmail(), usuario.isEs_admin_general(), usuario.getFoto_perfil());
    }
}
