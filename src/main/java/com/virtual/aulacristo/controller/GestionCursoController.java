package com.virtual.aulacristo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.virtual.aulacristo.model.Curso;
import com.virtual.aulacristo.model.Grado;
import com.virtual.aulacristo.service.CursoService;
import com.virtual.aulacristo.service.GradoService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/algo/cursos")
public class GestionCursoController {

    private final CursoService cursoService;
    private final GradoService gradoService;

    public GestionCursoController(
            CursoService cursoService,
            GradoService gradoService) {

        this.cursoService = cursoService;
        this.gradoService = gradoService;
    }

    // AVCR-145: Listar y registrar cursos
    @GetMapping
    public String mostrarCursos(
            HttpSession session,
            Model model) {

        String acceso = validarAcceso(session);

        if (acceso != null) {
            return acceso;
        }

        if (!model.containsAttribute("curso")) {
            Curso curso = new Curso();
            curso.setGrado(new Grado());

            model.addAttribute("curso", curso);
        }

        cargarDatosListado(model);

        return "cursos/lista";
    }

    // AVCR-145: Registrar curso desde la interfaz
    @PostMapping("/guardar")
    public String guardarCurso(
            @Valid @ModelAttribute("curso") Curso curso,
            BindingResult result,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        String acceso = validarAcceso(session);

        if (acceso != null) {
            return acceso;
        }

        if (result.hasErrors()) {

            if (curso.getGrado() == null) {
                curso.setGrado(new Grado());
            }

            model.addAttribute(
                    "mensajeError",
                    "Revisa los datos ingresados.");

            cargarDatosListado(model);

            return "cursos/lista";
        }

        try {
            cursoService.registrarCurso(curso);

            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "Curso registrado correctamente.");

            return "redirect:/cursos";

        } catch (IllegalArgumentException exception) {

            model.addAttribute(
                    "mensajeError",
                    exception.getMessage());

            cargarDatosListado(model);

            return "cursos/lista";
        }
    }

    // Mostrar formulario de edición
    @GetMapping("/editar/{id}")
    public String mostrarEdicion(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        String acceso = validarAcceso(session);

        if (acceso != null) {
            return acceso;
        }

        try {
            model.addAttribute(
                    "curso",
                    cursoService.buscarPorId(id));

            model.addAttribute(
                    "cursoId",
                    id);

            model.addAttribute(
                    "grados",
                    listarGradosActivos());

            return "cursos/formulario";

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    exception.getMessage());

            return "redirect:/cursos";
        }
    }

    // Actualizar curso
    @PostMapping("/actualizar/{id}")
    public String actualizarCurso(
            @PathVariable Long id,
            @Valid @ModelAttribute("curso") Curso curso,
            BindingResult result,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        String acceso = validarAcceso(session);

        if (acceso != null) {
            return acceso;
        }

        if (result.hasErrors()) {

            model.addAttribute("cursoId", id);
            model.addAttribute(
                    "grados",
                    listarGradosActivos());

            model.addAttribute(
                    "mensajeError",
                    "Revisa los datos ingresados.");

            return "cursos/formulario";
        }

        try {
            cursoService.actualizarCurso(id, curso);

            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "Curso actualizado correctamente.");

            return "redirect:/cursos";

        } catch (IllegalArgumentException exception) {

            model.addAttribute("cursoId", id);
            model.addAttribute(
                    "grados",
                    listarGradosActivos());

            model.addAttribute(
                    "mensajeError",
                    exception.getMessage());

            return "cursos/formulario";
        }
    }

    // Activar o desactivar curso
    @PostMapping("/{id}/estado")
    public String cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String acceso = validarAcceso(session);

        if (acceso != null) {
            return acceso;
        }

        try {
            cursoService.cambiarEstado(id, activo);

            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    activo
                            ? "Curso activado correctamente."
                            : "Curso desactivado correctamente.");

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    exception.getMessage());
        }

        return "redirect:/cursos";
    }

    private void cargarDatosListado(Model model) {

        List<Curso> cursos =
                cursoService.listarCursos();

        long cursosActivos = cursos.stream()
                .filter(Curso::isActivo)
                .count();

        long cursosInactivos =
                cursos.size() - cursosActivos;

        model.addAttribute("cursos", cursos);
        model.addAttribute(
                "grados",
                listarGradosActivos());

        model.addAttribute(
                "totalCursos",
                cursos.size());

        model.addAttribute(
                "cursosActivos",
                cursosActivos);

        model.addAttribute(
                "cursosInactivos",
                cursosInactivos);
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

        String rol =
                (String) session.getAttribute("rolUsuario");

        if (!"ADMINISTRADOR".equals(rol) &&
                !"DIRECCION_GENERAL".equals(rol) &&
                !"SUBDIRECCION".equals(rol) &&
                !"SECRETARIA".equals(rol)) {
            return "redirect:/?error=No autorizado";
        }

        return null;
    }
}