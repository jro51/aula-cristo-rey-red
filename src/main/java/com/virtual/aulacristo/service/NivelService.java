package com.virtual.aulacristo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.virtual.aulacristo.model.Nivel;
import com.virtual.aulacristo.repository.NivelRepository;

@Service
public class NivelService {

    private final NivelRepository nivelRepository;

    public NivelService(NivelRepository nivelRepository) {
        this.nivelRepository = nivelRepository;
    }

    // AVCR-128 y AVCR-132: Registrar nivel sin duplicados
    public Nivel registrarNivel(Nivel nivel) {

        String nombre = normalizarNombre(
                nivel.getNombre());

        if (nivelRepository.existsByNombreIgnoreCase(nombre)) {
            throw new IllegalArgumentException(
                    "Ya existe un nivel registrado con el nombre: "
                            + nombre);
        }

        nivel.setId(null);
        nivel.setNombre(nombre);
        nivel.setActivo(true);

        return nivelRepository.save(nivel);
    }

    // AVCR-128: Listar niveles
    public List<Nivel> listarNiveles() {
        return nivelRepository.findAll();
    }

    // AVCR-128: Buscar nivel por ID
    public Nivel buscarNivelPorId(Long id) {
        return nivelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el nivel con ID: " + id));
    }

    // AVCR-128 y AVCR-132: Actualizar nivel sin duplicados
    public Nivel actualizarNivel(
            Long id,
            Nivel nivelActualizado) {

        Nivel nivelExistente = buscarNivelPorId(id);

        String nombre = normalizarNombre(
                nivelActualizado.getNombre());

        boolean nombreDuplicado = nivelRepository
                .existsByNombreIgnoreCaseAndIdNot(
                        nombre,
                        id);

        if (nombreDuplicado) {
            throw new IllegalArgumentException(
                    "Ya existe otro nivel registrado con el nombre: "
                            + nombre);
        }

        nivelExistente.setNombre(nombre);

        nivelExistente.setDescripcion(
                nivelActualizado.getDescripcion());

        return nivelRepository.save(nivelExistente);
    }

    // AVCR-128: Activar o desactivar nivel
    public Nivel cambiarEstado(
            Long id,
            boolean nuevoEstado) {

        Nivel nivel = buscarNivelPorId(id);

        nivel.setActivo(nuevoEstado);

        return nivelRepository.save(nivel);
    }

    // AVCR-128: Eliminación lógica
    public void eliminarNivel(Long id) {

        Nivel nivel = buscarNivelPorId(id);

        // No se elimina físicamente para conservar
        // las futuras relaciones con los grados
        nivel.setActivo(false);

        nivelRepository.save(nivel);
    }
    // Limpiar y validar el nombre
private String normalizarNombre(String nombre) {

    if (nombre == null || nombre.trim().isEmpty()) {
        throw new IllegalArgumentException(
            "El nombre del nivel es obligatorio."
        );
    }

    return nombre.trim();
}
}