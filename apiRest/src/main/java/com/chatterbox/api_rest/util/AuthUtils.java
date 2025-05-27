package com.chatterbox.api_rest.util;

import com.chatterbox.api_rest.dto.usuario.UsuarioBdDto;
import com.chatterbox.api_rest.repository.UsuariosRepository;
import com.chatterbox.api_rest.security.UsuarioAutenticado;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component // Pq uso clases inyectadas por Spring Boot
public class AuthUtils {
    private final UsuariosRepository usuariosRepository;

    public AuthUtils(UsuariosRepository usuariosRepository) {
        this.usuariosRepository = usuariosRepository;
    }

    public Long obtenerIdDelToken() {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        UsuarioAutenticado usuario = (UsuarioAutenticado) authentication.getPrincipal();
        return usuario.id();
    }

    public boolean usuarioNoEncontrado(Long idUsuario) {
        Optional<UsuarioBdDto> usuarioAutenticadoOptional = usuariosRepository.findUsuarioById(idUsuario);
        return usuarioAutenticadoOptional.isEmpty();
    }

    public boolean esAdminGrupo(Long idUsuario, Long idGrupo) {
        return usuariosRepository.usuarioIsAdminGrupo(idUsuario, idGrupo);
    }
}
