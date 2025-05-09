package com.chatterbox.api_rest.controller;

import com.chatterbox.api_rest.service.ChatterBoxService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatsController {
    private final ChatterBoxService chatterBoxService;

    @GetMapping("/{idChat}/mensajes")
    public ResponseEntity<?> obtenerMensajesDeUnChat(@PathVariable Long idChat) {
        return chatterBoxService.obtenerMensajesDeUnChat(idChat);
    }
}
