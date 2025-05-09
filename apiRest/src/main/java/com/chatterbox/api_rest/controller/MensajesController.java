package com.chatterbox.api_rest.controller;

import com.chatterbox.api_rest.service.websocket.MensajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mensajes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MensajesController {
    private final MensajeService mensajeService;

    @GetMapping("/mensajes/{idMensaje}")
    public ResponseEntity<?> getMensajePorId(@PathVariable Long idMensaje) {
        return null;
    }
}
