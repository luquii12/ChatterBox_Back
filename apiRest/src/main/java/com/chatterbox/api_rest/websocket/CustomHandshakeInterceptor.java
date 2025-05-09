package com.chatterbox.api_rest.websocket;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

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
                attributes.put("chatId", chatId); // Guardar chatId en los atributos de la sesión
            }
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        System.out.println("Después del handshake");
    }
}
