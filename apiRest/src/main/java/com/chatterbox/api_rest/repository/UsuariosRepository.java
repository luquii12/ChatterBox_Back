package com.chatterbox.api_rest.repository;

import com.chatterbox.api_rest.dto.usuario.UsuarioBdDto;
import com.chatterbox.api_rest.dto.usuario_grupo.GrupoDelUsuarioDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class UsuariosRepository {
    private final JdbcClient jdbcClient;

    public int countUsuariosExceptoASiMismo(Long idUsuario) {
        return jdbcClient.sql("SELECT COUNT(*) FROM usuarios WHERE id_usuario != ?")
                .param(1, idUsuario)
                .query(Integer.class)
                .single();
    }

    public int countAdminGeneral() {
        return jdbcClient.sql("SELECT COUNT(*) FROM usuarios WHERE es_admin_general = true")
                .query(Integer.class)
                .single();
    }

    public List<UsuarioBdDto> findAllUsuariosExceptoASiMismo(Long idUsuario, Pageable pageable) {
        int pageSize = pageable.getPageSize();
        int offset = (int) pageable.getOffset();

        return jdbcClient.sql("SELECT * FROM usuarios WHERE id_usuario != ? ORDER BY apodo LIMIT ? OFFSET ?")
                .param(1, idUsuario)
                .param(2, pageSize)
                .param(3, offset)
                .query(UsuarioBdDto.class)
                .list();
    }

    public Optional<UsuarioBdDto> findUsuarioById(Long idUsuario) {
        return jdbcClient.sql("SELECT * FROM usuarios WHERE id_usuario = ?")
                .param(1, idUsuario)
                .query(UsuarioBdDto.class)
                .optional();
    }

    public Optional<UsuarioBdDto> findUsuarioByIdAndChatId(Long idUsuario, Long idChat) {
        return jdbcClient.sql("SELECT u.* FROM usuarios u JOIN usuarios_grupos ug ON u.id_usuario = ug.id_usuario JOIN chats c ON ug.id_grupo = c.id_grupo WHERE u.id_usuario = ? AND c.id_chat = ?")
                .param(1, idUsuario)
                .param(2, idChat)
                .query(UsuarioBdDto.class)
                .optional();
    }

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

    public boolean usuarioIsAdminGrupo(Long idUsuario, Long idGrupo) {
        return jdbcClient.sql("SELECT es_admin_grupo FROM usuarios_grupos WHERE id_usuario = ? AND id_grupo = ?")
                .param(1, idUsuario)
                .param(2, idGrupo)
                .query(Boolean.class)
                .optional()
                .orElse(false);
    }

    public Long insertUsuario(UsuarioBdDto nuevoUsuario) {
        jdbcClient.sql("INSERT INTO usuarios (apodo, nombre_usuario, email, hash_password, foto_perfil) VALUES (?, ?, ?, ?, ?)")
                .param(1, nuevoUsuario.getApodo())
                .param(2, nuevoUsuario.getNombre_usuario())
                .param(3, nuevoUsuario.getEmail())
                .param(4, nuevoUsuario.getHash_password())
                .param(5, nuevoUsuario.getFoto_perfil())
                .update();

        return jdbcClient.sql("SELECT id_usuario FROM usuarios WHERE email = ?")
                .param(1, nuevoUsuario.getEmail())
                .query(Long.class)
                .single();
    }

    public List<GrupoDelUsuarioDto> findGruposByUsuarioIdOrderByFechaInscripcion(Long idUsuario) {
        return jdbcClient.sql("SELECT ug.es_admin_grupo, DATE_FORMAT(ug.fecha_inscripcion, '%Y-%m-%d %H:%i:%s') AS fecha_inscripcion, g.* FROM usuarios_grupos ug JOIN grupos g ON ug.id_grupo = g.id_grupo WHERE ug.id_usuario = ? ORDER BY ug.fecha_inscripcion")
                .param(1, idUsuario)
                .query(GrupoDelUsuarioDto.class)
                .list();
    }

    public Optional<UsuarioBdDto> findUsuarioByApodoOrEmailAndDifferentId(Long idUsuario, String apodo, String email) {
        return jdbcClient.sql("SELECT * FROM usuarios WHERE (apodo = ? OR email = ?) AND id_usuario != ?")
                .param(1, apodo)
                .param(2, email)
                .param(3, idUsuario)
                .query(UsuarioBdDto.class)
                .optional();
    }

    public String findFotoPerfilByIdUsuario(Long idUsuario) {
        return jdbcClient.sql("SELECT foto_perfil FROM usuarios WHERE id_usuario = ?")
                .param(1, idUsuario)
                .query(String.class)
                .single();
    }

    public String findHashPasswordByIdUsuario(Long idUsuario) {
        return jdbcClient.sql("SELECT hash_password FROM usuarios WHERE id_usuario = ?")
                .param(1, idUsuario)
                .query(String.class)
                .single();
    }

    public void updateUsuario(UsuarioBdDto usuarioBd) {
        jdbcClient.sql("UPDATE usuarios SET apodo = ?, nombre_usuario = ?, email = ?, hash_password = ?, foto_perfil = ? WHERE id_usuario = ?")
                .param(1, usuarioBd.getApodo())
                .param(2, usuarioBd.getNombre_usuario())
                .param(3, usuarioBd.getEmail())
                .param(4, usuarioBd.getHash_password())
                .param(5, usuarioBd.getFoto_perfil())
                .param(6, usuarioBd.getId_usuario())
                .update();
    }

    @Transactional
    public boolean deleteUsuario(Long idUsuario) {
        jdbcClient.sql("DELETE FROM usuarios_grupos WHERE id_usuario = ?")
                .param(1, idUsuario)
                .update();

        int filasEliminadasUsuarios = jdbcClient.sql("DELETE FROM usuarios WHERE id_usuario = ?")
                .param(1, idUsuario)
                .update();

        return filasEliminadasUsuarios == 1;
    }

    public void setAdminGeneral(Long idUsuario) {
        jdbcClient.sql("UPDATE usuarios SET es_admin_general = true WHERE id_usuario = ?")
                .param(1, idUsuario)
                .update();
    }

    public boolean deleteAdminGeneral(Long idUsuario) {
        int filasActualizadas = jdbcClient.sql("UPDATE usuarios SET es_admin_general = false WHERE id_usuario = ?")
                .param(1, idUsuario)
                .update();

        return filasActualizadas == 1;
    }

    @Transactional
    public UsuarioBdDto setAdminGrupoUsuarioMasLongevo(Long idGrupo) {
        Long idUsuario = jdbcClient.sql("SELECT id_usuario FROM usuarios_grupos WHERE id_grupo = ? ORDER BY fecha_inscripcion LIMIT 1")
                .param(1, idGrupo)
                .query(Long.class)
                .single();

        jdbcClient.sql("UPDATE usuarios_grupos SET es_admin_grupo = true WHERE id_usuario = ? AND id_grupo = ?")
                .param(1, idUsuario)
                .param(2, idGrupo)
                .update();

        return jdbcClient.sql("SELECT * FROM usuarios WHERE id_usuario = ?")
                .param(1, idUsuario)
                .query(UsuarioBdDto.class)
                .single();
    }
}
