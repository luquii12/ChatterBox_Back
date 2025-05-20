package com.chatterbox.api_rest.service;

import com.chatterbox.api_rest.dto.usuario.UsuarioBdDto;
import com.chatterbox.api_rest.dto.usuario.UsuarioRequestDto;
import com.chatterbox.api_rest.dto.usuario.UsuarioResponseDto;
import com.chatterbox.api_rest.dto.usuario_grupo.GrupoDelUsuarioDto;
import com.chatterbox.api_rest.repository.UsuariosRepository;
import com.chatterbox.api_rest.security.JwtUtil;
import com.chatterbox.api_rest.util.AuthUtils;
import com.chatterbox.api_rest.util.ValidacionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
    private final AuthUtils authUtils;

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
        try {
            if (authUtils.usuarioNoEncontrado(idUsuario)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado");
            }

            List<String> camposObligatorios = List.of(usuarioModificado.getApodo(), usuarioModificado.getNombre_usuario(), usuarioModificado.getEmail());
            if (!ValidacionUtils.camposValidos(camposObligatorios)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Campos inválidos");
            }

            Optional<UsuarioBdDto> usuarioOptional = usuariosRepository.findUsuarioByApodoOrEmailAndDifferentId(idUsuario, usuarioModificado.getApodo(), usuarioModificado.getEmail());
            if (usuarioOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Ya existe un usuario con el mismo apodo o email");
            }

            // Si no se ha cambiado la foto se le asigna la que tenía
            String rutaFotoPerfil;
            if (usuarioModificado.getFoto_perfil() == null || usuarioModificado.getFoto_perfil()
                    .isEmpty()) {
                rutaFotoPerfil = usuariosRepository.findFotoPerfilByIdUsuario(idUsuario);
            } else {
                // Guardar la img en la carpeta img/
                rutaFotoPerfil = guardarArchivoFotoPerfil(usuarioModificado.getFoto_perfil(), idUsuario);
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

    // ¿Por qué la excepción y cómo la puedo controlar?
    private String guardarArchivoFotoPerfil(MultipartFile archivo, Long idUsuario) {
        // La primera comprobación me la puedo ahorrar ya que la hago antes de llamar al método

        String carpetaDestino = ""; // Debería recuperarlo del application.properties
        Path rutaCarpeta = Paths.get(carpetaDestino);
        // Me puedo ahorrar la comprobación de que la carpeta de destino no exista, ya que la creo manualmente y no la vuelvo a editar

        // Generar un nombre único para el archivo
        String extension = "";

        // Extraer la extensión original (si la hay)
        String nombreOriginal = archivo.getOriginalFilename();
        if (nombreOriginal != null && nombreOriginal.contains(".")) {
            extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
        }

        String nombreArchivo = "usuario_" + idUsuario + "_" + System.currentTimeMillis() + extension;

        Path destino = rutaCarpeta.resolve(nombreArchivo);

        // Guardar el archivo en la ruta de destino
        Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        return nombreArchivo; // ¿Nombre o ruta? Pq puede que tenga una subcarpeta para fotos de perfil y otra para fotos de grupo
    }
}
