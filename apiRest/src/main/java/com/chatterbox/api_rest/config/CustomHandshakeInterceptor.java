package com.chatterbox.api_rest.config;

import org.springframework.web.socket.HandshakeInterceptor;
import org.springframework.web.socket.WebSocketSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class CustomHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(HttpServletRequest request, HttpServletResponse response,
                                   WebSocketSession wsSession, Map<String, Object> attributes) throws Exception {
        // Aquí puedes agregar lógica antes de que se complete el handshake, por ejemplo:
        System.out.println("Antes del handshake");
        return true; // Si devuelve false, el handshake será rechazado
    }

    @Override
    public void afterHandshake(HttpServletRequest request, HttpServletResponse response,
                               WebSocketSession wsSession, Exception exception) {
        // Aquí puedes agregar lógica después de que el handshake haya finalizado
        System.out.println("Después del handshake");
    }
}
