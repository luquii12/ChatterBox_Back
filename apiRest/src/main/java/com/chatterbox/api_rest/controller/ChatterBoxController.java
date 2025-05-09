package com.chatterbox.api_rest.controller;

import com.chatterbox.api_rest.service.ChatterBoxService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatterBoxController {
    private final ChatterBoxService chatterboxService;







    @GetMapping("/mensajes/{idMensaje}")
    public ResponseEntity<?> obtenerMensajePorId(@PathVariable Long idMensaje) {
        return null;
    }

    // Faltan los endpoints correspondientes a los chats y a los admins generales y de grupo
}
