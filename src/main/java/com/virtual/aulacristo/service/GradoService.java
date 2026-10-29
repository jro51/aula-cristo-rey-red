package com.virtual.aulacristo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.virtual.aulacristo.model.Grado;
import com.virtual.aulacristo.model.Nivel;
import com.virtual.aulacristo.repository.GradoRepository;
import com.virtual.aulacristo.repository.NivelRepository;

@Service
public class GradoService {

    private final GradoRepository gradoRepository;
    private final NivelRepository nivelRepository;

    public GradoService(
            GradoRepository gradoRepository,
            NivelRepository nivelRepository) {

        this.gradoRepository = gradoRepository;
        this.nivelRepository = nivelRepository;
    }

    // AVCR-129 y AVCR-132: Registrar grado sin duplicados
public Grado registrarGrado(Grado grado) {

    validarNivel(grado);

    Long nivelId = grado.getNivel().getId();

    Nivel nivel = buscarNivel(nivelId);

    String nombre = normalizarNombre(
        grado.getNombre()
    );

    boolean gradoDuplicado =
            gradoRepository
                    .existsByNombreIgnoreCaseAndNivelId(
                        nombre,
                        nivelId
                    );

    if (gradoDuplicado) {
        throw new IllegalArgumentException(
            "Ya existe el grado " + nombre
            + " dentro del nivel "
            + nivel.getNombre() + "."
        );
    }

    grado.setId(null);
    grado.setNombre(nombre);
    grado.setActivo(true);
    grado.setNivel(nivel);

    return gradoRepository.save(grado);
}

    // AVCR-129: Listar todos los grados
    public List<Grado> listarGrados() {
        return gradoRepository.findAll();
    }

    // AVCR-129: Listar grados por nivel
    public List<Grado> listarGradosPorNivel(
            Long nivelId) {

        buscarNivel(nivelId);

        return gradoRepository.findByNivelId(nivelId);
    }

    // AVCR-129: Buscar grado por ID
    public Grado buscarGradoPorId(Long id) {

        return gradoRepository.findById(id)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "No se encontró el grado con ID: " + id
                    )
                );
    }

    // AVCR-129 y AVCR-132: Actualizar grado sin duplicados
public Grado actualizarGrado(
        Long id,
        Grado gradoActualizado) {

    Grado gradoExistente =
            buscarGradoPorId(id);

    validarNivel(gradoActualizado);

    Long nivelId =
            gradoActualizado.getNivel().getId();

    Nivel nivel = buscarNivel(nivelId);

    String nombre = normalizarNombre(
        gradoActualizado.getNombre()
    );

    boolean gradoDuplicado =
            gradoRepository
                    .existsByNombreIgnoreCaseAndNivelIdAndIdNot(
                        nombre,
                        nivelId,
                        id
                    );

    if (gradoDuplicado) {
        throw new IllegalArgumentException(
            "Ya existe el grado " + nombre
            + " dentro del nivel "
            + nivel.getNombre() + "."
        );
    }

    gradoExistente.setNombre(nombre);

    gradoExistente.setDescripcion(
        gradoActualizado.getDescripcion()
    );

    gradoExistente.setNivel(nivel);

    return gradoRepository.save(gradoExistente);
}

    // AVCR-129: Activar o desactivar grado
    public Grado cambiarEstado(
            Long id,
            boolean nuevoEstado) {

        Grado grado = buscarGradoPorId(id);

        grado.setActivo(nuevoEstado);

        return gradoRepository.save(grado);
    }

    // AVCR-129: Eliminación lógica
    public void eliminarGrado(Long id) {

        Grado grado = buscarGradoPorId(id);

        grado.setActivo(false);

        gradoRepository.save(grado);
    }

    // Validar que se haya enviado un nivel
    private void validarNivel(Grado grado) {

        if (grado.getNivel() == null
                || grado.getNivel().getId() == null) {

            throw new IllegalArgumentException(
                "Debe seleccionar un nivel para el grado."
            );
        }
    }

    // Buscar un nivel existente en MySQL
    private Nivel buscarNivel(Long nivelId) {

        return nivelRepository.findById(nivelId)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "No se encontró el nivel con ID: "
                        + nivelId
                    )
                );
    }
    private String normalizarNombre(String nombre) {

    if (nombre == null || nombre.trim().isEmpty()) {
        throw new IllegalArgumentException(
            "El nombre del grado es obligatorio."
        );
    }

    return nombre.trim();
}
}