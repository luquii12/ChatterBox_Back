package com.chatterbox.api_rest.service;

import com.chatterbox.api_rest.dto.grupo.ChatDeUnGrupoDto;
import com.chatterbox.api_rest.dto.grupo.GrupoDto;
import com.chatterbox.api_rest.repository.GruposRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class GruposService {
    private final GruposRepository gruposRepository;

    public ResponseEntity<?> createGrupo(GrupoDto nuevoGrupo) {
        // ¿Usar GrupoDto está bien o me creo otro DTO para recibir los datos del nuevo grupo?
        List<String> atributos = Arrays.asList(nuevoGrupo.getNombre_grupo());

        return null;
    }

    public ResponseEntity<?> getChatsDeUnGrupo(Long idGrupo) {
        try {
            Optional<GrupoDto> grupoOptional = gruposRepository.findGrupoById(idGrupo);
            if (grupoOptional.isPresent()) {
                List<ChatDeUnGrupoDto> chatsGrupo = gruposRepository.findChatsByGrupoIdOrderByFechaCreacion(idGrupo);
                if (!chatsGrupo.isEmpty()) {
                    return ResponseEntity.ok(chatsGrupo);
                }
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No existe el grupo buscado");
        } catch (Exception e) {
            log.error("Error al obtener los chats del grupo con id {}", idGrupo);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }
}
