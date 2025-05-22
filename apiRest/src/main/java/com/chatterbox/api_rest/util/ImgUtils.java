package com.chatterbox.api_rest.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
public class ImgUtils {
    private static final String rutaImgPorDefecto = "static/img_default/foto_perfil_default.png";
    private static final String rutaImgPerfil = "apiRest/img/fotos_perfil";

    public ImgUtils() {
    }

    public static ResponseEntity<?> obtenerImgComoResponse(String nombreArchivo, String carpetaDestino) {
        try {
            if (nombreArchivo == null || nombreArchivo.isBlank()) {
                if (carpetaDestino.equalsIgnoreCase(rutaImgPerfil)) {
                    return cargarImgPorDefecto();
                }
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No hay foto disponible");
            }

            Path rutaArchivo = Paths.get(carpetaDestino)
                    .resolve(nombreArchivo)
                    .normalize(); // Eliminar cosas raras que pueda haber en la ruta ("..", ".", etc)
            if (!rutaArchivo.startsWith(Paths.get(carpetaDestino)) || !Files.exists(rutaArchivo)) {
                if (carpetaDestino.equalsIgnoreCase(rutaImgPerfil)) {
                    return cargarImgPorDefecto();
                }
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Archivo no encontrado");
            }

            Resource recurso = new UrlResource(rutaArchivo.toUri());
            String contentType = Files.probeContentType(rutaArchivo);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(recurso);
        } catch (Exception e) {
            log.error("Error inesperado al obtener la foto de perfil", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    public static String guardarImg(MultipartFile archivo, Long id, String carpetaDestino, String tipo) throws IOException {
        Path rutaCarpeta = Paths.get(carpetaDestino);
        if (!Files.exists(rutaCarpeta)) {
            Files.createDirectories(rutaCarpeta);
        }

        String extension = obtenerExtensionPorMime(archivo);
        // Generar un nombre único para el archivo
        String nombreArchivo = tipo + "_" + id + "_" + System.currentTimeMillis() + "." + extension;
        Path destino = rutaCarpeta.resolve(nombreArchivo);

        // Guardar el archivo en la ruta de destino
        Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        return nombreArchivo;
    }

    public static void eliminarImgAnterior(String nombreArchivo, String carpetaDestino) throws IOException {
        if (nombreArchivo == null || nombreArchivo.isBlank()) return;

        Path rutaArchivo = Paths.get(carpetaDestino, nombreArchivo);

        Files.deleteIfExists(rutaArchivo);
    }

    private static ResponseEntity<?> cargarImgPorDefecto() throws IOException {
        ClassPathResource imgPorDefecto = new ClassPathResource(rutaImgPorDefecto);

        if (!imgPorDefecto.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Imagen por defecto no encontrada");
        }

        String contentType = Files.probeContentType(imgPorDefecto.getFile()
                .toPath());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(imgPorDefecto);
    }

    private static String obtenerExtensionPorMime(MultipartFile archivo) throws IOException {
        String tipoMime = archivo.getContentType();

        if (tipoMime == null) {
            throw new IOException("No se pudo determinar el tipo MIME del archivo");
        }

        switch (tipoMime) {
            case "image/jpg", "image/jpeg" -> {
                return "jpg";
            }
            case "image/png" -> {
                return "png";
            }
            case "image/webp" -> {
                return "webp";
            }
            case "image/avif" -> {
                return "avif";
            }
            default -> throw new IOException("Tipo MIME no permitido: " + tipoMime);
        }
    }
}
