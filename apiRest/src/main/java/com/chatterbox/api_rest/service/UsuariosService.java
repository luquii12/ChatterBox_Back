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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    @Value("${app.ruta.imagenes.perfil}")
    private String carpetaDestino;

    // Modificar objeto respuesta a UsuarioResponseDto
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

            String fotoPerfilString;
            String fotoAntiguaString = usuariosRepository.findFotoPerfilByIdUsuario(idUsuario);
            if (fotoNoCambiada(usuarioModificado.getFoto_perfil())) {
                fotoPerfilString = fotoAntiguaString;
            } else {
                try {
                    fotoPerfilString = guardarArchivoFotoPerfil(usuarioModificado.getFoto_perfil(), idUsuario);
                    eliminarArchivoFotoPerfilAnterior(fotoAntiguaString);
                } catch (IOException e) {
                    log.error("Error al guardar la foto de perfil", e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error al guardar la imagen");
                }
            }

            String hashPasswordFinal;
            if (passwordNoCambiada(usuarioModificado.getPassword())) {
                hashPasswordFinal = usuariosRepository.findHashPasswordByIdUsuario(idUsuario);
            } else {
                hashPasswordFinal = usuarioModificado.getPasswordCifrada(passwordEncoder);
            }

            UsuarioBdDto usuarioBd = UsuarioBdDto.builder()
                    .id_usuario(idUsuario)
                    .apodo(usuarioModificado.getApodo())
                    .nombre_usuario(usuarioModificado.getNombre_usuario())
                    .email(usuarioModificado.getEmail())
                    .hash_password(hashPasswordFinal)
                    .foto_perfil(fotoPerfilString)
                    .build();

            usuariosRepository.updateUsuario(usuarioBd);

            return ResponseEntity.ok(prepararRespuestaConToken(usuarioBd));
        } catch (Exception e) {
            log.error("Error inesperado al actualizar los datos del usuario", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    public ResponseEntity<?> getFotoPerfil() {
        try {
            Long idUsuarioAutenticado = authUtils.obtenerIdDelToken();
            if (authUtils.usuarioNoEncontrado(idUsuarioAutenticado)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado");
            }

            String nombreArchivo = usuariosRepository.findFotoPerfilByIdUsuario(idUsuarioAutenticado);
            if (nombreArchivo == null || nombreArchivo.isBlank()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No hay foto de perfil");
            }

            Path rutaArchivo = Paths.get(carpetaDestino)
                    .resolve(nombreArchivo)
                    .normalize(); // Eliminar cosas raras que pueda haber en la ruta ("..", ".", etc)
            if (!rutaArchivo.startsWith(Paths.get(carpetaDestino)) || !Files.exists(rutaArchivo)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Archivo no encontrado");
            }

            Resource recurso = new UrlResource(rutaArchivo.toUri());

            String contentType = Files.probeContentType(rutaArchivo);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(recurso);
        } catch (Exception e) {
            log.error("Error inesperado al obtener la foto de perfil", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    private boolean fotoNoCambiada(MultipartFile foto) {
        return foto == null || foto.isEmpty();
    }

    private boolean passwordNoCambiada(String password) {
        return password == null || password.isBlank();
    }

    private String guardarArchivoFotoPerfil(MultipartFile archivo, Long idUsuario) throws IOException {
        Path rutaCarpeta = Paths.get(carpetaDestino);
        if (!Files.exists(rutaCarpeta)) {
            Files.createDirectories(rutaCarpeta);
        }

        String extension = obtenerExtensionPorMime(archivo);
        // Generar un nombre único para el archivo
        String nombreArchivo = "usuario_" + idUsuario + "_" + System.currentTimeMillis() + "." + extension;
        Path destino = rutaCarpeta.resolve(nombreArchivo);

        // Guardar el archivo en la ruta de destino
        Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        return nombreArchivo;
    }

    private String obtenerExtensionPorMime(MultipartFile archivo) throws IOException {
        String tipoMime = archivo.getContentType();

        if (tipoMime == null) {
            throw new IOException("No se pudo determinar el tipo MIME del archivo");
        }

        switch (tipoMime) {
            case "image/jpg", "image/jpeg" -> {
                return "jpg";
            }
            case "image/png" -> {
                return "png";
            }
            case "image/webp" -> {
                return "webp";
            }
            case "image/avif" -> {
                return "avif";
            }
            default -> throw new IOException("Tipo MIME no permitido: " + tipoMime);
        }
    }

    private void eliminarArchivoFotoPerfilAnterior(String nombreArchivo) throws IOException {
        if (nombreArchivo == null || nombreArchivo.isBlank()) return;

        Path rutaArchivo = Paths.get(carpetaDestino, nombreArchivo);

        Files.deleteIfExists(rutaArchivo);
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
