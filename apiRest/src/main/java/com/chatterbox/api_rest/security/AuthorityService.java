package com.chatterbox.api_rest.security;

import com.chatterbox.api_rest.dto.usuario.UsuarioBdDto;
import com.chatterbox.api_rest.repository.ChatterBoxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class AuthorityService {
    private final ChatterBoxRepository chatterBoxRepository;

    public List<GrantedAuthority> getAuthoritiesForUsuario(String email) {
        Optional<UsuarioBdDto> usuarioOptional = chatterBoxRepository.findUsuarioByEmail(email);
        if (usuarioOptional.isEmpty()) {
            return List.of();
        }

        UsuarioBdDto usuario = usuarioOptional.get();
        List<GrantedAuthority> authorities = new ArrayList<>();

    }
}
