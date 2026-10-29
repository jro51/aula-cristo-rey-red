package com.virtual.aulacristo.controller;

import com.virtual.aulacristo.entity.Usuario;
import com.virtual.aulacristo.model.Curso;
import com.virtual.aulacristo.model.Tarea;
import com.virtual.aulacristo.repository.AsignacionAcademicaEstudianteRepository;
import com.virtual.aulacristo.repository.CursoRepository;
import com.virtual.aulacristo.repository.EstudianteRepository;
import com.virtual.aulacristo.repository.GradoRepository;
import com.virtual.aulacristo.repository.TareaRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/tareas")
public class TareaController {

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private AsignacionAcademicaEstudianteRepository asignacionRepository;

    @Autowired
    private GradoRepository gradoRepository;

    // ─── Vista del DOCENTE/ADMIN: lista de tareas por curso ───────────────────────────────

    @GetMapping("/curso/{cursoId}")
    public String listarTareasPorCurso(
            @PathVariable Long cursoId,
            Model model,
            HttpSession session) {

        String rol = (String) session.getAttribute("rolUsuario");
        if ("ADMINISTRADOR".equals(rol)) {
            return "redirect:/"; // Bloquea al administrador si se desea
        }

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("Curso inválido Id:" + cursoId));

        // Obtener tareas publicadas de este curso (puedes ajustar el repositorio para traer todas las tareas del curso si es necesario)
        List<Tarea> tareas = tareaRepository.findAll().stream()
                .filter(t -> t.getCurso() != null && t.getCurso().getId().equals(cursoId))
                .toList();

        model.addAttribute("curso", curso);
        model.addAttribute("tareas", tareas);
        
