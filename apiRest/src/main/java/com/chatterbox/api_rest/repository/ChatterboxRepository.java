package com.chatterbox.api_rest.repository;

import com.chatterbox.api_rest.dto.UsuarioDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ChatterboxRepository {
    private final JdbcClient jdbcClient;

    public Optional<UsuarioDto> findUsuarioById(Long idUsuario) {
        return jdbcClient.sql("SELECT * FROM usuarios WHERE id_usuario = ?").param(1, idUsuario).query(UsuarioDto.class).optional();
    }

    public Optional<UsuarioDto> findUsuarioByEmail(String email) {
        return jdbcClient.sql("SELECT * FROM usuarios WHERE email = ?").param(1, email).query(UsuarioDto.class).optional();
    }
}
