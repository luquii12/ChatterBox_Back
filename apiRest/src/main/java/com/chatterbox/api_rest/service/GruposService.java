package com.chatterbox.api_rest.service;

import com.chatterbox.api_rest.dto.grupo.ChatDeUnGrupoDto;
import com.chatterbox.api_rest.dto.grupo.GrupoDto;
import com.chatterbox.api_rest.dto.usuario_grupo.GrupoDelUsuarioDto;
import com.chatterbox.api_rest.repository.GruposRepository;
import com.chatterbox.api_rest.util.AuthUtils;
import com.chatterbox.api_rest.util.ValidacionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class GruposService {
    private final GruposRepository gruposRepository;
    private final AuthUtils authUtils;
    private final ModelMapper modelMapper;

    public ResponseEntity<?> createGrupo(GrupoDto nuevoGrupo) {
        try {
            Long idUsuarioAutenticado = authUtils.obtenerIdDelToken();
            if (authUtils.usuarioNoEncontrado(idUsuarioAutenticado)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado");
            }

            nuevoGrupo.setId_usuario_creador(idUsuarioAutenticado);

            List<String> camposObligatorios = List.of(nuevoGrupo.getNombre_grupo());
            if (!ValidacionUtils.camposValidos(camposObligatorios)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Credenciales inválidas");
            }

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

    public ResponseEntity<?> getGruposPublicosPorNombreWhereUsuarioNoEste(String nombre, Pageable pageable) {
        if (nombre == null || nombre.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Falta el nombre del grupo");
        }

        try {
            int total = gruposRepository.countGruposPublicosPorNombreWhereUsuarioNoEste(nombre, authUtils.obtenerIdDelToken());
            if (total == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No existen grupos disponibles con el nombre buscado");
            }

            List<GrupoDto> gruposPublicos = gruposRepository.findGruposPublicosByNombreWhereUsuarioNoEste(nombre, authUtils.obtenerIdDelToken(), pageable);
            Page<GrupoDto> page = new PageImpl<>(gruposPublicos, pageable, total);

            return ResponseEntity.ok(page);
        } catch (Exception e) {
            log.error("Error inesperado durante la búsqueda de los grupos públicos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    public ResponseEntity<?> joinGrupo(Long idGrupo) {
        try {
            Optional<GrupoDto> grupoOptional = gruposRepository.findGrupoById(idGrupo);
            if (grupoOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Grupo no encontrado");
            }

            Long idUsuarioAutenticado = authUtils.obtenerIdDelToken();
            if (authUtils.usuarioNoEncontrado(idUsuarioAutenticado)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado");
            }

            // Usuario está en el grupo
            if (gruposRepository.usuarioPerteneceAlGrupo(idUsuarioAutenticado, idGrupo)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("El usuario ya pertenece al grupo");
            }

            LocalDateTime fechaActual = LocalDateTime.now();
            gruposRepository.insertUsuarioGrupo(idUsuarioAutenticado, idGrupo, fechaActual);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String fechaFormateada = fechaActual.format(formatter);
            // Falta rellenar el dto
            GrupoDto grupo = grupoOptional.get();
            GrupoDelUsuarioDto nuevoGrupoDelUsuario = modelMapper.map(grupo, GrupoDelUsuarioDto.class);
            nuevoGrupoDelUsuario.setFecha_inscripcion(fechaFormateada);

            return ResponseEntity.ok(nuevoGrupoDelUsuario);
        } catch (Exception e) {
            log.error("Error inesperado al unirse al grupo {}", idGrupo, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    public ResponseEntity<?> editGrupo(Long idGrupo, GrupoDto grupoModificado) {
        try {
            Long idUsuarioAutenticado = authUtils.obtenerIdDelToken();
            if (authUtils.usuarioNoEncontrado(idUsuarioAutenticado)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado");
            }

            if (!authUtils.esAdminGrupo(idUsuarioAutenticado, idGrupo)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("El usuario no es administrador del grupo");
            }

            List<String> camposObligatorios = List.of(grupoModificado.getNombre_grupo());
            if (!ValidacionUtils.camposValidos(camposObligatorios)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Campos inválidos");
            }

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
        try {
            Long idUsuarioAutenticado = authUtils.obtenerIdDelToken();
            if (authUtils.usuarioNoEncontrado(idUsuarioAutenticado)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado");
            }

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

    public ResponseEntity<?> deleteGrupo(Long idGrupo) {
        try {
            Long idUsuarioAutenticado = authUtils.obtenerIdDelToken();
            if (authUtils.usuarioNoEncontrado(idUsuarioAutenticado)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado");
            }

            if (!authUtils.esAdminGrupo(idUsuarioAutenticado, idGrupo)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("El usuario no es administrador del grupo");
            }

            boolean eliminado = gruposRepository.deleteGrupo(idGrupo);
            if (!eliminado) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No se ha podido eliminar el grupo");
            }

            return ResponseEntity.noContent()
                    .build();
        } catch (Exception e) {
            log.error("Error inesperado al eliminar el grupo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }
}
