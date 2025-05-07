package com.chatterbox.api_rest.service;

import com.chatterbox.api_rest.dto.UsuarioBdDto;
import com.chatterbox.api_rest.repository.ChatterBoxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    private final ChatterBoxRepository chatterBoxRepository;

    public boolean usuarioPerteneceAlChat(Long idUsuario, Long idChat) {
        try {
            Optional<UsuarioBdDto> usuarioBdOptional = chatterBoxRepository.findUsuarioByIdAndChatId(idUsuario, idChat);
            return usuarioBdOptional.isPresent();
        } catch (Exception e) {
            log.error("Error al verificar si el usuario {} pertenece al chat {}: {}", idUsuario, idChat, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
