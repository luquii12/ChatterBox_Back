package com.chatterbox.api_rest.service;

import com.chatterbox.api_rest.dto.UsuarioBdDto;
import com.chatterbox.api_rest.repository.ChatterboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class ChatterboxService {
    private final ChatterboxRepository chatterboxRepository;

//    Mirar cómo añadir los try-catch

    public ResponseEntity<?> getUsuarioById(Long idUsuario) {
        Optional<UsuarioBdDto> usuarioOptional = chatterboxRepository.findUsuarioById(idUsuario);
        if (usuarioOptional.isPresent()) {
            return ResponseEntity.ok(usuarioOptional.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("No existe el usuario buscado");
    }
}
