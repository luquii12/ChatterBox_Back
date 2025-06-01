package com.chatterbox.api_rest.dto.usuario_grupo;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class UsuarioDelGrupoDto {
    private Long id_usuario;
    private String apodo;
    private String nombre_usuario;
    private String email;
    private String foto_perfil;
    private Long id_grupo;
    private boolean es_admin_grupo;
    private String fecha_inscripcion;
}
