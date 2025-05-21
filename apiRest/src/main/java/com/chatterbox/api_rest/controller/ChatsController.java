package com.chatterbox.api_rest.controller;

import com.chatterbox.api_rest.dto.chat.ChatDto;
import com.chatterbox.api_rest.service.ChatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatsController {
    private final ChatsService chatsService;

    @GetMapping("/{idChat}/mensajes")
    public ResponseEntity<?> getMensajesDeUnChat(@PathVariable Long idChat, @RequestParam(required = false) Integer limite) {
        if (limite != null) return chatsService.getMensajesDelChat(idChat, limite);
        return chatsService.getAllMensajesDelChat(idChat);
    }

    @PostMapping
    public ResponseEntity<?> createChat(@RequestBody ChatDto nuevoChat) {
        return chatsService.createChat(nuevoChat);
    }

    @DeleteMapping("/{idChat}")
    public ResponseEntity<?> deleteChat(@PathVariable Long idChat) {
        return chatsService.deleteChat(idChat);
    }
}
