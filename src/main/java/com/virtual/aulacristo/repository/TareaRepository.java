package com.virtual.aulacristo.repository;

import com.virtual.aulacristo.model.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {

    // AVCR-218 y AVCR-219: Tareas publicadas de los cursos que pertenecen al grado del estudiante
    @Query("""
        SELECT t FROM Tarea t
        JOIN t.curso c
        WHERE t.publicada = true
        AND c.grado.id = :gradoId
        ORDER BY t.fechaLimite ASC
    """)
    List<Tarea> findTareasPublicadasPorGrado(@Param("gradoId") Long gradoId);

    // AVCR-222: Filtro adicional por curso específico
    @Query("""
        SELECT t FROM Tarea t
        JOIN t.curso c
        WHERE t.publicada = true
        AND c.grado.id = :gradoId
        AND (:cursoId IS NULL OR c.id = :cursoId)
        ORDER BY t.fechaLimite ASC
    """)
    List<Tarea> findTareasPublicadasPorGradoYCurso(
        @Param("gradoId") Long gradoId,
        @Param("cursoId") Long cursoId
    );

    // Lista de cursos con tareas publicadas para un grado (para el filtro)
    @Query("""
        SELECT DISTINCT t.curso FROM Tarea t
        WHERE t.publicada = true
        AND t.curso.grado.id = :gradoId
        ORDER BY t.curso.nombre ASC
    """)
    List<com.virtual.aulacristo.model.Curso> findCursosConTareasPublicadasPorGrado(
        @Param("gradoId") Long gradoId
    );
}
