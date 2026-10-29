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

import com.virtual.aulacristo.model.Grado;
import com.virtual.aulacristo.service.GradoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/grados")
public class GradoController {

    private final GradoService gradoService;

    public GradoController(GradoService gradoService) {
        this.gradoService = gradoService;
    }

    // GET: Listar todos los grados
    @GetMapping
    public List<Grado> listarGrados() {
        return gradoService.listarGrados();
    }

    // GET: Buscar grado por ID
    @GetMapping("/{id}")
    public Grado buscarGradoPorId(
            @PathVariable Long id) {

        return gradoService.buscarGradoPorId(id);
    }

    // GET: Listar grados pertenecientes a un nivel
    @GetMapping("/nivel/{nivelId}")
    public List<Grado> listarGradosPorNivel(
            @PathVariable Long nivelId) {

        return gradoService
                .listarGradosPorNivel(nivelId);
    }

    // POST: Registrar grado
    @PostMapping
    public ResponseEntity<Grado> registrarGrado(
            @Valid @RequestBody Grado grado) {

        Grado gradoRegistrado =
                gradoService.registrarGrado(grado);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(gradoRegistrado);
    }

    // PUT: Actualizar grado
    @PutMapping("/{id}")
    public Grado actualizarGrado(
            @PathVariable Long id,
            @Valid @RequestBody Grado grado) {

        return gradoService.actualizarGrado(
            id,
            grado
        );
    }

    // PATCH: Activar o desactivar grado
    @PatchMapping("/{id}/estado")
    public Grado cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo) {

        return gradoService.cambiarEstado(
            id,
            activo
        );
    }

    // DELETE: Eliminación lógica
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarGrado(
            @PathVariable Long id) {

        gradoService.eliminarGrado(id);

        return ResponseEntity.noContent().build();
    }

    // Manejar errores
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarError(
            IllegalArgumentException error) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "mensaje",
                    error.getMessage()
                ));
    }
}