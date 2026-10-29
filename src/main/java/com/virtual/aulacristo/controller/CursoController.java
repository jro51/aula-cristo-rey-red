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

import com.virtual.aulacristo.model.Curso;
import com.virtual.aulacristo.service.CursoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    // AVCR-144: Listar todos los cursos
    @GetMapping
    public List<Curso> listarCursos() {
        return cursoService.listarCursos();
    }

    // Consultar un curso por ID
    @GetMapping("/{id}")
    public Curso buscarPorId(@PathVariable Long id) {
        return cursoService.buscarPorId(id);
    }

    // Listar cursos de un grado
    @GetMapping("/grado/{gradoId}")
    public List<Curso> listarPorGrado(
            @PathVariable Long gradoId) {

        return cursoService.listarPorGrado(gradoId);
    }

    // Listar todos los cursos activos
    @GetMapping("/activos")
    public List<Curso> listarCursosActivos() {
        return cursoService.listarCursosActivos();
    }

    // Obtener cursos activos de un grado
    // para futuras asignaciones
    @GetMapping("/grado/{gradoId}/activos")
    public List<Curso> listarActivosPorGrado(
            @PathVariable Long gradoId) {

        return cursoService
                .listarActivosPorGrado(gradoId);
    }

    // Registrar un nuevo curso
    @PostMapping
    public ResponseEntity<Curso> registrarCurso(
            @Valid @RequestBody Curso curso) {

        Curso cursoRegistrado =
                cursoService.registrarCurso(curso);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cursoRegistrado);
    }

    // Actualizar los datos de un curso
    @PutMapping("/{id}")
    public ResponseEntity<Curso> actualizarCurso(
            @PathVariable Long id,
            @Valid @RequestBody Curso curso) {

        Curso cursoActualizado =
                cursoService.actualizarCurso(id, curso);

        return ResponseEntity.ok(cursoActualizado);
    }

    // Activar o desactivar un curso
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Curso> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo) {

        Curso curso =
                cursoService.cambiarEstado(id, activo);

        return ResponseEntity.ok(curso);
    }

    // Baja lógica del curso
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCurso(
            @PathVariable Long id) {

        cursoService.eliminarCurso(id);

        return ResponseEntity.noContent().build();
    }

    // Manejar errores de negocio
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