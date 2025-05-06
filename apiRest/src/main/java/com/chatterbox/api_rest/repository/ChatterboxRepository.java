package com.chatterbox.api_rest.repository;

import com.chatterbox.api_rest.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ChatterboxRepository {
    private final JdbcClient jdbcClient;

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

    public Long insertUser(UsuarioBdDto nuevoUsuario) {
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

    public Optional<UsuarioBdDto> findUsuarioById(Long idUsuario) {
        return jdbcClient.sql("SELECT * FROM usuarios WHERE id_usuario = ?")
                .param(1, idUsuario)
                .query(UsuarioBdDto.class)
                .optional();
    }

    public List<GrupoUsuarioDto> findGruposByUsuarioIdOrderByFechaInscripcion(Long idUsuario) {
        return jdbcClient.sql("SELECT ug.es_admin_grupo, DATE_FORMAT(ug.fecha_inscripcion, '%Y-%m-%d %H:%i:%s') AS fecha_inscripcion, g.* FROM usuarios_grupos ug JOIN grupos g ON ug.id_grupo = g.id_grupo WHERE ug.id_usuario = ? ORDER BY fecha_inscripcion")
                .param(1, idUsuario)
                .query(GrupoUsuarioDto.class)
                .list();
    }

    public Optional<GrupoDto> findGrupoById(Long idGrupo) {
        return jdbcClient.sql("SELECT * FROM grupos WHERE id_grupo = ?")
                .param(1, idGrupo)
                .query(GrupoDto.class)
                .optional();
    }

    public List<GrupoChatDto> findChatsByGrupoIdOrderByFechaCreacion(Long idGrupo) {
        return jdbcClient.sql("SELECT id_chat, nombre_chat, DATE_FORMAT(fecha_creacion, '%Y-%m-%d %H:%i:%s') AS fecha_creacion FROM chats WHERE id_grupo = ? ORDER BY fecha_creacion")
                .param(1, idGrupo)
                .query(GrupoChatDto.class)
                .list();
    }

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
}
