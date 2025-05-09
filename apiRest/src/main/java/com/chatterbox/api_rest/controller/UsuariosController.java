package com.chatterbox.api_rest.controller;

import com.chatterbox.api_rest.service.ChatterBoxService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UsuariosController {
    private final ChatterBoxService chatterBoxService;

    // Falta determinar el tipo de mapping que van a ser los métodos
    @GetMapping("/{idUsuario}") // No sé si lo necesito
    public ResponseEntity<?> obtenerUsuarioPorId(@PathVariable Long idUsuario) {
        return chatterBoxService.getUsuarioById(idUsuario);
    }

    // Opcional: Añadir paginación en el futuro para controlarlos mejor
    @GetMapping("/{idUsuario}/mensajes")
    public ResponseEntity<?> obtenerMensajesDeUnUsuario(@PathVariable Long idUsuario) {
        return null;
    }

    @GetMapping("/{idUsuario}/grupos")
    public ResponseEntity<?> obtenerGruposDeUnUsuario(@PathVariable Long idUsuario) {
        return chatterBoxService.obtenerGruposDeUnUsuario(idUsuario);
    }
}
