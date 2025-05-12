package com.chatterbox.api_rest.service;

import com.chatterbox.api_rest.dto.grupo.ChatDeUnGrupoDto;
import com.chatterbox.api_rest.dto.grupo.GrupoDto;
import com.chatterbox.api_rest.repository.GruposRepository;
import com.chatterbox.api_rest.util.ValidacionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class GruposService {
    private final GruposRepository gruposRepository;

    public ResponseEntity<?> createGrupo(GrupoDto nuevoGrupo) {
        List<String> camposObligatorios = List.of(String.valueOf(nuevoGrupo.getId_usuario_creador()), nuevoGrupo.getNombre_grupo());
        if (!ValidacionUtils.camposValidos(camposObligatorios)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Credenciales inválidas");
        }

        try {
            Long id = gruposRepository.insertGrupo(nuevoGrupo);
            nuevoGrupo.setId_grupo(id);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(nuevoGrupo);
        } catch (DuplicateKeyException e) {
            log.error("Error al agregar grupo ", e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Grupo ya existente para ese usuario");
        }
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
