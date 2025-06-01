package com.chatterbox.api_rest.service;

import com.chatterbox.api_rest.dto.usuario.UsuarioBdDto;
import com.chatterbox.api_rest.dto.usuario.UsuarioRequestDto;
import com.chatterbox.api_rest.dto.usuario.UsuarioResponseDto;
import com.chatterbox.api_rest.dto.usuario_grupo.GrupoDelUsuarioDto;
import com.chatterbox.api_rest.repository.UsuariosRepository;
import com.chatterbox.api_rest.security.JwtUtil;
import com.chatterbox.api_rest.util.AuthUtils;
import com.chatterbox.api_rest.util.ImgUtils;
import com.chatterbox.api_rest.util.ValidacionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    public ResponseEntity<?> getUsuarioById(Long idUsuario) {
        try {
            Optional<UsuarioBdDto> usuarioOptional = usuariosRepository.findUsuarioById(idUsuario);
            if (usuarioOptional.isPresent()) {
                return ResponseEntity.ok(modelMapper.map(usuarioOptional.get(), UsuarioResponseDto.class));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No existe el usuario buscado");
        } catch (Exception e) {
            log.error("Error al obtener el usuario con id {}", idUsuario);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    public ResponseEntity<?> getAllUsuariosExceptoASiMismo(Pageable pageable) {
        try {
            Long idUsuarioAutenticado = authUtils.obtenerIdDelToken();
            if (authUtils.usuarioNoEncontrado(idUsuarioAutenticado)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado");
            }

            int total = usuariosRepository.countUsuariosExceptoASiMismo(idUsuarioAutenticado);
            if (total == 0) {
                return ResponseEntity.noContent()
                        .build();
            }

            List<UsuarioBdDto> usuariosBd = usuariosRepository.findAllUsuariosExceptoASiMismo(idUsuarioAutenticado, pageable);
            List<UsuarioResponseDto> usuariosResponse = usuariosBd.stream()
                    .map(u -> modelMapper.map(u, UsuarioResponseDto.class))
                    .toList();
            Page<UsuarioResponseDto> page = new PageImpl<>(usuariosResponse, pageable, total);

            return ResponseEntity.ok(page);
        } catch (Exception e) {
            log.error("Error inesperado durante la búsqueda de los usuarios de la app", e);
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

    public ResponseEntity<?> getFotoPerfil(Long idUsuario) {
        String nombreArchivo = usuariosRepository.findFotoPerfilByIdUsuario(idUsuario);
        return ImgUtils.obtenerImgComoResponse(nombreArchivo, carpetaDestino);
    }

    public ResponseEntity<?> editUsuario(Long idUsuario, UsuarioRequestDto usuarioModificado) {
        try {
            Optional<UsuarioBdDto> usuarioOpt = usuariosRepository.findUsuarioById(idUsuario);
            if (usuarioOpt.isEmpty()) {
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
                    fotoPerfilString = ImgUtils.guardarImg(usuarioModificado.getFoto_perfil(), idUsuario, carpetaDestino, "usuario");
                    ImgUtils.eliminarImgAnterior(fotoAntiguaString, carpetaDestino);
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

    public ResponseEntity<?> setAdminGeneral(Long idUsuario) {
        try {
            Optional<UsuarioBdDto> usuarioOptional = usuariosRepository.findUsuarioById(idUsuario);
            if (usuarioOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado");
            }

            usuariosRepository.setAdminGeneral(idUsuario);
            UsuarioBdDto usuarioBd = usuarioOptional.get();
            usuarioBd.setEs_admin_general(true);

            return ResponseEntity.ok(modelMapper.map(usuarioBd, UsuarioResponseDto.class));
        } catch (Exception e) {
            log.error("Error inesperado al dar permisos de administrador general al usuario", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    public ResponseEntity<?> deleteUsuario(Long idUsuario) {
        try {
            Optional<UsuarioBdDto> usuarioOptional = usuariosRepository.findUsuarioById(idUsuario);
            if (usuarioOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado");
            }

            if (esUnicoAdminGeneral(usuarioOptional.get())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No se puede eliminar el usuario");
            }

            boolean eliminado = usuariosRepository.deleteUsuario(idUsuario);
            if (!eliminado) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No se ha podido eliminar el usuario");
            }

            return ResponseEntity.noContent()
                    .build();
        } catch (Exception e) {
            log.error("Error inesperado al eliminar el usuario", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    public ResponseEntity<?> deleteAdminGeneral(Long idUsuario) {
        try {
            Optional<UsuarioBdDto> usuarioOptional = usuariosRepository.findUsuarioById(idUsuario);
            if (usuarioOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado");
            }

            UsuarioBdDto usuarioBd = usuarioOptional.get();
            if (esUnicoAdminGeneral(usuarioBd)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No se pueden quitar permisos de administrador general al usuario");
            }

            boolean permisosQuitados = usuariosRepository.deleteAdminGeneral(idUsuario);
            if (!permisosQuitados) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No se ha podido quitar permisos de administrador general al usuario");
            }
            usuarioBd.setEs_admin_general(false);

            return ResponseEntity.ok(modelMapper.map(usuarioBd, UsuarioResponseDto.class));
        } catch (Exception e) {
            log.error("Error inesperado al eliminar permisos de administrador general al usuario", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    private boolean esUnicoAdminGeneral(UsuarioBdDto usuarioBd) {
        return usuarioBd.isEs_admin_general() && usuariosRepository.countAdminGeneral() == 1;
    }

    private boolean fotoNoCambiada(MultipartFile foto) {
        return foto == null || foto.isEmpty();
    }

    private boolean passwordNoCambiada(String password) {
        return password == null || password.isBlank();
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
