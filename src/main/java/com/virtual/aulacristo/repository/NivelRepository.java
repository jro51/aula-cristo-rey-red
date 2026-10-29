package com.virtual.aulacristo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.virtual.aulacristo.model.Nivel;

@Repository
public interface NivelRepository
        extends JpaRepository<Nivel, Long> {

    // Validar duplicado al registrar
    boolean existsByNombreIgnoreCase(String nombre);

    // Validar duplicado al actualizar, excluyendo el mismo ID
    boolean existsByNombreIgnoreCaseAndIdNot(
            String nombre,
            Long id
    );
}