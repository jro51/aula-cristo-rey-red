package com.virtual.aulacristo.repository;

import com.virtual.aulacristo.model.Beneficio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// AVCR-202: Repositorio para gestionar la persistencia de becas y descuentos
@Repository
public interface BeneficioRepository extends JpaRepository<Beneficio, Long> {

    // Buscar beneficios por estudiante
    List<Beneficio> findByEstudianteId(Long estudianteId);

    // Buscar si un estudiante ya cuenta con un beneficio en un período/año específico
    Optional<Beneficio> findByEstudianteIdAndAnioPeriodo(Long estudianteId, String anioPeriodo);
}
