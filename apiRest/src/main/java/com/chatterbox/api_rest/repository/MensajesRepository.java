package com.chatterbox.api_rest.repository;

import com.chatterbox.api_rest.dto.chat.ChatMensajeRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Repository
public class MensajesRepository {
    private final JdbcClient jdbcClient;

    @Transactional
    public Long insertMensaje(ChatMensajeRequestDto chatMensajeRequest, LocalDateTime horaEnvio) {
        jdbcClient.sql("INSERT INTO mensajes (id_usuario, id_chat, contenido, hora_envio) VALUES (?, ?, ?, ?)")
                .param(1, chatMensajeRequest.getId_usuario())
                .param(2, chatMensajeRequest.getId_chat())
                .param(3, chatMensajeRequest.getContenido())
                .param(4, horaEnvio)
                .update();

        return jdbcClient.sql("SELECT LAST_INSERT_ID()")
                .query(Long.class)
                .single();
    }
}
