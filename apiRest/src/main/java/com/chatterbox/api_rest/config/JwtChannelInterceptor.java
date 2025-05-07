package com.chatterbox.api_rest.config;

import com.chatterbox.api_rest.dto.UsuarioBdDto;
import com.chatterbox.api_rest.repository.ChatterBoxRepository;
import com.chatterbox.api_rest.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
class JwtChannelInterceptor implements ChannelInterceptor {
    private final JwtUtil jwtUtil;
    private final ChatterBoxRepository chatterBoxRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Obtener el token del header Authorization
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String email = jwtUtil.getEmailFromToken(token);

                Optional<UsuarioBdDto> usuarioOpt = chatterBoxRepository.findUsuarioByEmail(email);
                if (usuarioOpt.isPresent()) {
                    UsuarioBdDto usuario = usuarioOpt.get();

                    if (jwtUtil.validateToken(token, usuario)) {
                        // Establece la autenticación para la sesión WebSocket
                        Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, List.of());
                        accessor.setUser(authentication);
                        return message;
                    }
                }
            }

            throw new IllegalArgumentException("Token JWT inválido o usuario no encontrado");
        }

        return message;
    }
}
