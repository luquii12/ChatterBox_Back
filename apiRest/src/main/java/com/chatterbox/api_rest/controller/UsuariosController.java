package com.chatterbox.api_rest.controller;

import com.chatterbox.api_rest.dto.usuario.UsuarioRequestDto;
import com.chatterbox.api_rest.service.UsuariosService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/{idUsuario}/grupos")
    public ResponseEntity<?> getGruposDeUnUsuario(@PathVariable Long idUsuario) {
        return usuariosService.getGruposDelUsuario(idUsuario);
    }

    @PutMapping(value = "/{idUsuario}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> editUsuario(@PathVariable Long idUsuario, @ModelAttribute UsuarioRequestDto usuarioModificado) {
        return usuariosService.editUsuario(idUsuario, usuarioModificado);
    }

    @GetMapping("/{idUsuario}/foto-perfil")
    public ResponseEntity<?> getFotoPerfil(@PathVariable Long idUsuario) {
        return usuariosService.getFotoPerfil(idUsuario);
    }

    @GetMapping
        public ResponseEntity<?> getAllUsuarios(@PageableDefault(size = 10, sort = "apodo") Pageable pageable) {
        return usuariosService.getAllUsuarios(pageable);
    }
}
