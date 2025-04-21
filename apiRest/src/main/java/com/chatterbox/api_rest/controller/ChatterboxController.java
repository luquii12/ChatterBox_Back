package com.chatterbox.api_rest.controller;

import com.chatterbox.api_rest.model.UsuarioGrupo;
import com.chatterbox.api_rest.service.ChatterboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatterboxController {
    private final ChatterboxService chatterboxService;

    // Falta determinar el tipo de mapping que van a ser los métodos
    // Los GetMapping pueden pasar a PostMapping si vienen de un formulario
    @GetMapping("/usuarios/{idUsuario}")
    public ResponseEntity<?> obtenerUsuarioPorId(@PathVariable int idUsuario) {
        return null;
    }

    // ¿RequestBody en vez de RequestParam?
    @PostMapping("/usuarios/buscar")
    public ResponseEntity<?> obtenerUsuarioPorNombreYApellidos(@RequestParam String nombre, @RequestParam String apellidos) {
        return null;
    }

    // Opcional: Añadir paginación en el futuro para controlarlos mejor
    @GetMapping("/usuarios/{idUsuario}/mensajes")
    public ResponseEntity<?> obtenerMensajesDeUnUsuario(@PathVariable int idUsuario) {
        return null;
    }

    @GetMapping("/usuarios/{idUsuario}/grupos")
    public ResponseEntity<?> obtenerGruposPorUsuario(@PathVariable int idUsuario) {
        return null;
    }

    @GetMapping("/grupos/{idGrupo}")
    public ResponseEntity<?> obtenerGrupoPorId(@PathVariable int idGrupo) {
        return null;
    }

    @PostMapping("/grupos/buscar")
    public ResponseEntity<?> obtenerGrupoPorNombre(@RequestParam String nombre) {
        return null;
    }

    @GetMapping("/grupos/{idGrupo}/usuarios")
    public ResponseEntity<?> obtenerUsuariosDeUnGrupo(@PathVariable int idGrupo) {
        return null;
    }

    @GetMapping("/grupos/{idGrupo}/administradores")
    public ResponseEntity<?> obtenerAdministradoresDeUnGrupo(@PathVariable int idGrupo) {
        return null;
    }

    // Opcional: Añadir paginación en el futuro para controlarlos mejor
    @GetMapping("/grupos/{idGrupo}/mensajes")
    public ResponseEntity<?> obtenerMensajesDeUnGrupo(@PathVariable int idGrupo) {
        return null;
    }

    @GetMapping("/grupos/{idGrupo}/usuarios/{idUsuario}/mensajes")
    public ResponseEntity<?> obtenerMensajesDeUnUsuarioEnUnGrupo(@PathVariable int idGrupo, @PathVariable int idUsuario) {
        return null;
    }

    @GetMapping("/mensajes/{idMensaje}")
    public ResponseEntity<?> obtenerMensajePorId(@PathVariable int idMensaje) {
        return null;
    }
}
