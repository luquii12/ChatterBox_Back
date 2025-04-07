package com.chatterbox.api_rest.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class UsuarioGrupoId implements Serializable {
    private Long idUsuario;
    private Long idGrupo;
}
