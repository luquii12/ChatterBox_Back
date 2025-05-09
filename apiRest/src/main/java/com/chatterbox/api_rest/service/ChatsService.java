package com.chatterbox.api_rest.service;

import com.chatterbox.api_rest.dto.chat.ChatDto;
import com.chatterbox.api_rest.dto.chat.ChatMensajeDto;
import com.chatterbox.api_rest.repository.ChatsRepository;
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

    public ResponseEntity<?> getMensajesDeUnChat(Long idChat) {
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
}
