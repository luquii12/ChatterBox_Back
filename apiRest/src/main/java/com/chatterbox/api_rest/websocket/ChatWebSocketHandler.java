package com.chatterbox.api_rest.websocket;

import com.chatterbox.api_rest.dto.chat.ChatMensajeDto;
import com.chatterbox.api_rest.dto.chat.ChatMensajeRequestDto;
import com.chatterbox.api_rest.dto.mensaje.MensajeDto;
import com.chatterbox.api_rest.dto.usuario.UsuarioBdDto;
import com.chatterbox.api_rest.repository.ChatterBoxRepository;
import com.chatterbox.api_rest.security.JwtUtil;
import com.chatterbox.api_rest.service.websocket.ChatService;
import com.chatterbox.api_rest.service.websocket.MensajeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
    private final ChatterBoxRepository chatterBoxRepository;
    private final ChatService chatService;
    private final MensajeService mensajeService;
    private final ObjectMapper objectMapper = new ObjectMapper(); // para leer JSON si hace falta
    // Guarda las sesiones activas por chat (solo en memoria mientras están conectados)
    private final Map<Long, Set<WebSocketSession>> sesionesPorChat = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Obtener el token JWT de los atributos de la sesión (pasado desde el interceptor de handshake)
        String token = (String) session.getAttributes()
                .get("token");
        String chatId = (String) session.getAttributes()
                .get("chatId");

        if (token != null && !token.isBlank() && chatId != null && !chatId.isBlank()) {
            // Validar y decodificar el token JWT
            String email = jwtUtil.getEmailFromToken(token);

            Optional<UsuarioBdDto> usuarioOptional = chatterBoxRepository.findUsuarioByEmail(email);
            if (usuarioOptional.isPresent() && jwtUtil.validateToken(token, usuarioOptional.get())) {
                // Aquí puedes asociar la autenticación al WebSocket session si es necesario
                Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, List.of());
                session.getAttributes()
                        .put("usuario", authentication); // Guardamos el usuario autenticado en los atributos de la sesión

                // Asociar la sesión con el chatId correspondiente
                Long chatIdLong = Long.parseLong(chatId); // Convertir el chatId de String a Long
                sesionesPorChat
                        .computeIfAbsent(chatIdLong, k -> ConcurrentHashMap.newKeySet())
                        .add(session); // Añadir la sesión al conjunto de sesiones del chat
                return;
            }
        }

        session.close(CloseStatus.BAD_DATA);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("Mensaje recibido: {}", payload);

        // Convertir el mensaje JSON en objeto DTO
        ChatMensajeRequestDto peticion = objectMapper.readValue(payload, ChatMensajeRequestDto.class);

        if (!chatService.usuarioEsMiembroDelChat(peticion.getId_usuario(), peticion.getId_chat())) {
            log.warn("El usuario {} no pertenece al chat {}", peticion.getId_usuario(), peticion.getId_chat());
            return;
        }

        MensajeDto mensajeGuardado = mensajeService.guardarMensajeEnBD(peticion);
        ChatMensajeDto respuesta = new ChatMensajeDto(
                mensajeGuardado.getId_mensaje(),
                mensajeGuardado.getId_usuario(),
                mensajeGuardado.getContenido(),
                mensajeGuardado.getHora_envio()
        );

        // Enviar el mensaje a todos los usuarios conectados al chat
        String jsonRespuesta = objectMapper.writeValueAsString(respuesta);
        for (WebSocketSession s : sesionesPorChat.get(peticion.getId_chat())) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(jsonRespuesta));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("Conexión cerrada: {}", closeStatus);

        // Eliminamos la sesión cerrada de todos los chats donde esté
        sesionesPorChat.entrySet()
                .removeIf(entry -> {
                    Set<WebSocketSession> sesiones = entry.getValue();
                    sesiones.remove(session);
                    return sesiones.isEmpty(); // limpiamos el chat si no queda nadie conectado
                });
    }

    @Override
    public void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        // Este método se usa para manejar mensajes binarios (si es necesario)
        // Actualmente no lo estamos usando, pero es obligatorio implementarlo
    }

    @Override
    public boolean supportsPartialMessages() {
        // Si el WebSocket maneja mensajes grandes en fragmentos (opcional)
        return false;
    }
}

