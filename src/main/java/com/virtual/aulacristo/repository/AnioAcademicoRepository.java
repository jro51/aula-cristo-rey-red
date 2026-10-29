package com.virtual.aulacristo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.virtual.aulacristo.model.AnioAcademico;

@Repository
public interface AnioAcademicoRepository
        extends JpaRepository<AnioAcademico, Long> {

    boolean existsByAnio(Integer anio);

    Optional<AnioAcademico> findByAnio(Integer anio);
}