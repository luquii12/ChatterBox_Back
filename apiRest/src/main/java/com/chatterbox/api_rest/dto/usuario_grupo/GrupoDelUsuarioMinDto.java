package com.chatterbox.api_rest.dto.usuario_grupo;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class GrupoDelUsuarioMinDto {
    private Long id_usuario;
    private Long id_grupo;
    private boolean es_admin_grupo;
    private String fecha_inscripcion;
}
