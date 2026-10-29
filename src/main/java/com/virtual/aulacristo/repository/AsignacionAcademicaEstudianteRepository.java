package com.virtual.aulacristo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.virtual.aulacristo.model.AsignacionAcademicaEstudiante;

@Repository
public interface AsignacionAcademicaEstudianteRepository
        extends JpaRepository<AsignacionAcademicaEstudiante, Long> {

    // Buscar la asignación del estudiante en un período
    Optional<AsignacionAcademicaEstudiante>
        findByEstudiante_IdAndPeriodoAcademico_Id(
            Long estudianteId,
            Long periodoAcademicoId
        );

    // AVCR-158: Validar la misma asignación completa
    boolean
        existsByEstudiante_IdAndGrado_IdAndSeccion_IdAndPeriodoAcademico_IdAndActivoTrue(
            Long estudianteId,
            Long gradoId,
            Long seccionId,
            Long periodoAcademicoId
        );

    // Validar cualquier asignación del estudiante en el período
    boolean existsByEstudiante_IdAndPeriodoAcademico_Id(
        Long estudianteId,
        Long periodoAcademicoId
    );

    // Listar todas las asignaciones
    List<AsignacionAcademicaEstudiante>
        findAllByOrderByFechaAsignacionDesc();

    // Listar asignaciones activas del período
    List<AsignacionAcademicaEstudiante>
        findByPeriodoAcademico_IdAndActivoTrueOrderByEstudiante_ApellidosAscEstudiante_NombresAsc(
            Long periodoAcademicoId
        );

    // Listar estudiantes por grado y sección
    List<AsignacionAcademicaEstudiante>
        findByGrado_IdAndSeccion_IdAndPeriodoAcademico_IdAndActivoTrueOrderByEstudiante_ApellidosAscEstudiante_NombresAsc(
            Long gradoId,
            Long seccionId,
            Long periodoAcademicoId
        );
}