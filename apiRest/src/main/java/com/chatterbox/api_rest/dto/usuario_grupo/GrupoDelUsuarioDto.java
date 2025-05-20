package com.chatterbox.api_rest.dto.usuario_grupo;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class GrupoDelUsuarioDto {
    private Long id_grupo;
    private Long id_usuario_creador;
    private String nombre_grupo;
    private String descripcion;
    private boolean es_privado;
    private String foto_grupo;
    private boolean es_admin_grupo = false;
    private String fecha_inscripcion;
}
