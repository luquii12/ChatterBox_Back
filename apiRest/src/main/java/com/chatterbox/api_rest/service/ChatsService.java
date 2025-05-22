package com.chatterbox.api_rest.service;

import com.chatterbox.api_rest.dto.chat.ChatDto;
import com.chatterbox.api_rest.dto.chat.ChatMensajeDto;
import com.chatterbox.api_rest.dto.usuario.UsuarioBdDto;
import com.chatterbox.api_rest.repository.ChatsRepository;
import com.chatterbox.api_rest.repository.UsuariosRepository;
import com.chatterbox.api_rest.util.AuthUtils;
import com.chatterbox.api_rest.util.ValidacionUtils;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class ChatsService {
    private final ChatsRepository chatsRepository;
    private final UsuariosRepository usuariosRepository;
    private final AuthUtils authUtils;

    public ResponseEntity<?> getMensajesDelChat(Long idChat, @Nullable Integer limite) {
        try {
            Optional<ChatDto> grupoOptional = chatsRepository.findChatById(idChat);
            if (grupoOptional.isPresent()) {
                List<ChatMensajeDto> mensajesChat = chatsRepository.findMensajesByChatIdOrderByHoraEnvioLimitDeterminado(idChat, limite);
                if (!mensajesChat.isEmpty()) {
                    return ResponseEntity.ok(mensajesChat);
                }
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No existe el chat buscado");
        } catch (Exception e) {
            log.error("Error al obtener los mensajes del chat con id {}", idChat);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    public ResponseEntity<?> getAllMensajesDelChat(Long idChat) {
        try {
            Optional<ChatDto> grupoOptional = chatsRepository.findChatById(idChat);
            if (grupoOptional.isPresent()) {
                List<ChatMensajeDto> mensajesChat = chatsRepository.findMensajesByChatIdOrderByHoraEnvio(idChat);
                if (!mensajesChat.isEmpty()) {
                    return ResponseEntity.ok(mensajesChat);
                }
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No existe el chat buscado");
        } catch (Exception e) {
            log.error("Error al obtener los mensajes del chat con id {}", idChat);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    public boolean usuarioEsMiembroDelChat(Long idUsuario, Long idChat) {
        try {
            Optional<UsuarioBdDto> usuarioBdOptional = usuariosRepository.findUsuarioByIdAndChatId(idUsuario, idChat);
            return usuarioBdOptional.isPresent();
        } catch (Exception e) {
            log.error("Error al verificar si el usuario {} pertenece al chat {}: {}", idUsuario, idChat, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<?> createChat(ChatDto nuevoChat) {
        try {
            Long idUsuarioAutenticado = authUtils.obtenerIdDelToken();
            if (authUtils.usuarioNoEncontrado(idUsuarioAutenticado)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado");
            }

            if (!authUtils.esAdminGrupo(idUsuarioAutenticado, nuevoChat.getId_grupo())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("El usuario no es administrador del grupo");
            }

            List<String> camposObligatorios = List.of(nuevoChat.getNombre_chat());
            if (!ValidacionUtils.camposValidos(camposObligatorios)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Campos inválidos");
            }

            LocalDateTime fechaActual = LocalDateTime.now();
            Long id = chatsRepository.insertChat(nuevoChat, fechaActual);
            nuevoChat.setId_chat(id);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String fechaFormateada = fechaActual.format(formatter);
            nuevoChat.setFecha_creacion(fechaFormateada);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(nuevoChat);
        } catch (Exception e) {
            log.error("Error inesperado al crear el chat", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    public ResponseEntity<?> deleteChat(Long idChat) {
        try {
            Long idUsuarioAutenticado = authUtils.obtenerIdDelToken();
            if (authUtils.usuarioNoEncontrado(idUsuarioAutenticado)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado");
            }

            Long idGrupo = chatsRepository.findIdGrupoByIdChat(idChat);
            if (idGrupo == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Chat no encontrado");
            }

            if (!authUtils.esAdminGrupo(idUsuarioAutenticado, idGrupo)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("El usuario no es administrador del grupo");
            }

            boolean eliminado = chatsRepository.deleteChat(idChat);
            if (!eliminado) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No se ha podido eliminar el chat");
            }

            return ResponseEntity.noContent()
                    .build();
        } catch (Exception e) {
            log.error("Error inesperado al eliminar el chat", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }
}
