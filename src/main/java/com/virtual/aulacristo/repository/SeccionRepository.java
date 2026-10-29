package com.virtual.aulacristo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.virtual.aulacristo.model.Seccion;

@Repository
public interface SeccionRepository
        extends JpaRepository<Seccion, Long> {

    List<Seccion> findByGradoId(Long gradoId);

    // Validar sección duplicada dentro del mismo grado
    boolean existsByNombreIgnoreCaseAndGradoId(
            String nombre,
            Long gradoId
    );

    // Validar actualización excluyendo la misma sección
    boolean existsByNombreIgnoreCaseAndGradoIdAndIdNot(
            String nombre,
            Long gradoId,
            Long id
    );
}