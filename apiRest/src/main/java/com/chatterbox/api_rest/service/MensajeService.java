package com.chatterbox.api_rest.service;

import com.chatterbox.api_rest.dto.ChatMensajeRequestDto;
import com.chatterbox.api_rest.dto.MensajeDto;
import com.chatterbox.api_rest.repository.ChatterBoxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class MensajeService {
    private final ChatterBoxRepository chatterBoxRepository;

    public MensajeDto guardarMensaje(ChatMensajeRequestDto chatMensajeRequest) {
        LocalDateTime horaActual = LocalDateTime.now();
        try {
            Long id = chatterBoxRepository.insertMensaje(chatMensajeRequest, horaActual);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String horaFormateada = horaActual.format(formatter);

            return new MensajeDto(id, chatMensajeRequest.getId_usuario(), chatMensajeRequest.getId_chat(), chatMensajeRequest.getContenido(), horaFormateada);
        } catch (Exception e) {
            log.error("Error al insertar el mensaje para el usuario {} en el chat {}: {}", chatMensajeRequest.getId_usuario(), chatMensajeRequest.getId_chat(), e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
