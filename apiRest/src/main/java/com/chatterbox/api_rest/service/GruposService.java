package com.chatterbox.api_rest.service;

import com.chatterbox.api_rest.dto.grupo.ChatDeUnGrupoDto;
import com.chatterbox.api_rest.dto.grupo.GrupoDto;
import com.chatterbox.api_rest.dto.grupo.GrupoEditDto;
import com.chatterbox.api_rest.dto.usuario.UsuarioBdDto;
import com.chatterbox.api_rest.dto.usuario_grupo.GrupoDelUsuarioDto;
import com.chatterbox.api_rest.dto.usuario_grupo.UsuarioDelGrupoDto;
import com.chatterbox.api_rest.repository.GruposRepository;
import com.chatterbox.api_rest.repository.UsuariosRepository;
import com.chatterbox.api_rest.util.AuthUtils;
import com.chatterbox.api_rest.util.ImgUtils;
import com.chatterbox.api_rest.util.ValidacionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class GruposService {
    private final GruposRepository gruposRepository;
    private final AuthUtils authUtils;
    private final ModelMapper modelMapper;
    private final UsuariosRepository usuariosRepository;

    @Value("${app.ruta.imagenes.grupo}")
    private String carpetaDestino;

    public ResponseEntity<?> getGrupoById(Long idGrupo) {
        try {
            Optional<GrupoDto> grupoOptional = gruposRepository.findGrupoById(idGrupo);
            if (grupoOptional.isPresent()) {
                return ResponseEntity.ok(grupoOptional.get());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No existe el grupo buscado");
        } catch (Exception e) {
            log.error("Error al obtener el grupo con id {}", idGrupo);
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

    public ResponseEntity<?> getAllUsuariosGrupoExceptoASiMismo(Long idGrupo, Pageable pageable) {
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

            int total = gruposRepository.countUsuariosGrupoExceptoASiMismo(idGrupo, idUsuarioAutenticado);
            if (total == 0) {
                return ResponseEntity.noContent()
                        .build();
            }

            List<UsuarioDelGrupoDto> usuariosGrupo = gruposRepository.findAllUsuariosGrupoExceptoASiMismo(idGrupo, idUsuarioAutenticado, pageable);
            Page<UsuarioDelGrupoDto> page = new PageImpl<>(usuariosGrupo, pageable, total);

            return ResponseEntity.ok(page);
        } catch (Exception e) {
            log.error("Error inesperado durante la búsqueda de los usuarios del grupo", e);
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

    public ResponseEntity<?> getFotoGrupo(Long idGrupo) {
        String nombreArchivo = gruposRepository.findFotoPerfilByIdGrupo(idGrupo);
        return ImgUtils.obtenerImgComoResponse(nombreArchivo, carpetaDestino);
    }

    public ResponseEntity<?> createGrupo(GrupoEditDto nuevoGrupo) {
        try {
            Long idUsuarioAutenticado = authUtils.obtenerIdDelToken();
            if (authUtils.usuarioNoEncontrado(idUsuarioAutenticado)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado");
            }

            List<String> camposObligatorios = List.of(nuevoGrupo.getNombre_grupo());
            if (!ValidacionUtils.camposValidos(camposObligatorios) && noHayFotoNueva(nuevoGrupo.getFoto_grupo())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Credenciales inválidas");
            }

            GrupoDto grupoResponse = GrupoDto.builder()
                    .id_usuario_creador(idUsuarioAutenticado)
                    .nombre_grupo(nuevoGrupo.getNombre_grupo())
                    .descripcion(nuevoGrupo.getDescripcion())
                    .es_privado(nuevoGrupo.isEs_privado())
                    .build();

            Long id = gruposRepository.insertGrupo(grupoResponse);
            grupoResponse.setId_grupo(id);

            String fotoGrupoString;
            try {
                fotoGrupoString = ImgUtils.guardarImg(nuevoGrupo.getFoto_grupo(), id, carpetaDestino, "grupo");
            } catch (IOException e) {
                log.error("Error al guardar la foto del grupo", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error al guardar la imagen");
            }

            gruposRepository.updateFotoGrupo(id, fotoGrupoString);
            grupoResponse.setFoto_grupo(fotoGrupoString);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(grupoResponse);
        } catch (MaxUploadSizeExceededException e) {
            log.error("Error al subir la imagen ", e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("La imagen supera el tamaño límite de 5MB");
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

    public ResponseEntity<?> editGrupo(Long idGrupo, GrupoEditDto grupoModificado) {
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

            String fotoGrupoString;
            String fotoAntiguaString = gruposRepository.findFotoGrupoByIdGrupo(idGrupo);
            if (noHayFotoNueva(grupoModificado.getFoto_grupo())) {
                fotoGrupoString = fotoAntiguaString;
            } else {
                try {
                    fotoGrupoString = ImgUtils.guardarImg(grupoModificado.getFoto_grupo(), idGrupo, carpetaDestino, "grupo");
                    ImgUtils.eliminarImgAnterior(fotoAntiguaString, carpetaDestino);
                } catch (IOException e) {
                    log.error("Error al guardar la foto del grupo", e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error al guardar la imagen");
                }
            }

            GrupoDto grupoResponse = GrupoDto.builder()
                    .id_grupo(idGrupo)
                    .id_usuario_creador(grupoModificado.getId_usuario_creador())
                    .nombre_grupo(grupoModificado.getNombre_grupo())
                    .descripcion(grupoModificado.getDescripcion())
                    .es_privado(grupoModificado.isEs_privado())
                    .foto_grupo(fotoGrupoString)
                    .build();

            gruposRepository.updateGrupo(grupoResponse);

            return ResponseEntity.ok(grupoResponse);
        } catch (Exception e) {
            log.error("Error inesperado al actualizar los datos del grupo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    public ResponseEntity<?> setAdminGrupo(Long idGrupo, Long idUsuario) {
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

            Optional<UsuarioBdDto> usuarioOptional = usuariosRepository.findUsuarioById(idUsuario);
            if (usuarioOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado");
            }

            if (!gruposRepository.usuarioPerteneceAlGrupo(idUsuario, idGrupo)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("El usuario no pertenece al grupo");
            }

            UsuarioDelGrupoDto usuarioDelGrupo = gruposRepository.setAdminGrupo(idGrupo, idUsuario);

            return ResponseEntity.ok(usuarioDelGrupo);
        } catch (Exception e) {
            log.error("Error inesperado al dar permisos de administrador del grupo al usuario", e);
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

            boolean esAdmin = usuariosRepository.usuarioIsAdminGrupo(idUsuarioAutenticado, idGrupo);
            boolean eliminado = gruposRepository.deleteUsuarioDelGrupo(idGrupo, idUsuarioAutenticado);
            if (!eliminado) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No se ha podido abandonar el grupo");
            }

            if (ultimoUsuarioGrupo(idGrupo)) {
                boolean grupoEliminado = gruposRepository.deleteGrupo(idGrupo);
                if (!grupoEliminado) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("No se ha podido eliminar el grupo");
                }
                return ResponseEntity.ok("Abandonado el grupo y grupo eliminado porque no quedan usuarios");
            }

            if (esAdmin && gruposRepository.countAdminGrupo(idGrupo) == 0) {
                UsuarioBdDto nuevoAdmin = usuariosRepository.setAdminGrupoUsuarioMasLongevo(idGrupo);
                return ResponseEntity.ok(Map.of("mensaje", "Abandonado el grupo exitosamente", "nuevoAdmin", nuevoAdmin));
            }

            return ResponseEntity.ok("Abandonado el grupo exitosamente");
        } catch (Exception e) {
            log.error("Error inesperado al abandonar el grupo {}", idGrupo, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    public ResponseEntity<?> deleteUsuarioDelGrupo(Long idUsuario, Long idGrupo) {
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

            Optional<UsuarioBdDto> usuarioOptional = usuariosRepository.findUsuarioById(idUsuario);
            if (usuarioOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado");
            }

            if (!gruposRepository.usuarioPerteneceAlGrupo(idUsuario, idGrupo)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("El usuario no pertenece al grupo");
            }

            if (gruposRepository.usuarioIsCreadorGrupo(idUsuario, idGrupo)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("No se puede eliminar al creador del grupo");
            }

            boolean eliminado = gruposRepository.deleteUsuarioDelGrupo(idGrupo, idUsuario);
            if (!eliminado) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No se ha podido eliminar el usuario del grupo");
            }

            return ResponseEntity.noContent()
                    .build();
        } catch (Exception e) {
            log.error("Error inesperado al eliminar el usuario {} del grupo {}", idUsuario, idGrupo, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    public ResponseEntity<?> deleteAdminGrupo(Long idGrupo, Long idUsuario) {
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

            Optional<UsuarioBdDto> usuarioOptional = usuariosRepository.findUsuarioById(idUsuario);
            if (usuarioOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado");
            }

            if (!gruposRepository.usuarioPerteneceAlGrupo(idUsuario, idGrupo)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("El usuario no pertenece al grupo");
            }

            if (gruposRepository.usuarioIsCreadorGrupo(idUsuario, idGrupo)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("No se puede eliminar al creador del grupo");
            }

            UsuarioDelGrupoDto usuarioDelGrupo = gruposRepository.deleteAdminGrupo(idGrupo, idUsuario);
            if (usuarioDelGrupo.isEs_admin_grupo()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No se ha podido quitar permisos de administrador del grupo al usuario");
            }

            return ResponseEntity.ok(usuarioDelGrupo);
        } catch (Exception e) {
            log.error("Error inesperado al eliminar permisos de administrador del grupo al usuario", e);
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

    private boolean noHayFotoNueva(MultipartFile foto) {
        return foto == null || foto.isEmpty();
    }

    private boolean ultimoUsuarioGrupo(Long idGrupo) {
        return gruposRepository.countUsuariosGrupo(idGrupo) == 0;
    }
}
