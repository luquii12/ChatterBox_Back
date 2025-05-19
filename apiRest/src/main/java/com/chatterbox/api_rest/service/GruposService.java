package com.chatterbox.api_rest.service;

import com.chatterbox.api_rest.dto.grupo.ChatDeUnGrupoDto;
import com.chatterbox.api_rest.dto.grupo.GrupoDto;
import com.chatterbox.api_rest.dto.usuario.UsuarioBdDto;
import com.chatterbox.api_rest.repository.GruposRepository;
import com.chatterbox.api_rest.repository.UsuariosRepository;
import com.chatterbox.api_rest.security.JwtUtil;
import com.chatterbox.api_rest.security.UsuarioAutenticado;
import com.chatterbox.api_rest.util.ValidacionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class GruposService {
    private final GruposRepository gruposRepository;
    private final UsuariosRepository usuariosRepository;
    private final JwtUtil jwtUtil;

    public ResponseEntity<?> createGrupo(GrupoDto nuevoGrupo) {
        List<String> camposObligatorios = List.of(String.valueOf(nuevoGrupo.getId_usuario_creador()), nuevoGrupo.getNombre_grupo());
        if (!ValidacionUtils.camposValidos(camposObligatorios)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Credenciales inválidas");
        }

        try {
            Long id = gruposRepository.insertGrupo(nuevoGrupo);
            nuevoGrupo.setId_grupo(id);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(nuevoGrupo);
        } catch (DuplicateKeyException e) {
            log.error("Error al agregar grupo ", e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Grupo ya existente para ese usuario");
        } catch (Exception e) {
            log.error("Error inesperado al crear el grupo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    public ResponseEntity<?> getChatsDeUnGrupo(Long idGrupo) {
        try {
            Optional<GrupoDto> grupoOptional = gruposRepository.findGrupoById(idGrupo);
            if (grupoOptional.isPresent()) {
                List<ChatDeUnGrupoDto> chatsGrupo = gruposRepository.findChatsByGrupoIdOrderByFechaCreacion(idGrupo);
                if (!chatsGrupo.isEmpty()) {
                    return ResponseEntity.ok(chatsGrupo);
                }
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No existe el grupo buscado");
        } catch (Exception e) {
            log.error("Error al obtener los chats del grupo con id {}", idGrupo);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    public ResponseEntity<?> getGruposPublicosPorNombre(String nombre, Pageable pageable) {
        if (nombre == null || nombre.trim()
                .isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Falta el nombre del grupo");
        }

        try {
            Page<GrupoDto> gruposPublicos = gruposRepository.findGruposPublicosByNombre(nombre, pageable);
            if (gruposPublicos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No existe grupos con el nombre buscado");
            }

            return ResponseEntity.ok(gruposPublicos);
            return  null;
        } catch (Exception e) {
            log.error("Error inesperado durante la búsqueda de los grupos públicos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    public ResponseEntity<?> joinGrupo(Long idGrupo) {

        return null;
    }

    public ResponseEntity<?> editGrupo(Long idGrupo, GrupoDto grupoModificado) {
        // Comprobar que el usuario sea admin del grupo
        Long idUsuarioAutenticado = obtenerIdDelToken();
        Optional<UsuarioBdDto> usuarioAutenticadoOptional = usuariosRepository.findUsuarioById(idUsuarioAutenticado);
        if (usuarioAutenticadoOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Usuario no encontrado");
        }

        if (!usuariosRepository.findIfUsuarioIsAdminGrupoByIdUsuario(idUsuarioAutenticado, idGrupo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("El usuario no es administrador del grupo");
        }

        List<String> camposObligatorios = List.of(grupoModificado.getNombre_grupo());
        if (!ValidacionUtils.camposValidos(camposObligatorios)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Campos inválidos");
        }

        try {
            // En el caso de que no se haya pasado el id del usuario creador
            if (grupoModificado.getId_usuario_creador() == null) {
                Long idUsuarioCreador = gruposRepository.findIdUsuarioCreadorByIdGrupo(idGrupo);
                grupoModificado.setId_usuario_creador(idUsuarioCreador);
            }

            Optional<GrupoDto> grupoOptional = gruposRepository.findGrupoByNombreAndIdUsuarioCreadorAndDifferentId(idGrupo, grupoModificado);
            if (grupoOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Ya existe un grupo creado por el mismo usuario con el mismo nombre");
            }

            // Si no se ha cambiado la foto se le asigna la que tenía
            if (grupoModificado.getFoto_grupo() == null || grupoModificado.getFoto_grupo()
                    .isEmpty()) {
                grupoModificado.setFoto_grupo(gruposRepository.findFotoGrupoByIdGrupo(idGrupo));
            }

            grupoModificado.setId_grupo(idGrupo);
            gruposRepository.updateGrupo(grupoModificado);

            return ResponseEntity.ok(grupoModificado);
        } catch (Exception e) {
            log.error("Error inesperado durante el registro de los cambios del grupo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    public ResponseEntity<?> leaveGrupo(Long idGrupo) {
        Long idUsuarioAutenticado = obtenerIdDelToken();

        try {
            if (!gruposRepository.usuarioPerteneceAlGrupo(idUsuarioAutenticado, idGrupo)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El usuario no pertenece al grupo");
            }

            boolean eliminado = gruposRepository.deleteUsuarioDelGrupo(idGrupo, idUsuarioAutenticado);
            if (!eliminado) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No se ha podido abandonar el grupo");
            }

            return ResponseEntity.ok("Abandonado el grupo exitosamente");
        } catch (Exception e) {
            log.error("Error inesperado al abandonar el grupo {}", idGrupo, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    private Long obtenerIdDelToken() {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        UsuarioAutenticado usuario = (UsuarioAutenticado) authentication.getPrincipal();
        return usuario.id();
    }
}
