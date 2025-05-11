package com.chatterbox.api_rest.dto.usuario;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class UsuarioBdDto {
    private Long id_usuario;
    private String apodo;
    private String nombre_usuario;
    private String email;
    private String hash_password;
    @Builder.Default // Para que al usar builder() se asignen automáticamente los valores por defecto
    private boolean es_admin_general = false;
    @Builder.Default
    private String foto_perfil = ""; // Poner url img por defecto
}
