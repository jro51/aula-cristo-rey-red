package com.virtual.aulacristo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.virtual.aulacristo.model.Nivel;
import com.virtual.aulacristo.service.NivelService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/niveles")
public class NivelController {

    private final NivelService nivelService;

    public NivelController(NivelService nivelService) {
        this.nivelService = nivelService;
    }

    // GET: Listar niveles
    @GetMapping
    public List<Nivel> listarNiveles() {
        return nivelService.listarNiveles();
    }

    // GET: Buscar nivel por ID
    @GetMapping("/{id}")
    public Nivel buscarNivelPorId(
            @PathVariable Long id) {

        return nivelService.buscarNivelPorId(id);
    }

    // POST: Registrar nivel
    @PostMapping
    public ResponseEntity<Nivel> registrarNivel(
            @Valid @RequestBody Nivel nivel) {

        Nivel nivelRegistrado =
                nivelService.registrarNivel(nivel);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(nivelRegistrado);
    }

    // PUT: Actualizar nivel
    @PutMapping("/{id}")
    public Nivel actualizarNivel(
            @PathVariable Long id,
            @Valid @RequestBody Nivel nivel) {

        return nivelService.actualizarNivel(
            id,
            nivel
        );
    }

    // PATCH: Activar o desactivar nivel
    @PatchMapping("/{id}/estado")
    public Nivel cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo) {

        return nivelService.cambiarEstado(
            id,
            activo
        );
    }

    // DELETE: Realizar eliminación lógica
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarNivel(
            @PathVariable Long id) {

        nivelService.eliminarNivel(id);

        return ResponseEntity.noContent().build();
    }

    // Manejar niveles que no existen
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarError(
            IllegalArgumentException error) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "mensaje",
                    error.getMessage()
                ));
    }
}