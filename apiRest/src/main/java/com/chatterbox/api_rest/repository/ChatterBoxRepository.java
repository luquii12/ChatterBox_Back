package com.chatterbox.api_rest.repository;

import com.chatterbox.api_rest.dto.chat.ChatMensajeRequestDto;
import com.chatterbox.api_rest.dto.usuario.UsuarioBdDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ChatterBoxRepository {
    private final JdbcClient jdbcClient;

    // Por ahora voy a tener los métodos que son de las clases de Auth, JWT y WebSocket, si aumentan mucho lo dividiré en varios

    public Optional<UsuarioBdDto> findUsuarioByEmail(String email) {
        return jdbcClient.sql("SELECT * FROM usuarios WHERE email = ?")
                .param(1, email)
                .query(UsuarioBdDto.class)
                .optional();
    }

    public Optional<UsuarioBdDto> findUsuarioByApodoOrEmail(String apodo, String email) {
        return jdbcClient.sql("SELECT * FROM usuarios WHERE apodo = ? OR email = ?")
                .param(1, apodo)
                .param(2, email)
                .query(UsuarioBdDto.class)
                .optional();
    }

    public Long insertUsuario(UsuarioBdDto nuevoUsuario) {
        jdbcClient.sql("INSERT INTO usuarios (apodo, nombre_usuario, email, hash_password) VALUES (?, ?, ?, ?)")
                .param(1, nuevoUsuario.getApodo())
                .param(2, nuevoUsuario.getNombre_usuario())
                .param(3, nuevoUsuario.getEmail())
                .param(4, nuevoUsuario.getHash_password())
                .update();

        return jdbcClient.sql("SELECT id_usuario FROM usuarios WHERE email = ?")
                .param(1, nuevoUsuario.getEmail())
                .query(Long.class)
                .single();
    }

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

    public Optional<UsuarioBdDto> findUsuarioByIdAndChatId(Long idUsuario, Long idChat) {
        return jdbcClient.sql("SELECT u.* FROM usuarios u JOIN usuarios_grupos ug ON u.id_usuario = ug.id_usuario JOIN chats c ON ug.id_grupo = c.id_grupo WHERE u.id_usuario = ? AND c.id_chat = ?")
                .param(1, idUsuario)
                .param(2, idChat)
                .query(UsuarioBdDto.class)
                .optional();
    }
}
