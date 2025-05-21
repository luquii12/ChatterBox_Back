package com.chatterbox.api_rest.controller;

import com.chatterbox.api_rest.service.MensajesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mensajes")
@RequiredArgsConstructor
public class MensajesController {
    private final MensajesService mensajesService;

    @GetMapping("/mensajes/{idMensaje}")
    public ResponseEntity<?> getMensajePorId(@PathVariable Long idMensaje) {
        return null;
    }
}
