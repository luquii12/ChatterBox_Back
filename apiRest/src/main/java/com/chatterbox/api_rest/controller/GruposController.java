package com.chatterbox.api_rest.controller;

import com.chatterbox.api_rest.dto.grupo.GrupoDto;
import com.chatterbox.api_rest.service.GruposService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/grupos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GruposController {
    private final GruposService gruposService;

    @GetMapping("/{idGrupo}")
    public ResponseEntity<?> getGrupoPorId(@PathVariable Long idGrupo) {
        return null;
    }

    @PostMapping("/buscar")
    public ResponseEntity<?> getGrupoPorNombre(@RequestParam String nombre) {
        return null;
    }

    @PostMapping
    public ResponseEntity<?> createGrupo(@RequestBody GrupoDto nuevoGrupo) {
        return gruposService.createGrupo(nuevoGrupo);
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinGrupo(@RequestBody GrupoDto nuevoGrupo) {
        return null;
    }

    @GetMapping("/{idGrupo}/administradores")
    public ResponseEntity<?> getAdministradoresDeUnGrupo(@PathVariable Long idGrupo) {
        return null;
    }

    @GetMapping("/{idGrupo}/chats")
    public ResponseEntity<?> getChatsDeUnGrupo(@PathVariable Long idGrupo) {
        return gruposService.getChatsDeUnGrupo(idGrupo);
    }

    // Puede que sea de chats y no grupos
    @GetMapping("/{idGrupo}/usuarios/{idUsuario}/mensajes")
    public ResponseEntity<?> getMensajesDeUnUsuarioEnUnGrupo(@PathVariable Long idGrupo, @PathVariable Long idUsuario) {
        return null;
    }
}
