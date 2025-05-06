package com.chatterbox.api_rest.repository;

import com.chatterbox.api_rest.dto.GrupoDto;
import com.chatterbox.api_rest.dto.UsuarioBdDto;
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

    public List<GrupoDto> findGruposByUsuarioId(Long idUsuario) {
        // Consulta a usuarios_grupos para obtener ids grupos y si el usuario es admin de él
        // y luego consulta a grupos para obtener la info de cada grupo
        //        return jdbcClient.sql("SELECT * FROM usuarios_grupos WHERE ")
        return null;
    }
}
