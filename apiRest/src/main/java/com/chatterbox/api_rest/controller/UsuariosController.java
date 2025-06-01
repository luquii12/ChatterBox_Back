package com.chatterbox.api_rest.controller;

import com.chatterbox.api_rest.dto.usuario.UsuarioRequestDto;
import com.chatterbox.api_rest.service.UsuariosService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuariosController {
    private final UsuariosService usuariosService;

    @GetMapping("/{idUsuario}")
    public ResponseEntity<?> getUsuarioPorId(@PathVariable Long idUsuario) {
        return usuariosService.getUsuarioById(idUsuario);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN_GENERAL')")
    public ResponseEntity<?> getAllUsuariosExceptoASiMismo(@PageableDefault(size = 10, sort = "apodo") Pageable pageable) {
        return usuariosService.getAllUsuariosExceptoASiMismo(pageable);
    }

    @GetMapping("/{idUsuario}/grupos")
    public ResponseEntity<?> getGruposDeUnUsuario(@PathVariable Long idUsuario) {
        return usuariosService.getGruposDelUsuario(idUsuario);
    }

    @GetMapping("/{idUsuario}/foto-perfil")
    public ResponseEntity<?> getFotoPerfil(@PathVariable Long idUsuario) {
        return usuariosService.getFotoPerfil(idUsuario);
    }

    @PutMapping(value = "/{idUsuario}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> editUsuario(@PathVariable Long idUsuario, @ModelAttribute UsuarioRequestDto usuarioModificado) {
        return usuariosService.editUsuario(idUsuario, usuarioModificado);
    }

    @PutMapping("/{idUsuario}/roles/admin_general")
    @PreAuthorize("hasRole('ADMIN_GENERAL')")
    public ResponseEntity<?> setAdminGeneral(@PathVariable Long idUsuario) {
        return usuariosService.setAdminGeneral(idUsuario);
    }

    @DeleteMapping("/{idUsuario}")
    @PreAuthorize("hasRole('ADMIN_GENERAL')")
    public ResponseEntity<?> deleteUsuario(@PathVariable Long idUsuario) {
        return usuariosService.deleteUsuario(idUsuario);
    }

    @DeleteMapping("/{idUsuario}/roles/admin_general")
    @PreAuthorize("hasRole('ADMIN_GENERAL')")
    public ResponseEntity<?> deleteAdminGeneral(@PathVariable Long idUsuario) {
        return usuariosService.deleteAdminGeneral(idUsuario);
    }
}
