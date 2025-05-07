package com.chatterbox.api_rest.security;

import com.chatterbox.api_rest.dto.UsuarioBdDto;
import com.chatterbox.api_rest.repository.ChatterBoxRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final ChatterBoxRepository chatterboxRepository;

    public JwtFilter(JwtUtil jwtUtil, ChatterBoxRepository chatterboxRepository) {
        this.jwtUtil = jwtUtil;
        this.chatterboxRepository = chatterboxRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            final String token = authHeader.substring(7);
            final String email = jwtUtil.getEmailFromToken(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Optional<UsuarioBdDto> usuarioOptional = chatterboxRepository.findUsuarioByEmail(email);
                UsuarioBdDto usuario = usuarioOptional.orElse(null);

                if (usuario == null || jwtUtil.isTokenExpired(token) || !jwtUtil.validateToken(token, usuario)) {
                    log.warn("Token inválido, usuario no encontrado o token expirado para el email: " + email);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write(usuario == null ? "Usuario no encontrado" : "Token inválido o expirado");
                    return;
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, null, null);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
