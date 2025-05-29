package com.chatterbox.api_rest.dto.usuario_grupo;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class UsuarioDelGrupoDto {
    // Datos del usuario y de usuario_grupo (Repasar datos)
    private Long id_usuario;
    private String apodo;
    private String nombre_usuario;
    private String email;
    private String hash_password;
    private boolean es_admin_general;
    private String foto_perfil;
    private Long id_grupo;
    private boolean es_admin_grupo;
    private String fecha_inscripcion;
}
