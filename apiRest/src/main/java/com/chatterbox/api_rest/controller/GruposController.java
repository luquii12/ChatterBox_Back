package com.chatterbox.api_rest.controller;

import com.chatterbox.api_rest.dto.GrupoDto;
import com.chatterbox.api_rest.service.ChatterBoxService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/grupos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GruposController {
    private final ChatterBoxService chatterBoxService;

    @GetMapping("/{idGrupo}")
    public ResponseEntity<?> obtenerGrupoPorId(@PathVariable Long idGrupo) {
        return null;
    }

    @PostMapping("/buscar")
    public ResponseEntity<?> obtenerGrupoPorNombre(@RequestParam String nombre) {
        return null;
    }

    @PostMapping
    public ResponseEntity<?> crearGrupo(@RequestBody GrupoDto grupoDto) {
        return null;
    }

    @GetMapping("/{idGrupo}/administradores")
    public ResponseEntity<?> obtenerAdministradoresDeUnGrupo(@PathVariable Long idGrupo) {
        return null;
    }

    @GetMapping("/{idGrupo}/chats")
    public ResponseEntity<?> obtenerChatsDeUnGrupo(@PathVariable Long idGrupo) {
        return chatterBoxService.obtenerChatsDeUnGrupo(idGrupo);
    }

    // Puede que sea de chats y no grupos
    @GetMapping("/{idGrupo}/usuarios/{idUsuario}/mensajes")
    public ResponseEntity<?> obtenerMensajesDeUnUsuarioEnUnGrupo(@PathVariable Long idGrupo, @PathVariable Long idUsuario) {
        return null;
    }
}
