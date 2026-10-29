package com.virtual.aulacristo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.virtual.aulacristo.entity.Usuario;
import com.virtual.aulacristo.model.Grado;
import com.virtual.aulacristo.model.Seccion;
import com.virtual.aulacristo.service.AsignacionDocenteService;
import com.virtual.aulacristo.service.CursoService;
import com.virtual.aulacristo.service.GradoService;
import com.virtual.aulacristo.service.SeccionService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/asignaciones-docente")
public class GestionAsignacionDocenteController {

    private final AsignacionDocenteService asignacionService;
    private final CursoService cursoService;
    private final GradoService gradoService;
    private final SeccionService seccionService;

    public GestionAsignacionDocenteController(
            AsignacionDocenteService asignacionService,
            CursoService cursoService,
            GradoService gradoService,
            SeccionService seccionService) {

        this.asignacionService = asignacionService;
        this.cursoService = cursoService;
        this.gradoService = gradoService;
        this.seccionService = seccionService;
    }

    // Interfaz del administrador
    @GetMapping
    public String mostrarAsignaciones(
            Model model,
            HttpSession session) {

        if (!puedeAdministrar(session)) {
            return "redirect:/";
        }

        cargarDatos(model);

        return "asignaciones/lista";
    }

    // AVCR-151: Cursos asignados al docente autenticado
    @GetMapping("/mis-cursos")
    public String mostrarMisCursos(
            Model model,
            HttpSession session) {

        Object usuarioSesion =
                session.getAttribute("usuarioLogueado");

        if (!(usuarioSesion instanceof Usuario)) {
            return "redirect:/";
        }

        String rol =
                (String) session.getAttribute("rolUsuario");

        if (!"DOCENTE".equalsIgnoreCase(rol)) {
            return "redirect:/";
        }

        Usuario docente = (Usuario) usuarioSesion;

        model.addAttribute(
            "docente",
            docente
        );

        model.addAttribute(
            "asignaciones",
            asignacionService.listarActivasPorDocente(
                docente.getIdUsuario()
            )
        );

        return "asignaciones/mis-cursos";
    }

    // Registrar una asignación
    @Transactional
    @PostMapping("/guardar")
    public String guardarAsignacion(
            @RequestParam Long docenteId,
            @RequestParam Long cursoId,
            @RequestParam Long gradoId,
            @RequestParam Long seccionId,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        if (!puedeAdministrar(session)) {
            return "redirect:/";
        }

        try {
            asignacionService.asignarDocente(
                docenteId,
                cursoId,
                gradoId,
                seccionId
            );

            redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "El docente fue asignado correctamente."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                "mensajeError",
                e.getMessage()
            );
        }

        return "redirect:/asignaciones-docente";
    }

    // Activar o desactivar una asignación
    @Transactional
    @PostMapping("/{id}/estado")
    public String cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        if (!puedeAdministrar(session)) {
            return "redirect:/";
        }

        try {
            asignacionService.cambiarEstado(id, activo);

            String mensaje = activo
                    ? "La asignación fue activada correctamente."
                    : "La asignación fue desactivada correctamente.";

            redirectAttributes.addFlashAttribute(
                "mensajeExito",
                mensaje
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                "mensajeError",
                e.getMessage()
            );
        }

        return "redirect:/asignaciones-docente";
    }

    @GetMapping("/parcial")
    public String listarAsignacionesParcial(HttpSession session, Model model) {
        if (!puedeAdministrar(session)) {
            return "";
        }
        model.addAttribute("asignaciones", asignacionService.listarAsignaciones());
        return "asignaciones/lista :: tablaAsignacionesContainer";
    }

    @GetMapping("/mis-cursos-parcial")
    public String listarMisCursosParcial(HttpSession session, Model model) {
        Object usuarioSesion = session.getAttribute("usuarioLogueado");

        if (!(usuarioSesion instanceof Usuario)) {
            return "";
        }

        String rol = (String) session.getAttribute("rolUsuario");

        if (!"DOCENTE".equalsIgnoreCase(rol)) {
            return "";
        }

        Usuario docente = (Usuario) usuarioSesion;

        model.addAttribute("docente", docente);
        model.addAttribute(
                "asignaciones",
                asignacionService.listarActivasPorDocente(docente.getIdUsuario())
        );

        return "asignaciones/mis-cursos :: misCursosContainer";
    }

    private void cargarDatos(Model model) {

        List<Grado> gradosActivos = gradoService
                .listarGrados()
                .stream()
                .filter(Grado::isActivo)
                .toList();

        List<Seccion> seccionesActivas = seccionService
                .listarSecciones()
                .stream()
                .filter(Seccion::isActivo)
                .toList();

        model.addAttribute(
            "docentes",
            asignacionService.listarDocentesActivos()
        );

        model.addAttribute(
            "cursos",
            cursoService.listarCursosActivos()
        );

        model.addAttribute(
            "grados",
            gradosActivos
        );

        model.addAttribute(
            "secciones",
            seccionesActivas
        );

        model.addAttribute(
            "asignaciones",
            asignacionService.listarAsignaciones()
        );
    }

    private boolean puedeAdministrar(HttpSession session) {

        if (session.getAttribute("usuarioLogueado") == null) {
            return false;
        }

        String rol =
                (String) session.getAttribute("rolUsuario");

        return "ADMINISTRADOR".equalsIgnoreCase(rol) ||
                "DIRECCION_GENERAL".equalsIgnoreCase(rol) ||
                "SUBDIRECCION".equalsIgnoreCase(rol) ||
                "SECRETARIA".equalsIgnoreCase(rol);
    }
}