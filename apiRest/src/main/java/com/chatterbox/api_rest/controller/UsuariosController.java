package com.chatterbox.api_rest.controller;

import com.chatterbox.api_rest.dto.usuario.UsuarioRequestDto;
import com.chatterbox.api_rest.service.UsuariosService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UsuariosController {
    private final UsuariosService usuariosService;

    @GetMapping("/{idUsuario}")
    public ResponseEntity<?> getUsuarioPorId(@PathVariable Long idUsuario) {
        return usuariosService.getUsuarioById(idUsuario);
    }

    // Opcional: Añadir paginación en el futuro para controlarlos mejor
    @GetMapping("/{idUsuario}/mensajes")
    public ResponseEntity<?> getMensajesDeUnUsuario(@PathVariable Long idUsuario) {
        return null;
    }

    @GetMapping("/{idUsuario}/grupos")
    public ResponseEntity<?> getGruposDeUnUsuario(@PathVariable Long idUsuario) {
        return usuariosService.getGruposDelUsuario(idUsuario);
    }

    @PutMapping("/{idUsuario}")
    public ResponseEntity<?> editUsuario(@PathVariable Long idUsuario, @RequestBody UsuarioRequestDto usuarioModificado) {
        return usuariosService.editUsuario(usuarioModificado);
    }
}
