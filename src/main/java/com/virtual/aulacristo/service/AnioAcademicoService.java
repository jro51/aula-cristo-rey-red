package com.virtual.aulacristo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.virtual.aulacristo.model.AnioAcademico;
import com.virtual.aulacristo.repository.AnioAcademicoRepository;

@Service
public class AnioAcademicoService {

    private final AnioAcademicoRepository anioAcademicoRepository;

    public AnioAcademicoService(
            AnioAcademicoRepository anioAcademicoRepository) {

        this.anioAcademicoRepository = anioAcademicoRepository;
    }

    // AVCR-136: Registrar año académico
    public AnioAcademico registrar(AnioAcademico anioAcademico) {

        if (anioAcademico == null || anioAcademico.getAnio() == null) {
            throw new IllegalArgumentException(
                    "Debe ingresar el año académico.");
        }

        if (anioAcademico.getAnio() < 2000
                || anioAcademico.getAnio() > 2100) {

            throw new IllegalArgumentException(
                    "El año académico debe estar entre 2000 y 2100.");
        }

        if (anioAcademicoRepository
                .existsByAnio(anioAcademico.getAnio())) {

            throw new IllegalArgumentException(
                    "El año académico "
                            + anioAcademico.getAnio()
                            + " ya se encuentra registrado.");
        }

        if (anioAcademico.getDescripcion() != null) {
            anioAcademico.setDescripcion(
                    anioAcademico.getDescripcion().trim());
        }

        anioAcademico.setActivo(true);

        return anioAcademicoRepository.save(anioAcademico);
    }

    public List<AnioAcademico> listarTodos() {
        return anioAcademicoRepository.findAll();
    }

    public AnioAcademico buscarPorId(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El identificador del año académico es obligatorio.");
        }

        return anioAcademicoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró el año académico."));
    }
}