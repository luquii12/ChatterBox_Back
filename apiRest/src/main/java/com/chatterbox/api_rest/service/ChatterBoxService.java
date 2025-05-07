package com.chatterbox.api_rest.service;

import com.chatterbox.api_rest.dto.*;
import com.chatterbox.api_rest.repository.ChatterBoxRepository;
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
public class ChatterBoxService {
    private final ChatterBoxRepository chatterboxRepository;

    public ResponseEntity<?> getUsuarioById(Long idUsuario) {
        try {
            Optional<UsuarioBdDto> usuarioOptional = chatterboxRepository.findUsuarioById(idUsuario);
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

    public ResponseEntity<?> obtenerGruposDeUnUsuario(Long idUsuario) {
        try {
            Optional<UsuarioBdDto> usuarioOptional = chatterboxRepository.findUsuarioById(idUsuario);
            if (usuarioOptional.isPresent()) {
                List<GrupoUsuarioDto> gruposUsuario = chatterboxRepository.findGruposByUsuarioIdOrderByFechaInscripcion(idUsuario);
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

    public ResponseEntity<?> obtenerChatsDeUnGrupo(Long idGrupo) {
        try {
            Optional<GrupoDto> grupoOptional = chatterboxRepository.findGrupoById(idGrupo);
            if (grupoOptional.isPresent()) {
                List<GrupoChatDto> chatsGrupo = chatterboxRepository.findChatsByGrupoIdOrderByFechaCreacion(idGrupo);
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

    public ResponseEntity<?> obtenerMensajesDeUnChat(Long idChat) {
        try {
            Optional<ChatDto> grupoOptional = chatterboxRepository.findChatById(idChat);
            if (grupoOptional.isPresent()) {
                List<ChatMensajeDto> mensajesChat = chatterboxRepository.findMensajesByChatIdOrderByHoraEnvio(idChat);
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
