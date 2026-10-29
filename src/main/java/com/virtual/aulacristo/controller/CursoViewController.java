package com.virtual.aulacristo.controller;

import com.virtual.aulacristo.model.Curso;
import com.virtual.aulacristo.model.Grado;
import com.virtual.aulacristo.service.CursoService;
import com.virtual.aulacristo.service.GradoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.util.List;

@Controller
@RequestMapping("/cursos")
public class CursoViewController {

    private final CursoService cursoService;
    private final GradoService gradoService;

    public CursoViewController(CursoService cursoService, GradoService gradoService) {
        this.cursoService = cursoService;
        this.gradoService = gradoService;
    }

    // Listado general de cursos con validación de roles y métricas
    @GetMapping
    public String listarCursos(Model model, HttpSession session) {
        String acceso = validarAcceso(session);
        if (acceso != null) return acceso;

        if (!model.containsAttribute("curso")) {
            Curso curso = new Curso();
            curso.setGrado(new Grado());
            model.addAttribute("curso", curso);
        }

        cargarMetricasYListas(model);
        return "cursos/lista";
    }

    // Endpoint parcial para actualizar la tabla de cursos en tiempo real vía AJAX
    @GetMapping("/parcial")
    public String listarCursosParcial(HttpSession session, Model model) {
        String acceso = validarAcceso(session);
        if (acceso != null) return "";

        cargarMetricasYListas(model);
        return "cursos/lista :: tablaCursosContainer";
    }

    // Registrar curso con integridad transaccional ACID
    @Transactional
    @PostMapping("/guardar")
    public String guardarCurso(@Valid @ModelAttribute("curso") Curso curso,
                               BindingResult result,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttrs) {
        String acceso = validarAcceso(session);
        if (acceso != null) return acceso;

        if (result.hasErrors()) {
            if (curso.getGrado() == null) {
                curso.setGrado(new Grado());
            }
            model.addAttribute("mensajeError", "Revisa los datos ingresados.");
            cargarMetricasYListas(model);
            return "cursos/lista";
        }

        try {
            cursoService.registrarCurso(curso);
            redirectAttrs.addFlashAttribute("mensajeExito", "Curso registrado exitosamente.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("mensajeError", e.getMessage());
            cargarMetricasYListas(model);
            return "cursos/lista";
        }

        return "redirect:/cursos";
    }

    // Mostrar formulario de edición
    @GetMapping("/editar/{id}")
    public String mostrarEdicion(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String acceso = validarAcceso(session);
        if (acceso != null) return acceso;

        try {
            model.addAttribute("cursoId", id);
            model.addAttribute("curso", cursoService.buscarPorId(id));
            model.addAttribute("grados", listarGradosActivos());
            return "cursos/formulario";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("mensajeError", exception.getMessage());
            return "redirect:/cursos";
        }
    }

    // Actualizar curso con integridad transaccional ACID
    @Transactional
    @PostMapping("/actualizar/{id}")
    public String actualizarCurso(@PathVariable Long id,
                                  @Valid @ModelAttribute("curso") Curso curso,
                                  BindingResult result,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes redirectAttrs) {
        String acceso = validarAcceso(session);
        if (acceso != null) return acceso;

        if (result.hasErrors()) {
            model.addAttribute("cursoId", id);
            model.addAttribute("grados", listarGradosActivos());
            model.addAttribute("mensajeError", "Revisa los datos ingresados.");
            return "cursos/formulario";
        }

        try {
            cursoService.actualizarCurso(id, curso);
            redirectAttrs.addFlashAttribute("mensajeExito", "Curso actualizado correctamente.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("cursoId", id);
            model.addAttribute("grados", listarGradosActivos());
            model.addAttribute("mensajeError", e.getMessage());
            return "cursos/formulario";
        }

        return "redirect:/cursos";
    }

    // Cambiar estado (Activar / Desactivar) con transaccionalidad
    @Transactional
    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam boolean activo,
                                HttpSession session,
                                RedirectAttributes redirectAttrs) {
        String acceso = validarAcceso(session);
        if (acceso != null) return acceso;

        try {
            cursoService.cambiarEstado(id, activo);
            redirectAttrs.addFlashAttribute("mensajeExito",
                    activo ? "Curso activado correctamente." : "Curso desactivado correctamente.");
        } catch (IllegalArgumentException exception) {
            redirectAttrs.addFlashAttribute("mensajeError", exception.getMessage());
        }

        return "redirect:/cursos";
    }

    // Métodos auxiliares privados
    private void cargarMetricasYListas(Model model) {
        List<Curso> cursos = cursoService.listarCursos();
        long cursosActivos = cursos.stream().filter(Curso::isActivo).count();
        long cursosInactivos = cursos.size() - cursosActivos;

        model.addAttribute("cursos", cursos);
        model.addAttribute("grados", listarGradosActivos());
        model.addAttribute("totalCursos", cursos.size());
        model.addAttribute("cursosActivos", cursosActivos);
        model.addAttribute("cursosInactivos", cursosInactivos);
    }

    private List<Grado> listarGradosActivos() {
        return gradoService.listarGrados()
                .stream()
                .filter(Grado::isActivo)
                .toList();
    }

    private String validarAcceso(HttpSession session) {
        if (session.getAttribute("usuarioLogueado") == null) {
            return "redirect:/login";
        }

        String rol = (String) session.getAttribute("rolUsuario");

        if (!"ADMINISTRADOR".equals(rol) &&
                !"DIRECCION_GENERAL".equals(rol) &&
                !"SUBDIRECCION".equals(rol) &&
                !"SECRETARIA".equals(rol)) {
            return "redirect:/?error=No autorizado";
        }

        return null;
    }
}
