package com.chatterbox.api_rest.dto.usuario;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class UsuarioResponseDto {
    private Long id_usuario;
    private String apodo;
    private String nombre_usuario;
    private String email;
    private boolean es_admin_general;
    private String foto_perfil;
}
