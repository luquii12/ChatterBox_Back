package com.chatterbox.api_rest.controller;

import com.chatterbox.api_rest.dto.grupo.GrupoEditDto;
import com.chatterbox.api_rest.service.GruposService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/grupos")
@RequiredArgsConstructor
public class GruposController {
    private final GruposService gruposService;

    @GetMapping("/{idGrupo}")
    public ResponseEntity<?> getGrupoById(@PathVariable Long idGrupo) {
        return gruposService.getGrupoById(idGrupo);
    }

    @GetMapping("/publicos/disponibles")
    public ResponseEntity<?> getGruposPublicosPorNombreWhereUsuarioNoEste(@RequestParam(required = false) String nombre, @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        return gruposService.getGruposPublicosPorNombreWhereUsuarioNoEste(nombre, pageable);
    }

    @GetMapping("/{idGrupo}/usuarios")
    public ResponseEntity<?> getAllUsuariosGrupoExceptoASiMismo(@PathVariable Long idGrupo, @PageableDefault(size = 10, sort = "apodo") Pageable pageable) {
        return gruposService.getAllUsuariosGrupoExceptoASiMismo(idGrupo, pageable);
    }

    @GetMapping("/{idGrupo}/chats")
    public ResponseEntity<?> getChatsDeUnGrupo(@PathVariable Long idGrupo) {
        return gruposService.getChatsDeUnGrupo(idGrupo);
    }

    @GetMapping("/{idGrupo}/foto")
    public ResponseEntity<?> getFotoGrupo(@PathVariable Long idGrupo) {
        return gruposService.getFotoGrupo(idGrupo);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createGrupo(@ModelAttribute GrupoEditDto nuevoGrupo) {
        return gruposService.createGrupo(nuevoGrupo);
    }

    @PostMapping("/{idGrupo}/join")
    public ResponseEntity<?> joinGrupo(@PathVariable Long idGrupo) {
        return gruposService.joinGrupo(idGrupo);
    }

    @PutMapping(value = "/{idGrupo}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> editGrupo(@PathVariable Long idGrupo, @ModelAttribute GrupoEditDto grupoModificado) {
        return gruposService.editGrupo(idGrupo, grupoModificado);
    }

    @PutMapping("/{idGrupo}/usuarios/{idUsuario}/roles/admin_grupo")
    public ResponseEntity<?> setAdminGrupo(@PathVariable Long idGrupo, @PathVariable Long idUsuario) {
        return gruposService.setAdminGrupo(idGrupo,idUsuario);
    }

    @DeleteMapping("/{idGrupo}/leave")
    public ResponseEntity<?> leaveGrupo(@PathVariable Long idGrupo) {
        return gruposService.leaveGrupo(idGrupo);
    }

    @DeleteMapping("/{idGrupo}/usuarios/{idUsuario}")
    public ResponseEntity<?> deleteUsuarioDelGrupo(@PathVariable Long idGrupo, @PathVariable Long idUsuario) {
        return gruposService.deleteUsuarioDelGrupo(idUsuario, idGrupo);
    }

    @DeleteMapping("/{idGrupo}/usuarios/{idUsuario}/roles/admin_grupo")
    public ResponseEntity<?> deleteAdminGrupo(@PathVariable Long idGrupo, @PathVariable Long idUsuario) {
        return gruposService.deleteAdminGrupo(idGrupo,idUsuario);
    }

    @DeleteMapping("/{idGrupo}")
    public ResponseEntity<?> deleteGrupo(@PathVariable Long idGrupo) {
        return gruposService.deleteGrupo(idGrupo);
    }
}
