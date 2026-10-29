package com.virtual.aulacristo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.virtual.aulacristo.model.AnioAcademico;
import com.virtual.aulacristo.service.AnioAcademicoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/anios-academicos")
public class AnioAcademicoController {

    private final AnioAcademicoService anioAcademicoService;

    public AnioAcademicoController(
            AnioAcademicoService anioAcademicoService) {

        this.anioAcademicoService = anioAcademicoService;
    }

    // AVCR-136: Registrar año académico
    @PostMapping
    public ResponseEntity<AnioAcademico> registrar(
            @Valid @RequestBody AnioAcademico anioAcademico) {

        AnioAcademico guardado =
                anioAcademicoService.registrar(anioAcademico);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(guardado);
    }

    @GetMapping
    public List<AnioAcademico> listarTodos() {
        return anioAcademicoService.listarTodos();
    }

    @GetMapping("/{id}")
    public AnioAcademico buscarPorId(@PathVariable Long id) {
        return anioAcademicoService.buscarPorId(id);
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