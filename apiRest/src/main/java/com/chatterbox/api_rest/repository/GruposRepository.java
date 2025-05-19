package com.chatterbox.api_rest.repository;

import com.chatterbox.api_rest.dto.grupo.ChatDeUnGrupoDto;
import com.chatterbox.api_rest.dto.grupo.GrupoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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

    public Optional<GrupoDto> findGrupoByNombreAndIdUsuarioCreadorAndDifferentId(Long idGrupo, GrupoDto grupoModificado) {
        return jdbcClient.sql("SELECT * FROM grupos WHERE nombre_grupo = ? AND id_usuario_creador = ? AND id_grupo != ?")
                .param(1, grupoModificado.getNombre_grupo())
                .param(2, grupoModificado.getId_usuario_creador())
                .param(3, idGrupo)
                .query(GrupoDto.class)
                .optional();
    }

    public Long findIdUsuarioCreadorByIdGrupo(Long idGrupo) {
        return jdbcClient.sql("SELECT id_usuario_creador FROM grupos WHERE id_grupo = ?")
                .param(1, idGrupo)
                .query(Long.class)
                .single();
    }

    public String findFotoGrupoByIdGrupo(Long idGrupo) {
        return jdbcClient.sql("SELECT foto_grupo FROM grupos WHERE id_grupo = ?")
                .param(1, idGrupo)
                .query(String.class)
                .single();
    }

    public void updateGrupo(GrupoDto grupoModificado) {
        jdbcClient.sql("UPDATE grupos SET descripcion = ?, nombre_grupo = ?, es_privado = ?, foto_grupo = ? WHERE id_grupo = ?")
                .param(1, grupoModificado.getDescripcion())
                .param(2, grupoModificado.getNombre_grupo())
                .param(3, grupoModificado.isEs_privado())
                .param(4, grupoModificado.getFoto_grupo())
                .param(5, grupoModificado.getId_grupo())
                .update();
    }

    public boolean deleteUsuarioDelGrupo(Long idGrupo, Long idUsuarioAutenticado) {
        int filasEliminadas = jdbcClient.sql("DELETE FROM usuarios_grupos WHERE id_usuario = ? AND id_grupo = ?")
                .param(1, idUsuarioAutenticado)
                .param(2, idGrupo)
                .update();
        return filasEliminadas == 1;
    }

    public boolean usuarioPerteneceAlGrupo(Long idUsuarioAutenticado, Long idGrupo) {
        return jdbcClient.sql("SELECT 1 FROM usuarios_grupos WHERE id_usuario = ? AND id_grupo = ?")
                .param(1, idUsuarioAutenticado)
                .param(2, idGrupo)
                .query(Integer.class)
                .optional()
                .isPresent();
    }

    public int countGruposPublicosPorNombre(String nombre) {
        String nombreBusqueda = "%" + nombre + "%";

        Optional<Integer> numOptional = jdbcClient.sql("SELECT COUNT(*) FROM grupos WHERE es_privado = false AND LOWER(nombre_grupo) LIKE LOWER(?)")
                .param(1, nombreBusqueda)
                .query(Integer.class)
                .optional();

        return numOptional.orElse(0);
    }

    public List<GrupoDto> findGruposPublicosByNombre(String nombre, Pageable pageable) {
        int pageSize = pageable.getPageSize();
        int offset = (int) pageable.getOffset();
        String nombreBusqueda = "%" + nombre + "%";

        return jdbcClient.sql("SELECT * FROM grupos WHERE es_privado = false AND LOWER(nombre_grupo) LIKE LOWER(?) ORDER BY nombre_grupo LIMIT ? OFFSET ?")
                .param(1, nombreBusqueda)
                .param(2, pageSize)
                .param(3, offset)
                .query(GrupoDto.class)
                .list();
    }
}
