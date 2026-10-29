package com.virtual.aulacristo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.virtual.aulacristo.model.Grado;
import com.virtual.aulacristo.model.Seccion;
import com.virtual.aulacristo.repository.GradoRepository;
import com.virtual.aulacristo.repository.SeccionRepository;

@Service
public class SeccionService {

    private final SeccionRepository seccionRepository;
    private final GradoRepository gradoRepository;

    public SeccionService(
            SeccionRepository seccionRepository,
            GradoRepository gradoRepository) {

        this.seccionRepository = seccionRepository;
        this.gradoRepository = gradoRepository;
    }

    // AVCR-130 y AVCR-132: Registrar sección sin duplicados
    public Seccion registrarSeccion(Seccion seccion) {

        validarGrado(seccion);

        Long gradoId = seccion.getGrado().getId();

        Grado grado = buscarGrado(gradoId);

        String nombre = normalizarNombre(
                seccion.getNombre());

        boolean seccionDuplicada = seccionRepository
                .existsByNombreIgnoreCaseAndGradoId(
                        nombre,
                        gradoId);

        if (seccionDuplicada) {
            throw new IllegalArgumentException(
                    "Ya existe la sección " + nombre
                            + " dentro del grado "
                            + grado.getNombre() + ".");
        }

        seccion.setId(null);
        seccion.setNombre(nombre);
        seccion.setActivo(true);
        seccion.setGrado(grado);

        return seccionRepository.save(seccion);
    }

    // AVCR-130: Listar todas las secciones
    public List<Seccion> listarSecciones() {
        return seccionRepository.findAll();
    }

    // AVCR-130: Listar secciones de un grado
    public List<Seccion> listarSeccionesPorGrado(
            Long gradoId) {

        buscarGrado(gradoId);

        return seccionRepository.findByGradoId(gradoId);
    }

    // AVCR-130: Buscar sección por ID
    public Seccion buscarSeccionPorId(Long id) {

        return seccionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la sección con ID: "
                                + id));
    }

    // AVCR-130 y AVCR-132: Actualizar sección sin duplicados
    public Seccion actualizarSeccion(
            Long id,
            Seccion seccionActualizada) {

        Seccion seccionExistente = buscarSeccionPorId(id);

        validarGrado(seccionActualizada);

        Long gradoId = seccionActualizada.getGrado().getId();

        Grado grado = buscarGrado(gradoId);

        String nombre = normalizarNombre(
                seccionActualizada.getNombre());

        boolean seccionDuplicada = seccionRepository
                .existsByNombreIgnoreCaseAndGradoIdAndIdNot(
                        nombre,
                        gradoId,
                        id);

        if (seccionDuplicada) {
            throw new IllegalArgumentException(
                    "Ya existe la sección " + nombre
                            + " dentro del grado "
                            + grado.getNombre() + ".");
        }

        seccionExistente.setNombre(nombre);

        seccionExistente.setDescripcion(
                seccionActualizada.getDescripcion());

        seccionExistente.setGrado(grado);

        return seccionRepository.save(
                seccionExistente);
    }

    // AVCR-130: Activar o desactivar sección
    public Seccion cambiarEstado(
            Long id,
            boolean nuevoEstado) {

        Seccion seccion = buscarSeccionPorId(id);

        seccion.setActivo(nuevoEstado);

        return seccionRepository.save(seccion);
    }

    // AVCR-130: Eliminación lógica
    public void eliminarSeccion(Long id) {

        Seccion seccion = buscarSeccionPorId(id);

        seccion.setActivo(false);

        seccionRepository.save(seccion);
    }

    // Validar que la sección tenga un grado
    private void validarGrado(Seccion seccion) {

        if (seccion.getGrado() == null
                || seccion.getGrado().getId() == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un grado para la sección.");
        }
    }

    // Buscar el grado en la base de datos
    private Grado buscarGrado(Long gradoId) {

        return gradoRepository.findById(gradoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el grado con ID: "
                                + gradoId));
    }

    private String normalizarNombre(String nombre) {

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre de la sección es obligatorio.");
        }

        return nombre.trim().toUpperCase();
    }
}