package com.virtual.aulacristo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.virtual.aulacristo.model.AnioAcademico;
import com.virtual.aulacristo.model.PeriodoAcademico;
import com.virtual.aulacristo.repository.AnioAcademicoRepository;
import com.virtual.aulacristo.repository.PeriodoAcademicoRepository;

@Service
public class PeriodoAcademicoService {

    private final PeriodoAcademicoRepository periodoAcademicoRepository;
    private final AnioAcademicoRepository anioAcademicoRepository;

    public PeriodoAcademicoService(
            PeriodoAcademicoRepository periodoAcademicoRepository,
            AnioAcademicoRepository anioAcademicoRepository) {

        this.periodoAcademicoRepository =
                periodoAcademicoRepository;

        this.anioAcademicoRepository =
                anioAcademicoRepository;
    }

    public PeriodoAcademico registrar(
            PeriodoAcademico periodoAcademico) {

        validarDatos(periodoAcademico);

        Long anioId =
                periodoAcademico.getAnioAcademico().getId();

        AnioAcademico anioAcademico =
                anioAcademicoRepository.findById(anioId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "El año académico seleccionado no existe."));

        if (!anioAcademico.isActivo()) {
            throw new IllegalArgumentException(
                    "El año académico seleccionado se encuentra inactivo.");
        }

        String nombreNormalizado =
                periodoAcademico.getNombre().trim();

        if (periodoAcademicoRepository
                .existsByAnioAcademicoIdAndNumero(
                        anioId,
                        periodoAcademico.getNumero())) {

            throw new IllegalArgumentException(
                    "Ya existe el periodo número "
                            + periodoAcademico.getNumero()
                            + " en el año académico "
                            + anioAcademico.getAnio()
                            + ".");
        }

        if (periodoAcademicoRepository
                .existsByAnioAcademicoIdAndNombreIgnoreCase(
                        anioId,
                        nombreNormalizado)) {

            throw new IllegalArgumentException(
                    "Ya existe un periodo llamado "
                            + nombreNormalizado
                            + " en el año académico "
                            + anioAcademico.getAnio()
                            + ".");
        }

        periodoAcademico.setNombre(nombreNormalizado);
        periodoAcademico.setAnioAcademico(anioAcademico);
        periodoAcademico.setVigente(false);
        periodoAcademico.setActivo(true);

        return periodoAcademicoRepository.save(
                periodoAcademico);
    }

    public List<PeriodoAcademico> listarTodos() {
        return periodoAcademicoRepository
                .findAllByOrderByFechaInicioDesc();
    }

    public List<PeriodoAcademico> listarPorAnio(
            Long anioAcademicoId) {

        if (anioAcademicoId == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un año académico.");
        }

        if (!anioAcademicoRepository
                .existsById(anioAcademicoId)) {

            throw new IllegalArgumentException(
                    "El año académico seleccionado no existe.");
        }

        return periodoAcademicoRepository
                .findByAnioAcademicoIdOrderByNumeroAsc(
                        anioAcademicoId);
    }

    public PeriodoAcademico buscarPorId(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El identificador del periodo es obligatorio.");
        }

        return periodoAcademicoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró el periodo académico."));
    }

    // AVCR-139: Activar un periodo como vigente
    @Transactional
    public PeriodoAcademico activarComoVigente(Long id) {

        PeriodoAcademico periodoSeleccionado =
                buscarPorId(id);

        if (!periodoSeleccionado.isActivo()) {
            throw new IllegalArgumentException(
                    "No se puede activar un periodo inactivo.");
        }

        if (!periodoSeleccionado
                .getAnioAcademico()
                .isActivo()) {

            throw new IllegalArgumentException(
                    "El año académico del periodo se encuentra inactivo.");
        }

        // Desactivar cualquier otro periodo vigente
        List<PeriodoAcademico> periodosVigentes =
                periodoAcademicoRepository.findByVigenteTrue();

        for (PeriodoAcademico periodo : periodosVigentes) {
            periodo.setVigente(false);
        }

        periodoAcademicoRepository.saveAll(periodosVigentes);

        // Activar el periodo seleccionado
        periodoSeleccionado.setVigente(true);

        return periodoAcademicoRepository.save(
                periodoSeleccionado);
    }

    private void validarDatos(
            PeriodoAcademico periodoAcademico) {

        if (periodoAcademico == null) {
            throw new IllegalArgumentException(
                    "Los datos del periodo son obligatorios.");
        }

        if (periodoAcademico.getNombre() == null
                || periodoAcademico.getNombre()
                        .trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El nombre del periodo es obligatorio.");
        }

        if (periodoAcademico.getNumero() == null
                || periodoAcademico.getNumero() < 1) {

            throw new IllegalArgumentException(
                    "El número del periodo debe ser mayor que cero.");
        }

        if (periodoAcademico.getFechaInicio() == null) {
            throw new IllegalArgumentException(
                    "La fecha de inicio es obligatoria.");
        }

        if (periodoAcademico.getFechaFin() == null) {
            throw new IllegalArgumentException(
                    "La fecha de fin es obligatoria.");
        }

        if (periodoAcademico.getFechaFin()
                .isBefore(periodoAcademico.getFechaInicio())) {

            throw new IllegalArgumentException(
                    "La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        if (periodoAcademico.getAnioAcademico() == null
                || periodoAcademico
                        .getAnioAcademico()
                        .getId() == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un año académico.");
        }
    }
}