package com.chatterbox.api_rest.repository;

import com.chatterbox.api_rest.dto.grupo.ChatDeUnGrupoDto;
import com.chatterbox.api_rest.dto.grupo.GrupoDto;
import com.chatterbox.api_rest.dto.grupo.GrupoEditDto;
import com.chatterbox.api_rest.dto.usuario_grupo.UsuarioDelGrupoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        jdbcClient.sql("INSERT INTO grupos (id_usuario_creador, descripcion, nombre_grupo, es_privado) VALUES (?, ?, ?, ?)")
                .param(1, nuevoGrupo.getId_usuario_creador())
                .param(2, nuevoGrupo.getDescripcion())
                .param(3, nuevoGrupo.getNombre_grupo())
                .param(4, nuevoGrupo.isEs_privado())
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

    public void updateFotoGrupo(Long idGrupo, String fotoGrupo) {
        jdbcClient.sql("UPDATE grupos SET foto_grupo = ? WHERE id_grupo = ?")
                .param(1, fotoGrupo)
                .param(2, idGrupo)
                .update();
    }

    public Optional<GrupoDto> findGrupoByNombreAndIdUsuarioCreadorAndDifferentId(Long idGrupo, GrupoEditDto grupoModificado) {
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

    public boolean deleteUsuarioDelGrupo(Long idGrupo, Long idUsuario) {
        int filasEliminadas = jdbcClient.sql("DELETE FROM usuarios_grupos WHERE id_usuario = ? AND id_grupo = ?")
                .param(1, idUsuario)
                .param(2, idGrupo)
                .update();

        return filasEliminadas == 1;
    }

    public boolean usuarioPerteneceAlGrupo(Long idUsuario, Long idGrupo) {
        return jdbcClient.sql("SELECT 1 FROM usuarios_grupos WHERE id_usuario = ? AND id_grupo = ?")
                .param(1, idUsuario)
                .param(2, idGrupo)
                .query(Integer.class)
                .optional()
                .isPresent();
    }

    public int countGruposPublicosPorNombreWhereUsuarioNoEste(String nombre, Long idUsuario) {
        String nombreBusqueda = "%" + nombre + "%";

        return jdbcClient.sql("SELECT COUNT(*) FROM grupos WHERE es_privado = false AND LOWER(nombre_grupo) LIKE LOWER(?) AND id_grupo NOT IN (SELECT id_grupo FROM usuarios_grupos WHERE id_usuario = ?)")
                .param(1, nombreBusqueda)
                .param(2, idUsuario)
                .query(Integer.class)
                .single();
    }

    public List<GrupoDto> findGruposPublicosByNombreWhereUsuarioNoEste(String nombre, Long idUsuario, Pageable pageable) {
        int pageSize = pageable.getPageSize();
        int offset = (int) pageable.getOffset();
        String nombreBusqueda = "%" + nombre + "%";

        return jdbcClient.sql("SELECT * FROM grupos WHERE es_privado = false AND LOWER(nombre_grupo) LIKE LOWER(?) AND id_grupo NOT IN (SELECT id_grupo FROM usuarios_grupos WHERE id_usuario = ?) ORDER BY nombre_grupo LIMIT ? OFFSET ?")
                .param(1, nombreBusqueda)
                .param(2, idUsuario)
                .param(3, pageSize)
                .param(4, offset)
                .query(GrupoDto.class)
                .list();
    }

    public void insertUsuarioGrupo(Long idUsuario, Long idGrupo, LocalDateTime fechaInscripcion) {
        jdbcClient.sql("INSERT INTO usuarios_grupos (id_usuario, id_grupo, fecha_inscripcion) VALUES (?, ?, ?)")
                .param(1, idUsuario)
                .param(2, idGrupo)
                .param(3, fechaInscripcion)
                .update();
    }

    @Transactional
    public boolean deleteGrupo(Long idGrupo) {
        jdbcClient.sql("DELETE FROM usuarios_grupos WHERE id_grupo = ?")
                .param(1, idGrupo)
                .update();

        int filasEliminadasGrupos = jdbcClient.sql("DELETE FROM grupos WHERE id_grupo = ?")
                .param(1, idGrupo)
                .update();

        return filasEliminadasGrupos == 1;
    }

    public String findFotoPerfilByIdGrupo(Long idGrupo) {
        return jdbcClient.sql("SELECT foto_grupo FROM grupos WHERE id_grupo = ?")
                .param(1, idGrupo)
                .query(String.class)
                .single();
    }

    public int countUsuariosGrupo(Long idGrupo) {
        return jdbcClient.sql("SELECT COUNT(*) FROM usuarios_grupos WHERE id_grupo = ?")
                .param(1, idGrupo)
                .query(Integer.class)
                .single();
    }

    public int countAdminGrupo(Long idGrupo) {
        return jdbcClient.sql("SELECT COUNT(*) FROM usuarios_grupos WHERE id_grupo = ? AND es_admin_grupo = true")
                .param(1, idGrupo)
                .query(Integer.class)
                .single();
    }

    public int countUsuariosGrupoExceptoASiMismo(Long idGrupo, Long idUsuario) {
        return jdbcClient.sql("SELECT COUNT(*) FROM usuarios_grupos WHERE id_grupo = ? AND id_usuario != ?")
                .param(1, idGrupo)
                .param(2, idUsuario)
                .query(Integer.class)
                .single();
    }

    public List<UsuarioDelGrupoDto> findAllUsuariosGrupoExceptoASiMismo(Long idGrupo, Long idUsuario, Pageable pageable) {
        int pageSize = pageable.getPageSize();
        int offset = (int) pageable.getOffset();

        return jdbcClient.sql("SELECT u.*, ug.id_grupo, ug.es_admin_grupo, ug.fecha_inscripcion FROM usuarios u JOIN usuarios_grupos ug ON u.id_usuario = ug.id_usuario WHERE u.id_usuario != ? AND ug.id_grupo = ? ORDER BY u.apodo LIMIT ? OFFSET ?")
                .param(1, idUsuario)
                .param(2, idGrupo)
                .param(3, pageSize)
                .param(4, offset)
                .query(UsuarioDelGrupoDto.class)
                .list();
    }

    public boolean usuarioIsCreadorGrupo(Long idUsuario, Long idGrupo) {
        return jdbcClient.sql("SELECT 1 FROM grupos WHERE id_grupo = ? AND id_usuario_creador = ?")
                .param(1, idGrupo)
                .param(2, idUsuario)
                .query(Integer.class)
                .optional()
                .isPresent();
    }

    @Transactional
    public UsuarioDelGrupoDto setAdminGrupo(Long idGrupo, Long idUsuario) {
        jdbcClient.sql("UPDATE usuarios_grupos SET es_admin_grupo = true WHERE id_grupo = ? AND id_usuario = ?")
                .param(1, idGrupo)
                .param(2, idUsuario)
                .update();

        return jdbcClient.sql("SELECT u.*, ug.id_grupo, ug.es_admin_grupo, ug.fecha_inscripcion FROM usuarios u JOIN usuarios_grupos ug ON u.id_usuario = ug.id_usuario WHERE u.id_usuario = ? AND ug.id_grupo = ?")
                .param(1, idUsuario)
                .param(2, idGrupo)
                .query(UsuarioDelGrupoDto.class)
                .single();
    }

    @Transactional
    public UsuarioDelGrupoDto deleteAdminGrupo(Long idGrupo, Long idUsuario) {
        jdbcClient.sql("UPDATE usuarios_grupos SET es_admin_grupo = false WHERE id_grupo = ? AND id_usuario = ?")
                .param(1, idGrupo)
                .param(2, idUsuario)
                .update();

        return jdbcClient.sql("SELECT u.*, ug.id_grupo, ug.es_admin_grupo, ug.fecha_inscripcion FROM usuarios u JOIN usuarios_grupos ug ON u.id_usuario = ug.id_usuario WHERE u.id_usuario = ? AND ug.id_grupo = ?")
                .param(1, idUsuario)
                .param(2, idGrupo)
                .query(UsuarioDelGrupoDto.class)
                .single();
    }
}
