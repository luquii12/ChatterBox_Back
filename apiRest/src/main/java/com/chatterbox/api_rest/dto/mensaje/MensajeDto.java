package com.chatterbox.api_rest.dto.mensaje;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class MensajeDto {
    private Long id_mensaje;
    private Long id_usuario;
    private Long id_chat;
    private String contenido;
    private String hora_envio;
}
