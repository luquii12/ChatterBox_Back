package com.chatterbox.api_rest.controller;

import com.chatterbox.api_rest.dto.grupo.GrupoDto;
import com.chatterbox.api_rest.service.GruposService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/grupos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GruposController {
    private final GruposService gruposService;

    @PostMapping
    public ResponseEntity<?> createGrupo(@RequestBody GrupoDto nuevoGrupo) {
        return gruposService.createGrupo(nuevoGrupo);
    }

    @GetMapping("/publicos/disponibles")
    public ResponseEntity<?> getGruposPublicosPorNombreWhereUsuarioNoEste(@RequestParam(required = false) String nombre, @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        return gruposService.getGruposPublicosPorNombreWhereUsuarioNoEste(nombre, pageable);
    }

    @PostMapping("/{idGrupo}/join")
    public ResponseEntity<?> joinGrupo(@PathVariable Long idGrupo) {
        return gruposService.joinGrupo(idGrupo);
    }

    @PutMapping("/{idGrupo}")
    public ResponseEntity<?> editGrupo(@PathVariable Long idGrupo, @RequestBody GrupoDto grupoModificado) {
        return gruposService.editGrupo(idGrupo, grupoModificado);
    }

    @DeleteMapping("/{idGrupo}/leave")
    public ResponseEntity<?> leaveGrupo(@PathVariable Long idGrupo) {
        return gruposService.leaveGrupo(idGrupo);
    }

    @DeleteMapping("/{idGrupo}")
    public ResponseEntity<?> deleteGrupo(@PathVariable Long idGrupo) {
        return gruposService.deleteGrupo(idGrupo);
    }

    @GetMapping("/{idGrupo}/chats")
    public ResponseEntity<?> getChatsDeUnGrupo(@PathVariable Long idGrupo) {
        return gruposService.getChatsDeUnGrupo(idGrupo);
    }

    @GetMapping("/{idGrupo}")
    public ResponseEntity<?> getGrupoPorId(@PathVariable Long idGrupo) {
        return null;
    }

    @GetMapping("/{idGrupo}/administradores")
    public ResponseEntity<?> getAdministradoresDeUnGrupo(@PathVariable Long idGrupo) {
        return null;
    }

    // Puede que sea de chats y no grupos
    @GetMapping("/{idGrupo}/usuarios/{idUsuario}/mensajes")
    public ResponseEntity<?> getMensajesDeUnUsuarioEnUnGrupo(@PathVariable Long idGrupo, @PathVariable Long idUsuario) {
        return null;
    }
}
