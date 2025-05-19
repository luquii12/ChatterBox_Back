package com.chatterbox.api_rest.websocket;

import com.chatterbox.api_rest.dto.chat.ChatMensajeDto;
import com.chatterbox.api_rest.dto.chat.ChatMensajeRequestDto;
import com.chatterbox.api_rest.dto.mensaje.MensajeDto;
import com.chatterbox.api_rest.dto.usuario.UsuarioBdDto;
import com.chatterbox.api_rest.repository.UsuariosRepository;
import com.chatterbox.api_rest.security.JwtUtil;
import com.chatterbox.api_rest.service.ChatsService;
import com.chatterbox.api_rest.service.MensajesService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final JwtUtil jwtUtil;
    private final UsuariosRepository usuariosRepository;
    private final ChatsService chatsService;
    private final MensajesService mensajesService;
    private final ObjectMapper objectMapper = new ObjectMapper(); // Para leer JSON si hace falta
    // Guarda las sesiones activas por chat (solo en memoria mientras están conectados)
    private final Map<Long, Set<WebSocketSession>> sesionesPorChat = new ConcurrentHashMap<>();
    private final Map<WebSocketSession, UsuarioBdDto> usuariosPorSesion = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Obtener el token JWT de los atributos de la sesión (pasado desde el interceptor de handshake)
        String token = (String) session.getAttributes()
                .get("token");
        Long chatId = (Long) session.getAttributes()
                .get("chatId");

        if (token != null && !token.trim()
                .isBlank() && chatId != null) {
            // Validar y decodificar el token JWT
            String email = jwtUtil.getEmailFromToken(token);
            Long idUsuario = Long.valueOf(jwtUtil.getIdFromToken(token));
            List<GrantedAuthority> authorities = jwtUtil.getAuthoritiesFromToken(token);

            Optional<UsuarioBdDto> usuarioOptional = usuariosRepository.findUsuarioByEmail(email);
            if (usuarioOptional.isPresent() && jwtUtil.validateToken(token, usuarioOptional.get())) {
                // Asociar la sesión con el chatId correspondiente
                sesionesPorChat.computeIfAbsent(chatId, k -> ConcurrentHashMap.newKeySet())
                        .add(session); // Añadir la sesión al conjunto de sesiones del chat
                // Asociar la sesión con el usuario correspondiente
                usuariosPorSesion.put(session, usuarioOptional.get());

                return;
            }
        }

        session.close(CloseStatus.BAD_DATA);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        UsuarioBdDto usuarioBd = usuariosPorSesion.get(session);
        if (usuarioBd == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Usuario no autenticado"));
            return;
        }

        Long chatId = (Long) session.getAttributes()
                .get("chatId");
        if (chatId == null) {
            session.close(CloseStatus.BAD_DATA.withReason("ChatId no encontrado en sesión"));
            return;
        }

        String payload = message.getPayload();
        log.info("Mensaje recibido: {}", payload);

        // Convertir el mensaje JSON en objeto DTO solo para obtener el contenido
        ChatMensajeRequestDto peticion = objectMapper.readValue(payload, ChatMensajeRequestDto.class);

        if (!chatsService.usuarioEsMiembroDelChat(usuarioBd.getId_usuario(), chatId)) {
            log.warn("El usuario {} no pertenece al chat {}", usuarioBd.getId_usuario(), chatId);
            return;
        }

        peticion.setId_usuario(usuarioBd.getId_usuario());
        peticion.setId_chat(chatId);
        MensajeDto mensajeGuardado = mensajesService.guardarMensajeEnBD(peticion);
        ChatMensajeDto respuesta = new ChatMensajeDto(mensajeGuardado.getId_mensaje(), mensajeGuardado.getId_usuario(), mensajeGuardado.getContenido(), mensajeGuardado.getHora_envio());

        // Enviar el mensaje a todos los usuarios conectados al chat
        String jsonRespuesta = objectMapper.writeValueAsString(respuesta);
        for (WebSocketSession s : sesionesPorChat.get(chatId)) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(jsonRespuesta));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("Conexión cerrada: {}", closeStatus);

        // Eliminar la sesión cerrada de todos los chats donde esté
        sesionesPorChat.entrySet()
                .removeIf(entry -> {
                    Set<WebSocketSession> sesiones = entry.getValue();
                    sesiones.remove(session);
                    return sesiones.isEmpty(); // Limpiar el chat si no queda nadie conectado
                });

        // Eliminar el usuario asociado a la sesión
        usuariosPorSesion.remove(session);
    }

    @Override
    public void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        // Este método se usa para manejar mensajes binarios (si es necesario)
        // Actualmente no lo estoy usando, pero es obligatorio implementarlo
    }

    @Override
    public boolean supportsPartialMessages() {
        // Si el WebSocket maneja mensajes grandes en fragmentos (opcional)
        return false;
    }
}
