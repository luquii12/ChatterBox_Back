package com.chatterbox.api_rest.repository;

import com.chatterbox.api_rest.dto.grupo.ChatDeUnGrupoDto;
import com.chatterbox.api_rest.dto.grupo.GrupoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class GruposRepository {
    private final JdbcClient jdbcClient;

    public Optional<GrupoDto> findGrupoById(Long idGrupo) {
        return jdbcClient.sql("SELECT * FROM grupos WHERE id_grupo = ?")
                .param(1, idGrupo)
                .query(GrupoDto.class)
                .optional();
    }

    public List<ChatDeUnGrupoDto> findChatsByGrupoIdOrderByFechaCreacion(Long idGrupo) {
        return jdbcClient.sql("SELECT id_chat, nombre_chat, DATE_FORMAT(fecha_creacion, '%Y-%m-%d %H:%i:%s') AS fecha_creacion FROM chats WHERE id_grupo = ? ORDER BY fecha_creacion")
                .param(1, idGrupo)
                .query(ChatDeUnGrupoDto.class)
                .list();
    }

    @Transactional
    public Long insertGrupo(GrupoDto nuevoGrupo) {
        jdbcClient.sql("INSERT INTO grupos (id_usuario_creador, descripcion, nombre_grupo, es_privado, foto_grupo) VALUES (?, ?, ?, ?, ?)")
                .param(1, nuevoGrupo.getId_usuario_creador())
                .param(2, nuevoGrupo.getDescripcion())
                .param(3, nuevoGrupo.getNombre_grupo())
                .param(4, nuevoGrupo.isEs_privado())
                .param(5, nuevoGrupo.getFoto_grupo())
                .update();

        Long id = jdbcClient.sql("SELECT LAST_INSERT_ID()")
                .query(Long.class)
                .single();

        jdbcClient.sql("INSERT INTO usuarios_grupos VALUES (?, ?, true, NOW())")
                .param(1, nuevoGrupo.getId_usuario_creador())
                .param(2, id)
                .update();

        return id;
    }
}
