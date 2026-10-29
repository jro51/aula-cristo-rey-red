package com.virtual.aulacristo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.virtual.aulacristo.model.Grado;

@Repository
public interface GradoRepository
        extends JpaRepository<Grado, Long> {

    List<Grado> findByNivelId(Long nivelId);

    // Validar grado duplicado dentro del mismo nivel
    boolean existsByNombreIgnoreCaseAndNivelId(
            String nombre,
            Long nivelId
    );

    // Validar actualización excluyendo el mismo grado
    boolean existsByNombreIgnoreCaseAndNivelIdAndIdNot(
            String nombre,
            Long nivelId,
            Long id
    );
}