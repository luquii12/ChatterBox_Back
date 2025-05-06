package com.chatterbox.api_rest.controller;

import com.chatterbox.api_rest.service.ChatterboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ChatterboxController {
    private final ChatterboxService chatterboxService;

    // Falta determinar el tipo de mapping que van a ser los métodos
    @GetMapping("/usuarios/{idUsuario}") // No sé si lo necesito
    public ResponseEntity<?> obtenerUsuarioPorId(@PathVariable Long idUsuario) {
        return chatterboxService.getUsuarioById(idUsuario);
    }

    // Opcional: Añadir paginación en el futuro para controlarlos mejor
    @GetMapping("/usuarios/{idUsuario}/mensajes")
    public ResponseEntity<?> obtenerMensajesDeUnUsuario(@PathVariable Long idUsuario) {
        return null;
    }

    @GetMapping("/usuarios/{idUsuario}/grupos")
    public ResponseEntity<?> obtenerGruposDeUnUsuario(@PathVariable Long idUsuario) {
        return chatterboxService.obtenerGruposDeUnUsuario(idUsuario);
    }

    @GetMapping("/grupos/{idGrupo}")
    public ResponseEntity<?> obtenerGrupoPorId(@PathVariable Long idGrupo) {
        return null;
    }

    @PostMapping("/grupos/buscar")
    public ResponseEntity<?> obtenerGrupoPorNombre(@RequestParam String nombre) {
        return null;
    }

    @GetMapping("/grupos/{idGrupo}/usuarios")
    public ResponseEntity<?> obtenerUsuariosDeUnGrupo(@PathVariable Long idGrupo) {
        return null;
    }

    @GetMapping("/grupos/{idGrupo}/administradores")
    public ResponseEntity<?> obtenerAdministradoresDeUnGrupo(@PathVariable Long idGrupo) {
        return null;
    }

    // Opcional: Añadir paginación en el futuro para controlarlos mejor
    @GetMapping("/grupos/{idGrupo}/mensajes")
    public ResponseEntity<?> obtenerMensajesDeUnGrupo(@PathVariable Long idGrupo) {
        return null;
    }

    @GetMapping("/grupos/{idGrupo}/usuarios/{idUsuario}/mensajes")
    public ResponseEntity<?> obtenerMensajesDeUnUsuarioEnUnGrupo(@PathVariable Long idGrupo, @PathVariable Long idUsuario) {
        return null;
    }

    @GetMapping("/mensajes/{idMensaje}")
    public ResponseEntity<?> obtenerMensajePorId(@PathVariable Long idMensaje) {
        return null;
    }

    // Faltan los endpoints correspondientes a los chats y a los admins generales y de grupo
}
