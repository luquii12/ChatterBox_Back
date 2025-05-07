package com.chatterbox.api_rest.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ChatMensajeRequestDto {
    private Long id_chat;
    private Long id_usuario;
    private String contenido;
}
