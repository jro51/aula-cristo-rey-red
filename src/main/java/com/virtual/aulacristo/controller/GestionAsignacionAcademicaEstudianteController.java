package com.virtual.aulacristo.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.virtual.aulacristo.model.Estudiante;
import com.virtual.aulacristo.model.Grado;
import com.virtual.aulacristo.model.Nivel;
import com.virtual.aulacristo.model.PeriodoAcademico;
import com.virtual.aulacristo.model.Seccion;
import com.virtual.aulacristo.service.AsignacionAcademicaEstudianteService;
import com.virtual.aulacristo.service.EstudianteService;
import com.virtual.aulacristo.service.GradoService;
import com.virtual.aulacristo.service.NivelService;
import com.virtual.aulacristo.service.SeccionService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/asignaciones-estudiantes")
public class GestionAsignacionAcademicaEstudianteController {

    private final AsignacionAcademicaEstudianteService
            asignacionService;

    private final EstudianteService estudianteService;
    private final NivelService nivelService;
    private final GradoService gradoService;
    private final SeccionService seccionService;

    public GestionAsignacionAcademicaEstudianteController(
            AsignacionAcademicaEstudianteService asignacionService,
            EstudianteService estudianteService,
            NivelService nivelService,
            GradoService gradoService,
            SeccionService seccionService) {

        this.asignacionService = asignacionService;
        this.estudianteService = estudianteService;
        this.nivelService = nivelService;
        this.gradoService = gradoService;
        this.seccionService = seccionService;
    }

    // AVCR-156: Mostrar interfaz de asignación
    @GetMapping
    public String mostrarAsignaciones(
            Model model,
            HttpSession session) {

        if (!puedeAdministrar(session)) {
            return "redirect:/";
        }

        cargarDatos(model);

        return "asignaciones-estudiantes/lista";
    }

    // AVCR-157: Consultar estudiantes por grado y sección
    @GetMapping("/consultar")
    public String consultarPorGradoYSeccion(
            @RequestParam(required = false) Long gradoId,
            @RequestParam(required = false) Long seccionId,
            Model model,
            HttpSession session) {

        if (!puedeAdministrar(session)) {
            return "redirect:/";
        }

        List<Nivel> nivelesActivos =
                nivelService.listarNiveles()
                    .stream()
                    .filter(Nivel::isActivo)
                    .toList();

        List<Grado> gradosActivos =
                gradoService.listarGrados()
                    .stream()
                    .filter(Grado::isActivo)
                    .toList();

        List<Seccion> seccionesActivas =
                seccionService.listarSecciones()
                    .stream()
                    .filter(Seccion::isActivo)
                    .toList();

        model.addAttribute(
            "niveles",
            nivelesActivos
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
            "gradoSeleccionadoId",
            gradoId
        );

        model.addAttribute(
            "seccionSeleccionadaId",
            seccionId
        );

        Long nivelSeleccionadoId = null;

        if (gradoId != null) {
            for (Grado grado : gradosActivos) {

                if (grado.getId().equals(gradoId)
                        && grado.getNivel() != null) {

                    nivelSeleccionadoId =
                            grado.getNivel().getId();

                    break;
                }
            }
        }

        model.addAttribute(
            "nivelSeleccionadoId",
            nivelSeleccionadoId
        );

        try {
            PeriodoAcademico periodoVigente =
                    asignacionService.obtenerPeriodoVigente();

            model.addAttribute(
                "periodoVigente",
                periodoVigente
            );

            if (gradoId != null && seccionId != null) {

                model.addAttribute(
                    "resultados",
                    asignacionService
                        .listarPorGradoYSeccion(
                            gradoId,
                            seccionId
                        )
                );

                model.addAttribute(
                    "consultaRealizada",
                    true
                );

            } else {

                model.addAttribute(
                    "resultados",
                    Collections.emptyList()
                );

                model.addAttribute(
                    "consultaRealizada",
                    false
                );
            }

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                "periodoVigente",
                null
            );

            model.addAttribute(
                "resultados",
                Collections.emptyList()
            );

            model.addAttribute(
                "consultaRealizada",
                false
            );

            model.addAttribute(
                "mensajeError",
                e.getMessage()
            );
        }

