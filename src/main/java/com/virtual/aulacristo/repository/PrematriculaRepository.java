package com.virtual.aulacristo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.virtual.aulacristo.model.Prematricula;

@Repository
public interface PrematriculaRepository extends JpaRepository<Prematricula, Long> {
    boolean existsByEstudianteIdAndAnioAcademico(Long estudianteId, String anioAcademico);
    List<Prematricula> findByEstado(com.virtual.aulacristo.model.EstadoPrematricula estado);
}

