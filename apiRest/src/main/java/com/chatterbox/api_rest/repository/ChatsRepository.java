package com.chatterbox.api_rest.repository;

import com.chatterbox.api_rest.dto.chat.ChatDto;
import com.chatterbox.api_rest.dto.chat.ChatMensajeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ChatsRepository {
    private final JdbcClient jdbcClient;

    public Optional<ChatDto> findChatById(Long idChat) {
        return jdbcClient.sql("SELECT id_chat,id_grupo,nombre_chat,DATE_FORMAT(fecha_creacion,'%Y-%m-%d %H:%i:%s') AS fecha_creacion FROM chats WHERE id_chat = ?")
                .param(1, idChat)
                .query(ChatDto.class)
                .optional();
    }

    public List<ChatMensajeDto> findMensajesByChatIdOrderByHoraEnvio(Long idChat) {
        return jdbcClient.sql("SELECT id_mensaje, id_usuario, contenido, SUBSTRING(DATE_FORMAT(hora_envio, '%Y-%m-%d %H:%i:%s.%f'), 1, 23) AS hora_envio FROM mensajes WHERE id_chat = ? ORDER BY hora_envio")
                .param(1, idChat)
                .query(ChatMensajeDto.class)
                .list();
    }

    public List<ChatMensajeDto> findMensajesByChatIdOrderByHoraEnvioLimitDeterminado(Long idChat, Integer limite) {
        return jdbcClient.sql("SELECT id_mensaje, id_usuario, contenido, SUBSTRING(DATE_FORMAT(hora_envio, '%Y-%m-%d %H:%i:%s.%f'), 1, 23) AS hora_envio FROM (SELECT * FROM mensajes WHERE id_chat = ? ORDER BY hora_envio DESC LIMIT ?) AS ultimos ORDER BY hora_envio")
                .param(1, idChat)
                .param(2, limite)
                .query(ChatMensajeDto.class)
                .list();
    }

    @Transactional
    public Long insertChat(ChatDto nuevoChat, LocalDateTime fechaCreacion) {
        jdbcClient.sql("INSERT INTO chats (id_grupo, nombre_chat, fecha_creacion) VALUES (?, ?, ?)")
                .param(1, nuevoChat.getId_grupo())
                .param(2, nuevoChat.getNombre_chat())
                .param(3, fechaCreacion)
                .update();
        return jdbcClient.sql("SELECT LAST_INSERT_ID()")
                .query(Long.class)
                .single();
    }

    public Long findIdGrupoByIdChat(Long idChat) {
        return jdbcClient.sql("SELECT id_grupo FROM chats WHERE id_chat = ?")
                .param(1, idChat)
                .query(Long.class)
                .optional()
                .orElse(null);
    }

    @Transactional
    public boolean deleteChat(Long idChat) {
        jdbcClient.sql("DELETE FROM mensajes WHERE id_chat = ?")
                .param(1, idChat)
                .update();

        int filasEliminadasChats = jdbcClient.sql("DELETE FROM chats WHERE id_chat = ?")
                .param(1, idChat)
                .update();

        return filasEliminadasChats == 1;
    }
}
