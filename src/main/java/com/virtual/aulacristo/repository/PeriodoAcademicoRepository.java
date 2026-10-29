package com.virtual.aulacristo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.virtual.aulacristo.model.PeriodoAcademico;

@Repository
public interface PeriodoAcademicoRepository
        extends JpaRepository<PeriodoAcademico, Long> {

    List<PeriodoAcademico>
            findByAnioAcademicoIdOrderByNumeroAsc(
                    Long anioAcademicoId);

    List<PeriodoAcademico>
            findAllByOrderByFechaInicioDesc();

    boolean existsByAnioAcademicoIdAndNumero(
            Long anioAcademicoId,
            Integer numero);

    boolean existsByAnioAcademicoIdAndNombreIgnoreCase(
            Long anioAcademicoId,
            String nombre);

    // AVCR-139: Buscar periodos actualmente vigentes
    List<PeriodoAcademico> findByVigenteTrue();
}