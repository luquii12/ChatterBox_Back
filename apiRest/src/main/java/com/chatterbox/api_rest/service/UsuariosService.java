package com.chatterbox.api_rest.service;

import com.chatterbox.api_rest.dto.usuario.UsuarioBdDto;
import com.chatterbox.api_rest.dto.usuario.UsuarioRequestDto;
import com.chatterbox.api_rest.dto.usuario.UsuarioResponseDto;
import com.chatterbox.api_rest.dto.usuario_grupo.GrupoDelUsuarioDto;
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
public class UsuariosService {
    private final UsuariosRepository usuariosRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final JwtUtil jwtUtil;

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

    public ResponseEntity<?> editUsuario(Long idUsuario, UsuarioRequestDto usuarioModificado) {
        List<String> camposObligatorios = List.of(usuarioModificado.getApodo(), usuarioModificado.getNombre_usuario(), usuarioModificado.getEmail(), usuarioModificado.getPassword());
        if (!ValidacionUtils.camposValidos(camposObligatorios)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Campos inválidos");
        }

        try {
            Optional<UsuarioBdDto> usuarioOptional = usuariosRepository.findUsuarioByApodoOrEmailAndDifferentId(idUsuario, usuarioModificado.getApodo(), usuarioModificado.getEmail());
            if (usuarioOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Ya existe un usuario con el mismo apodo o email");
            }

            // Si no se ha cambiado la foto se le asigna la que tenía
            String fotoPerfil;
            if (usuarioModificado.getFoto_perfil() == null || usuarioModificado.getFoto_perfil()
                    .isEmpty()) {
                fotoPerfil = usuariosRepository.findFotoPerfilByIdUsuario(idUsuario);
            } else {
                fotoPerfil = usuarioModificado.getFoto_perfil();
            }

            // Si no se ha cambiado la contraseña se le asigna la que tenía
            String hashPasswordFinal;
            if (usuarioModificado.getPassword() != null && !usuarioModificado.getPassword()
                    .isEmpty()) {
                hashPasswordFinal = usuarioModificado.getPasswordCifrada(passwordEncoder);
            } else {
                hashPasswordFinal = usuariosRepository.findHashPasswordByIdUsuario(idUsuario);
            }

            UsuarioBdDto usuarioBd = UsuarioBdDto.builder()
                    .id_usuario(idUsuario)
                    .apodo(usuarioModificado.getApodo())
                    .nombre_usuario(usuarioModificado.getNombre_usuario())
                    .email(usuarioModificado.getEmail())
                    .hash_password(hashPasswordFinal)
                    .foto_perfil(fotoPerfil)
                    .build();

            usuariosRepository.updateUsuario(usuarioBd);

            return ResponseEntity.ok(prepararRespuestaConToken(usuarioBd));
        } catch (Exception e) {
            log.error("Error inesperado durante el registro de los cambios del usuario", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
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
