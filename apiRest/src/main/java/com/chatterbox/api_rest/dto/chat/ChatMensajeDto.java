package com.chatterbox.api_rest.dto.chat;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ChatMensajeDto {
    private Long id_mensaje;
    private Long id_usuario;
    private String contenido;
    private String hora_envio;
}
