package com.chatterbox.api_rest.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.List;

public record UsuarioAutenticado(Long id, String email, List<GrantedAuthority> authorities) {
}