        return "asignaciones-estudiantes/consulta";
    }

    // Registrar o reasignar estudiante
    @Transactional
    @PostMapping("/guardar")
    public String guardarAsignacion(
            @RequestParam Long estudianteId,
            @RequestParam Long gradoId,
            @RequestParam Long seccionId,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        if (!puedeAdministrar(session)) {
            return "redirect:/";
        }

        try {
            asignacionService.asignarOReasignar(
                estudianteId,
                gradoId,
                seccionId
            );

            redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "La asignación académica fue guardada correctamente."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                "mensajeError",
                e.getMessage()
            );
        }

        return "redirect:/asignaciones-estudiantes";
    }

    @GetMapping("/parcial/lista")
    public String listarAsignacionesParcial(HttpSession session, Model model) {
        if (!puedeAdministrar(session)) return "";
        try {
            model.addAttribute("asignaciones", asignacionService.listarAsignacionesVigentes());
        } catch (Exception e) {
            model.addAttribute("asignaciones", Collections.emptyList());
        }
        return "asignaciones-estudiantes/lista :: tablaAsignacionesContainer";
    }

    @GetMapping("/parcial/consulta")
    public String consultarParcial(@RequestParam(required = false) Long gradoId,
                                   @RequestParam(required = false) Long seccionId,
                                   HttpSession session, Model model) {
        if (!puedeAdministrar(session)) return "";
        try {
            if (gradoId != null && seccionId != null) {
                model.addAttribute("resultados", asignacionService.listarPorGradoYSeccion(gradoId, seccionId));
                model.addAttribute("consultaRealizada", true);
            } else {
                model.addAttribute("resultados", Collections.emptyList());
                model.addAttribute("consultaRealizada", false);
            }
        } catch (Exception e) {
            model.addAttribute("resultados", Collections.emptyList());
            model.addAttribute("consultaRealizada", false);
        }
        return "asignaciones-estudiantes/consulta :: resultadosContainer";
    }

    private void cargarDatos(Model model) {

        List<Estudiante> estudiantesActivos =
                estudianteService.listarTodos()
                    .stream()
                    .filter(Estudiante::isActivo)
                    .toList();

        List<Nivel> nivelesActivos =
                nivelService.listarNiveles()
                    .stream()
                    .filter(Nivel::isActivo)
                    .toList();

        List<Grado> gradosActivos =
                gradoService.listarGrados()
                    .stream()
                    .filter(Grado::isActivo)
                    .toList();

        List<Seccion> seccionesActivas =
                seccionService.listarSecciones()
                    .stream()
                    .filter(Seccion::isActivo)
                    .toList();

        model.addAttribute(
            "estudiantes",
            estudiantesActivos
        );

        model.addAttribute(
            "niveles",
            nivelesActivos
        );

        model.addAttribute(
            "grados",
            gradosActivos
        );

        model.addAttribute(
            "secciones",
            seccionesActivas
        );

        try {
            PeriodoAcademico periodoVigente =
                    asignacionService.obtenerPeriodoVigente();

            model.addAttribute(
                "periodoVigente",
                periodoVigente
            );

            model.addAttribute(
                "asignaciones",
                asignacionService.listarAsignacionesVigentes()
            );

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                "periodoVigente",
                null
            );

            model.addAttribute(
                "asignaciones",
                Collections.emptyList()
            );

            if (!model.containsAttribute("mensajeError")) {
                model.addAttribute(
                    "mensajeError",
                    e.getMessage()
                );
            }
        }
    }

    private boolean puedeAdministrar(
            HttpSession session) {

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