package com.chatterbox.api_rest.dto.grupo;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class GrupoDto {
    private Long id_grupo;
    private Long id_usuario_creador;
    private String nombre_grupo;
    private String descripcion;
    private boolean es_privado = false;
    private String foto_grupo;
}
