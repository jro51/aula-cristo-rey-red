package com.virtual.aulacristo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.virtual.aulacristo.model.AsignacionDocente;

@Repository
public interface AsignacionDocenteRepository
        extends JpaRepository<AsignacionDocente, Long> {

    List<AsignacionDocente> findAllByOrderByIdDesc();

    List<AsignacionDocente>
        findByDocente_IdUsuarioOrderByCurso_NombreAsc(
            Long docenteId
        );

    List<AsignacionDocente>
        findByDocente_IdUsuarioAndActivoTrueOrderByCurso_NombreAsc(
            Long docenteId
        );

    // Validar la misma combinación completa
    boolean existsByDocente_IdUsuarioAndCurso_IdAndGrado_IdAndSeccion_Id(
        Long docenteId,
        Long cursoId,
        Long gradoId,
        Long seccionId
    );

    // Validar si el curso ya tiene otro docente activo
    boolean existsByCurso_IdAndGrado_IdAndSeccion_IdAndActivoTrue(
        Long cursoId,
        Long gradoId,
        Long seccionId
    );

    // Se utiliza cuando se intenta reactivar una asignación
    boolean existsByCurso_IdAndGrado_IdAndSeccion_IdAndActivoTrueAndIdNot(
        Long cursoId,
        Long gradoId,
        Long seccionId,
        Long asignacionId
    );
}