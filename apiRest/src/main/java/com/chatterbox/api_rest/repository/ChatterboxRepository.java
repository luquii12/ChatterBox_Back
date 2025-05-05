package com.chatterbox.api_rest.repository;

import com.chatterbox.api_rest.dto.UsuarioBdDto;
import com.chatterbox.api_rest.dto.UsuarioRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ChatterboxRepository {
    private final JdbcClient jdbcClient;

    public Optional<UsuarioBdDto> findUsuarioById(Long idUsuario) {
        return jdbcClient.sql("SELECT * FROM usuarios WHERE id_usuario = ?")
                .param(1, idUsuario)
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

    public Long insertUser(UsuarioRequestDto nuevoUsuario) {
        // Cambiar getPassword() por el método para encriptar
        jdbcClient.sql("INSERT INTO usuarios (apodo, nombre_usuario, email, hash_password) VALUES (?, ?, ?, ?)")
                .param(1, nuevoUsuario.getApodo())
                .param(2, nuevoUsuario.getNombre_usuario())
                .param(3, nuevoUsuario.getEmail())
                .param(4, nuevoUsuario.getPassword())
                .update();

        return jdbcClient.sql("SELECT id_usuario FROM usuarios WHERE email = ?")
                .param(1, nuevoUsuario.getEmail())
                .query(Long.class)
                .single();
    }
}
