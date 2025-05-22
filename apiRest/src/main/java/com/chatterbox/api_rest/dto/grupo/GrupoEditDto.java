package com.chatterbox.api_rest.dto.grupo;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class GrupoEditDto {
    private Long id_usuario_creador;
    private String nombre_grupo;
    private String descripcion;
    private boolean es_privado = false;
    private MultipartFile foto_grupo;
}
