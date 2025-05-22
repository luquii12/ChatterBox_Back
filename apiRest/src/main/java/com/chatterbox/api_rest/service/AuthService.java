package com.chatterbox.api_rest.service;

import com.chatterbox.api_rest.dto.auth.LoginDto;
import com.chatterbox.api_rest.dto.usuario.UsuarioBdDto;
import com.chatterbox.api_rest.dto.usuario.UsuarioRequestDto;
import com.chatterbox.api_rest.dto.usuario.UsuarioResponseDto;
import com.chatterbox.api_rest.repository.UsuariosRepository;
import com.chatterbox.api_rest.security.JwtUtil;
import com.chatterbox.api_rest.util.ValidacionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthService {
    private final UsuariosRepository usuariosRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ModelMapper modelMapper;

    public ResponseEntity<?> login(LoginDto login) {
        if (!usuarioLoginValido(login)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Credenciales inválidas");
        }

        try {
            Optional<UsuarioBdDto> usuarioOptional = usuariosRepository.findUsuarioByEmail(login.getEmail());
            if (usuarioOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado");
            }

            UsuarioBdDto usuarioBd = usuarioOptional.get();

            if (!passwordEncoder.matches(login.getPassword(), usuarioBd.getHash_password())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Contraseña incorrecta");
            }

            return ResponseEntity.ok(prepararRespuestaConToken(usuarioBd));
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
            Optional<UsuarioBdDto> usuarioOptional = usuariosRepository.findUsuarioByApodoOrEmail(nuevoUsuario.getApodo(), nuevoUsuario.getEmail());
            if (usuarioOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Ya existe un usuario con el mismo apodo o email");
            }

            UsuarioBdDto usuarioBd = UsuarioBdDto.builder()
                    .apodo(nuevoUsuario.getApodo())
                    .nombre_usuario(nuevoUsuario.getNombre_usuario())
                    .email(nuevoUsuario.getEmail())
                    .hash_password(nuevoUsuario.getPasswordCifrada(passwordEncoder))
                    .foto_perfil("foto_perfil_default.png")
                    .build();

            Long id = usuariosRepository.insertUsuario(usuarioBd);
            usuarioBd.setId_usuario(id);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(prepararRespuestaConToken(usuarioBd));
        } catch (Exception e) {
            log.error("Error inesperado durante el registro", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    private boolean usuarioLoginValido(LoginDto usuario) {
        List<String> camposObligatorios = List.of(usuario.getEmail(), usuario.getPassword());
        return ValidacionUtils.camposValidos(camposObligatorios);
    }

    private boolean usuarioRegistroValido(UsuarioRequestDto usuario) {
        List<String> camposObligatorios = List.of(usuario.getApodo(), usuario.getNombre_usuario(), usuario.getEmail(), usuario.getPassword());
        return ValidacionUtils.camposValidos(camposObligatorios);
    }

    private Map<String, Object> prepararRespuestaConToken(UsuarioBdDto usuarioBd) {
        UsuarioResponseDto usuarioResponse = modelMapper.map(usuarioBd, UsuarioResponseDto.class);
        String token = jwtUtil.generateToken(usuarioBd);
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("usuario", usuarioResponse);
        respuesta.put("token", token);
        return respuesta;
    }
}
