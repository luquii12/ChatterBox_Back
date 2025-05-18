package com.chatterbox.api_rest.dto.usuario;

import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class UsuarioRequestDto {
    private String apodo;
    private String nombre_usuario;
    private String email;
    private String password;
    private boolean es_admin_general;
    private String foto_perfil;

    public String getPasswordCifrada(PasswordEncoder passwordEncoder) {
        return passwordEncoder.encode(password);
    }
}