        return "tareas/docente-curso";
    }

    @GetMapping("/curso/{cursoId}/parcial")
    public String listarTareasPorCursoParcial(
            @PathVariable Long cursoId,
            HttpSession session,
            Model model) {

        String rol = (String) session.getAttribute("rolUsuario");
        if ("ADMINISTRADOR".equals(rol)) return "";

        Curso curso = cursoRepository.findById(cursoId).orElse(null);
        if (curso == null) return "";

        List<Tarea> tareas = tareaRepository.findAll().stream()
                .filter(t -> t.getCurso() != null && t.getCurso().getId().equals(cursoId))
                .toList();

        model.addAttribute("curso", curso);
        model.addAttribute("tareas", tareas);

        return "tareas/docente-curso :: tablaDocenteCursoContainer";
    }

    @GetMapping("/parcial")
    public String listarTareasGeneralParcial(Model model, HttpSession session) {
        String rol = (String) session.getAttribute("rolUsuario");
        if ("ADMINISTRADOR".equals(rol)) return "";

        model.addAttribute("tareas", tareaRepository.findAll());
        return "tareas/lista :: tablaTareasPublicadasContainer";
    }

    @GetMapping("/publicar/{cursoId}")
    public String mostrarFormularioPublicar(
            @PathVariable Long cursoId,
            Model model,
            HttpSession session) {

        if ("ADMINISTRADOR".equals(session.getAttribute("rolUsuario"))) {
            return "redirect:/";
        }
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("Curso inválido Id:" + cursoId));

        Tarea tarea = new Tarea();
        tarea.setCurso(curso);

        model.addAttribute("tarea", tarea);
        return "tareas/publicar";
    }

    @Transactional
    @PostMapping("/guardar")
    public String guardarTarea(@ModelAttribute Tarea tarea, HttpSession session) {
        if ("ADMINISTRADOR".equals(session.getAttribute("rolUsuario"))) {
            return "redirect:/";
        }
        tareaRepository.save(tarea);
        return "redirect:/tareas/curso/" + tarea.getCurso().getId();
    }

    // ─── AVCR-218/219/220/221/222: Vista del ESTUDIANTE ─────────────────────

    /**
     * AVCR-221: Vista de tareas/materiales para el estudiante.
     * AVCR-218: Consulta de contenido por estudiante (solo su grado).
     * AVCR-219: Muestra tareas de los cursos matriculados.
     * AVCR-220: Expone fechaLimite y estado (calculado en el modelo).
     * AVCR-222: Permite filtrar por curso específico.
     * AVCR-223: Solo el rol ESTUDIANTE accede a esta vista.
     */
    @GetMapping("/mis-tareas")
    public String misTareas(
            @RequestParam(required = false) Long cursoId,
            HttpSession session,
            Model model) {

        // AVCR-223: Validar que el usuario sea ESTUDIANTE
        if (session.getAttribute("usuarioLogueado") == null) {
            return "redirect:/login";
        }
        String rol = (String) session.getAttribute("rolUsuario");
        if (!"ESTUDIANTE".equals(rol)) {
            return "redirect:/?error=No autorizado";
        }

        // AVCR-218: Obtener el grado del estudiante desde la sesión
        // El username del usuario corresponde al código del estudiante
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        String username = usuario.getUsername();

        // Buscar el estudiante por su código (username)
        var estudianteOpt = estudianteRepository.findByCodigoIgnoreCaseOrDni(username, username);
        if (estudianteOpt.isEmpty()) {
            model.addAttribute("error", "No se encontró el perfil del estudiante.");
            model.addAttribute("tareas", List.of());
            model.addAttribute("cursos", List.of());
            return "tareas/mis-tareas";
        }

        var estudiante = estudianteOpt.get();

        // AVCR-218/219: Obtener el gradoId desde la última asignación activa
        // Buscamos la asignación académica vigente del estudiante
        var asignaciones = asignacionRepository.findAllByOrderByFechaAsignacionDesc();
        Long gradoId = asignaciones.stream()
                .filter(a -> a.getEstudiante().getId().equals(estudiante.getId()) && a.isActivo())
                .map(a -> a.getGrado().getId())
                .findFirst()
                .orElse(null);

        if (gradoId == null) {
            // Fallback: buscar el grado por el campo texto del estudiante (ej: "1ro Secundaria")
            String gradoTexto = estudiante.getGrado();
            if (gradoTexto != null && !gradoTexto.isBlank()) {
                gradoId = gradoRepository.findAll().stream()
                    .filter(g -> gradoTexto.toLowerCase().contains(g.getNombre().toLowerCase())
                              || g.getNombre().toLowerCase().contains(gradoTexto.toLowerCase()))
                    .map(g -> g.getId())
                    .findFirst()
                    .orElse(null);
            }
        }

        if (gradoId == null) {
            model.addAttribute("error", "El estudiante no tiene una asignación académica activa ni un grado reconocido.");
            model.addAttribute("tareas", List.of());
            model.addAttribute("cursos", List.of());
            return "tareas/mis-tareas";
        }

        // AVCR-222: Cursos con tareas publicadas (para el filtro)
        List<Curso> cursosDisponibles =
                tareaRepository.findCursosConTareasPublicadasPorGrado(gradoId);

        // AVCR-219/222: Tareas filtradas por curso (opcional)
        List<Tarea> tareas;
        if (cursoId != null) {
            tareas = tareaRepository.findTareasPublicadasPorGradoYCurso(gradoId, cursoId);
            // Pasar el objeto curso al modelo para el título
            cursosDisponibles.stream()
                .filter(c -> c.getId().equals(cursoId))
                .findFirst()
                .ifPresent(c -> model.addAttribute("cursoInfo", c));
        } else {
            tareas = tareaRepository.findTareasPublicadasPorGrado(gradoId);
        }

        model.addAttribute("tareas", tareas);
        model.addAttribute("cursos", cursosDisponibles);
        model.addAttribute("cursoSeleccionado", cursoId);
        model.addAttribute("estudiante", estudiante);

        return "tareas/mis-tareas";
    }

    @GetMapping("/mis-tareas/parcial")
    public String misTareasParcial(
            @RequestParam(required = false) Long cursoId,
            HttpSession session,
            Model model) {

        if (session.getAttribute("usuarioLogueado") == null) {
            return "";
        }

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        String username = usuario.getUsername();
        var estudianteOpt = estudianteRepository.findByCodigoIgnoreCaseOrDni(username, username);
        if (estudianteOpt.isEmpty()) return "";
        var estudiante = estudianteOpt.get();

        var asignaciones = asignacionRepository.findAllByOrderByFechaAsignacionDesc();
        Long gradoId = asignaciones.stream()
                .filter(a -> a.getEstudiante().getId().equals(estudiante.getId()) && a.isActivo())
                .map(a -> a.getGrado().getId())
                .findFirst()
                .orElse(null);

        if (gradoId == null) {
            String gradoTexto = estudiante.getGrado();
            if (gradoTexto != null && !gradoTexto.isBlank()) {
                gradoId = gradoRepository.findAll().stream()
                        .filter(g -> gradoTexto.toLowerCase().contains(g.getNombre().toLowerCase())
                                || g.getNombre().toLowerCase().contains(gradoTexto.toLowerCase()))
                        .map(g -> g.getId())
                        .findFirst()
                        .orElse(null);
            }
        }

        if (gradoId == null) return "";

        List<Tarea> tareas;
        if (cursoId != null) {
            tareas = tareaRepository.findTareasPublicadasPorGradoYCurso(gradoId, cursoId);
        } else {
            tareas = tareaRepository.findTareasPublicadasPorGrado(gradoId);
        }

        model.addAttribute("tareas", tareas);
        return "tareas/mis-tareas :: listaTareasContainer";
    }
}
