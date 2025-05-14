package com.chatterbox.api_rest.service;

import com.chatterbox.api_rest.dto.chat.ChatDto;
import com.chatterbox.api_rest.dto.chat.ChatMensajeDto;
import com.chatterbox.api_rest.dto.usuario.UsuarioBdDto;
import com.chatterbox.api_rest.repository.ChatsRepository;
import com.chatterbox.api_rest.repository.UsuariosRepository;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class ChatsService {
    private final ChatsRepository chatsRepository;
    private final UsuariosRepository usuariosRepository;

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

    public boolean usuarioEsMiembroDelChat(Long idUsuario, Long idChat) {
        try {
            Optional<UsuarioBdDto> usuarioBdOptional = usuariosRepository.findUsuarioByIdAndChatId(idUsuario, idChat);
            return usuarioBdOptional.isPresent();
        } catch (Exception e) {
            log.error("Error al verificar si el usuario {} pertenece al chat {}: {}", idUsuario, idChat, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
