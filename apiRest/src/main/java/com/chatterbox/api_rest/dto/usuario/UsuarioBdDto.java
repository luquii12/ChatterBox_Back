package com.chatterbox.api_rest.dto.usuario;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class UsuarioBdDto {
    private Long id_usuario;
    private String apodo;
    private String nombre_usuario;
    private String email;
    private String hash_password;
    private boolean es_admin_general = false;
    private String foto_perfil = ""; // Poner url img por defecto

    public UsuarioBdDto(Long id_usuario, String apodo, String nombre_usuario, String email, String hash_password) {
        this.id_usuario = id_usuario;
        this.apodo = apodo;
        this.nombre_usuario = nombre_usuario;
        this.email = email;
        this.hash_password = hash_password;
    }
}
