package com.virtual.aulacristo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.virtual.aulacristo.model.AnioAcademico;
import com.virtual.aulacristo.model.PeriodoAcademico;
import com.virtual.aulacristo.service.AnioAcademicoService;
import com.virtual.aulacristo.service.PeriodoAcademicoService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/academico/periodos")
public class ConfiguracionPeriodoController {

    private final AnioAcademicoService anioAcademicoService;
    private final PeriodoAcademicoService periodoAcademicoService;

    public ConfiguracionPeriodoController(
            AnioAcademicoService anioAcademicoService,
            PeriodoAcademicoService periodoAcademicoService) {

        this.anioAcademicoService = anioAcademicoService;
        this.periodoAcademicoService = periodoAcademicoService;
    }

    // AVCR-138: Mostrar interfaz de configuración
    @GetMapping
    public String mostrarConfiguracion(
            HttpSession session,
            Model model) {

        if (session.getAttribute("usuarioLogueado") == null) {
            return "redirect:/login";
        }

        if (!esAdministrador(session)) {
            return "redirect:/?error=No autorizado";
        }

        if (!model.containsAttribute("anioAcademico")) {
            model.addAttribute(
                    "anioAcademico",
                    new AnioAcademico());
        }

        if (!model.containsAttribute("periodoAcademico")) {
            PeriodoAcademico periodoAcademico =
                    new PeriodoAcademico();

            periodoAcademico.setAnioAcademico(
                    new AnioAcademico());

            model.addAttribute(
                    "periodoAcademico",
                    periodoAcademico);
        }

        model.addAttribute(
                "aniosAcademicos",
                anioAcademicoService.listarTodos());

        model.addAttribute(
                "periodosAcademicos",
                periodoAcademicoService.listarTodos());

        return "academico/periodos";
    }

    // AVCR-136 y AVCR-138: Registrar año desde la interfaz
    @PostMapping("/anios/guardar")
    public String guardarAnio(
            @ModelAttribute("anioAcademico")
            AnioAcademico anioAcademico,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("usuarioLogueado") == null) {
            return "redirect:/login";
        }

        if (!esAdministrador(session)) {
            return "redirect:/?error=No autorizado";
        }

        try {
            anioAcademicoService.registrar(anioAcademico);

            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "Año académico registrado correctamente.");

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    exception.getMessage());
        }

        return "redirect:/academico/periodos";
    }

    // AVCR-137 y AVCR-138: Registrar periodo desde la interfaz
    @PostMapping("/guardar")
    public String guardarPeriodo(
            @ModelAttribute("periodoAcademico")
            PeriodoAcademico periodoAcademico,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("usuarioLogueado") == null) {
            return "redirect:/login";
        }

        if (!esAdministrador(session)) {
            return "redirect:/?error=No autorizado";
        }

        try {
            periodoAcademicoService.registrar(
                    periodoAcademico);

            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "Periodo académico registrado correctamente.");

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    exception.getMessage());
        }

        return "redirect:/academico/periodos";
    }

    // AVCR-139: Activar periodo vigente desde la interfaz
    @PostMapping("/{id}/vigente")
    public String activarPeriodoVigente(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("usuarioLogueado") == null) {
            return "redirect:/login";
        }

        if (!esAdministrador(session)) {
            return "redirect:/?error=No autorizado";
        }

        try {
            PeriodoAcademico periodo =
                    periodoAcademicoService
                            .activarComoVigente(id);

            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    periodo.getNombre()
                            + " fue establecido como periodo vigente.");

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    exception.getMessage());
        }

        return "redirect:/academico/periodos";
    }

    private boolean esAdministrador(HttpSession session) {

        String rol =
                (String) session.getAttribute("rolUsuario");

        return "ADMINISTRADOR".equals(rol) ||
                "DIRECCION_GENERAL".equals(rol) ||
                "SUBDIRECCION".equals(rol);
    }
}