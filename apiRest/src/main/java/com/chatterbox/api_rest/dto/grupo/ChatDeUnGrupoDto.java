package com.chatterbox.api_rest.dto.grupo;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ChatDeUnGrupoDto {
    private Long id_chat;
    private String nombre_chat;
    private String fecha_creacion;
}
