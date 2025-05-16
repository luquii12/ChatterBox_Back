package com.chatterbox.api_rest.service;

import com.chatterbox.api_rest.dto.usuario.UsuarioBdDto;
import com.chatterbox.api_rest.dto.usuario.UsuarioRequestDto;
import com.chatterbox.api_rest.dto.usuario.UsuarioResponseDto;
import com.chatterbox.api_rest.dto.usuario_grupo.GrupoDelUsuarioDto;
import com.chatterbox.api_rest.repository.UsuariosRepository;
import com.chatterbox.api_rest.util.ValidacionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class UsuariosService {
    private final UsuariosRepository usuariosRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    // Modificar objeto respuesta
    public ResponseEntity<?> getUsuarioById(Long idUsuario) {
        try {
            Optional<UsuarioBdDto> usuarioOptional = usuariosRepository.findUsuarioById(idUsuario);
            if (usuarioOptional.isPresent()) {
                return ResponseEntity.ok(usuarioOptional.get());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No existe el usuario buscado");
        } catch (Exception e) {
            log.error("Error al obtener el usuario con id {}", idUsuario);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    public ResponseEntity<?> getGruposDelUsuario(Long idUsuario) {
        try {
            Optional<UsuarioBdDto> usuarioOptional = usuariosRepository.findUsuarioById(idUsuario);
            if (usuarioOptional.isPresent()) {
                List<GrupoDelUsuarioDto> gruposUsuario = usuariosRepository.findGruposByUsuarioIdOrderByFechaInscripcion(idUsuario);
                if (!gruposUsuario.isEmpty()) {
                    return ResponseEntity.ok(gruposUsuario);
                }
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No existe el usuario buscado");
        } catch (Exception e) {
            log.error("Error al obtener los grupos del usuario con id {}", idUsuario);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    // Tengo que devolver UsuarioResponseDto
    public ResponseEntity<?> editUsuario(UsuarioRequestDto usuarioModificado) {
        List<String> camposObligatorios = List.of(usuarioModificado.getApodo(), usuarioModificado.getNombre_usuario(), usuarioModificado.getEmail(), usuarioModificado.getPassword());
        if (!ValidacionUtils.camposValidos(camposObligatorios)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Campos invalidos");
        }

        try {
            Optional<UsuarioBdDto> usuarioOptional = usuariosRepository.findUsuarioByApodoOrEmailAndDifferentId(usuarioModificado);
            if (usuarioOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Ya existe un usuario con el mismo apodo o email");
            }

            UsuarioBdDto usuarioBd = UsuarioBdDto.builder()
                    .id_usuario(usuarioModificado.getId_usuario())
                    .apodo(usuarioModificado.getApodo())
                    .nombre_usuario(usuarioModificado.getNombre_usuario())
                    .email(usuarioModificado.getEmail())
                    .hash_password(usuarioModificado.getPasswordCifrada(passwordEncoder))
                    .es_admin_general(usuarioModificado.isEs_admin_general())
                    .foto_perfil(usuarioModificado.getFoto_perfil())
                    .build();

            usuariosRepository.updateUsuario(usuarioBd);

            return ResponseEntity.ok(modelMapper.map(usuarioBd, UsuarioResponseDto.class));
        } catch (Exception e) {
            log.error("Error inesperado durante el registro del cambio", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }

        return null;
    }
}
