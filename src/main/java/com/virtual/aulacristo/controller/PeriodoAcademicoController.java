package com.virtual.aulacristo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.virtual.aulacristo.model.PeriodoAcademico;
import com.virtual.aulacristo.service.PeriodoAcademicoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/periodos-academicos")
public class PeriodoAcademicoController {

    private final PeriodoAcademicoService periodoAcademicoService;

    public PeriodoAcademicoController(
            PeriodoAcademicoService periodoAcademicoService) {

        this.periodoAcademicoService =
                periodoAcademicoService;
    }

    @PostMapping
    public ResponseEntity<PeriodoAcademico> registrar(
            @Valid @RequestBody PeriodoAcademico periodoAcademico) {

        PeriodoAcademico guardado =
                periodoAcademicoService.registrar(
                        periodoAcademico);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(guardado);
    }

    @GetMapping
    public List<PeriodoAcademico> listarTodos() {
        return periodoAcademicoService.listarTodos();
    }

    @GetMapping("/{id}")
    public PeriodoAcademico buscarPorId(
            @PathVariable Long id) {

        return periodoAcademicoService.buscarPorId(id);
    }

    @GetMapping("/anio/{anioAcademicoId}")
    public List<PeriodoAcademico> listarPorAnio(
            @PathVariable Long anioAcademicoId) {

        return periodoAcademicoService
                .listarPorAnio(anioAcademicoId);
    }

    // AVCR-139: Activar periodo vigente
    @PatchMapping("/{id}/vigente")
    public ResponseEntity<PeriodoAcademico> activarVigente(
            @PathVariable Long id) {

        PeriodoAcademico periodo =
                periodoAcademicoService
                        .activarComoVigente(id);

        return ResponseEntity.ok(periodo);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarError(
            IllegalArgumentException exception) {

        return ResponseEntity
                .badRequest()
                .body(Map.of(
                        "mensaje",
                        exception.getMessage()));
    }
}