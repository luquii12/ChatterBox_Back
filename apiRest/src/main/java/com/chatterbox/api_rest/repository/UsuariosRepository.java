package com.chatterbox.api_rest.repository;

import com.chatterbox.api_rest.dto.usuario.UsuarioBdDto;
import com.chatterbox.api_rest.dto.usuario_grupo.GrupoDelUsuarioDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class UsuariosRepository {
    private final JdbcClient jdbcClient;

    public Optional<UsuarioBdDto> findUsuarioById(Long idUsuario) {
        return jdbcClient.sql("SELECT * FROM usuarios WHERE id_usuario = ?")
                .param(1, idUsuario)
                .query(UsuarioBdDto.class)
                .optional();
    }

    public List<GrupoDelUsuarioDto> findGruposByUsuarioIdOrderByFechaInscripcion(Long idUsuario) {
        return jdbcClient.sql("SELECT ug.es_admin_grupo, DATE_FORMAT(ug.fecha_inscripcion, '%Y-%m-%d %H:%i:%s') AS fecha_inscripcion, g.* FROM usuarios_grupos ug JOIN grupos g ON ug.id_grupo = g.id_grupo WHERE ug.id_usuario = ? ORDER BY ug.fecha_inscripcion")
                .param(1, idUsuario)
                .query(GrupoDelUsuarioDto.class)
                .list();
    }
}
