package com.chatterbox.api_rest.dto.chat;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ChatDto {
    private Long id_chat;
    private Long id_grupo;
    private String nombre_chat;
    private String fecha_creacion;
}
