package com.chatterbox.api_rest.controller;

import com.chatterbox.api_rest.dto.ChatMensajeDto;
import com.chatterbox.api_rest.dto.ChatMensajeRequestDto;
import com.chatterbox.api_rest.dto.MensajeDto;
import com.chatterbox.api_rest.service.ChatService;
import com.chatterbox.api_rest.service.MensajeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatService chatService;
    private final MensajeService mensajeService;

    @MessageMapping("/chat.enviar")
    public void recibirMensaje(ChatMensajeRequestDto peticion) {
        if (!chatService.usuarioPerteneceAlChat(peticion.getId_usuario(), peticion.getId_chat())) {
            log.warn("El usuario {} no pertenece al chat {}", peticion.getId_usuario(), peticion.getId_chat());
            return;
        }

        MensajeDto mensajeGuardado = mensajeService.guardarMensaje(peticion);

        ChatMensajeDto respuesta = new ChatMensajeDto(mensajeGuardado.getId_mensaje(), mensajeGuardado.getId_usuario(), mensajeGuardado.getContenido(), mensajeGuardado.getHora_envio());

        simpMessagingTemplate.convertAndSend("/topic/chat." + peticion.getId_chat(), respuesta);
    }
}
