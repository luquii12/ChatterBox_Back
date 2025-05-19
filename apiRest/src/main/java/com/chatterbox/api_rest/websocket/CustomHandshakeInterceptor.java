package com.chatterbox.api_rest.websocket;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
public class CustomHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) servletRequest.getServletRequest();
            String token = httpRequest.getParameter("token");
            String chatId = httpRequest.getParameter("chatId");
            if (token != null && !token.isBlank()) {
                attributes.put("token", token);
            }

            if (chatId != null) {
                try {
                    Long chatIdLong = Long.parseLong(chatId);
                    attributes.put("chatId", chatIdLong); // Guardar chatId en los atributos de la sesión
                } catch (NumberFormatException e) {
                    // Podrías lanzar excepción o ignorar, dependiendo de tu lógica
                    log.error("chatId no válido: " + chatId);
                    return false; // Cancela handshake si chatId no es válido
                }
            }
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        System.out.println("Después del handshake");
    }
}
