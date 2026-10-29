package com.virtual.aulacristo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.virtual.aulacristo.model.Curso;

@Repository
public interface CursoRepository
        extends JpaRepository<Curso, Long> {

    List<Curso> findAllByOrderByNombreAsc();

    List<Curso> findByGradoIdOrderByNombreAsc(
            Long gradoId);

    List<Curso> findByActivoTrueOrderByNombreAsc();

    List<Curso> findByGradoIdAndActivoTrueOrderByNombreAsc(
            Long gradoId);

    // AVCR-146: Validar código duplicado al registrar
    boolean existsByCodigoIgnoreCase(String codigo);

    // AVCR-146: Validar código duplicado al actualizar
    boolean existsByCodigoIgnoreCaseAndIdNot(
            String codigo,
            Long id);

    // AVCR-146: Validar nombre repetido en el mismo grado
    boolean existsByNombreIgnoreCaseAndGradoId(
            String nombre,
            Long gradoId);

    // AVCR-146: Validar nombre repetido al actualizar
    boolean existsByNombreIgnoreCaseAndGradoIdAndIdNot(
            String nombre,
            Long gradoId,
            Long id);
}