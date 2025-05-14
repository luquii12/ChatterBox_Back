package com.chatterbox.api_rest.security;

import com.chatterbox.api_rest.dto.usuario.UsuarioBdDto;
import com.chatterbox.api_rest.repository.UsuariosRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UsuariosRepository usuariosRepository;

    public JwtFilter(JwtUtil jwtUtil, UsuariosRepository usuariosRepository) {
        this.jwtUtil = jwtUtil;
        this.usuariosRepository = usuariosRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            final String token = authHeader.substring(7);
            final String email = jwtUtil.getEmailFromToken(token);
            List<GrantedAuthority> authorities = jwtUtil.getAuthoritiesFromToken(token);

            if (email != null && SecurityContextHolder.getContext()
                    .getAuthentication() == null) {
                Optional<UsuarioBdDto> usuarioOptional = usuariosRepository.findUsuarioByEmail(email);
                UsuarioBdDto usuario = usuarioOptional.orElse(null);

                if (usuario == null || jwtUtil.isTokenExpired(token) || !jwtUtil.validateToken(token, usuario)) {
                    log.warn("Token inválido, usuario no encontrado o token expirado para el email: " + email);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter()
                            .write(usuario == null ? "Usuario no encontrado" : "Token inválido o expirado");
                    return;
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext()
                        .setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
