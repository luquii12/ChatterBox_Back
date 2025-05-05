package com.chatterbox.api_rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class UsuarioRequestDto {
    private Long id_usuario;
    private String apodo;
    private String nombre_usuario;
    private String email;
    private String password;
    private boolean es_admin_general;
    private String foto_perfil;
}
