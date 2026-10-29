package com.virtual.aulacristo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final String uploadDir = "uploads/documentos/";
    // AVCR-175: Validar formatos permitidos
    private final List<String> formatosPermitidos = Arrays.asList("application/pdf", "image/jpeg", "image/png");

    public String almacenarArchivo(MultipartFile archivo, Long prematriculaId) {
        if (archivo.isEmpty()) {
            throw new IllegalArgumentException("No se puede subir un archivo vacío.");
        }

        if (!formatosPermitidos.contains(archivo.getContentType())) {
            throw new IllegalArgumentException("Formato no permitido. Solo se aceptan PDF, JPG o PNG.");
        }

        try {
            // Crear el directorio si no existe (uploads/documentos/{id_solicitud}/)
            Path rutaDirectorio = Paths.get(uploadDir + prematriculaId);
            if (!Files.exists(rutaDirectorio)) {
                Files.createDirectories(rutaDirectorio);
            }

            // Generar un nombre único para evitar conflictos y caracteres raros
            String nombreOriginal = StringUtils.cleanPath(archivo.getOriginalFilename());
            String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
            String nombreUnico = UUID.randomUUID().toString() + extension;

            Path rutaDestino = rutaDirectorio.resolve(nombreUnico);
            Files.copy(archivo.getInputStream(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);

            // Retornamos la ruta relativa para guardarla en la base de datos
            return prematriculaId + "/" + nombreUnico;

        } catch (IOException e) {
            throw new RuntimeException("Error al almacenar el archivo", e);
        }
    }

    public void eliminarArchivo(String rutaRelativa) {
        try {
            Path ruta = Paths.get(uploadDir + rutaRelativa);
            Files.deleteIfExists(ruta);
        } catch (IOException e) {
            System.err.println("No se pudo eliminar el archivo antiguo: " + e.getMessage());
        }
    }
}