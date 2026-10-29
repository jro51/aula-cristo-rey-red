package com.virtual.aulacristo.controller;

import com.virtual.aulacristo.entity.Usuario;
import com.virtual.aulacristo.model.AsignacionAcademicaEstudiante;
import com.virtual.aulacristo.model.Estudiante;
import com.virtual.aulacristo.repository.AsignacionAcademicaEstudianteRepository;
import com.virtual.aulacristo.repository.CursoRepository;
import com.virtual.aulacristo.repository.EstudianteRepository;
import com.virtual.aulacristo.repository.GradoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class DashboardController {

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private AsignacionAcademicaEstudianteRepository asignacionRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private GradoRepository gradoRepository;

    // AVCR-66: Restringir acceso a rutas después del cierre de sesión
    @GetMapping("/")
    public String inicio(HttpSession session, Model model) {

        Object usuarioObj = session.getAttribute("usuarioLogueado");
        if (usuarioObj == null) {
            return "redirect:/login";
        }

        String rol = (String) session.getAttribute("rolUsuario");

        // AVCR-218: Si es ESTUDIANTE, cargar su perfil
        if ("ESTUDIANTE".equals(rol)) {
            Usuario usuario = (Usuario) usuarioObj;
            String username = usuario.getUsername();

            Optional<Estudiante> estudianteOpt =
                    estudianteRepository.findByCodigoIgnoreCaseOrDni(username, username);

            if (estudianteOpt.isPresent()) {
                Estudiante estudiante = estudianteOpt.get();
                model.addAttribute("estudiante", estudiante);

                // Solo cargamos asignacion basica (sin cursos) para el mensaje de bienvenida
                List<AsignacionAcademicaEstudiante> asignaciones =
                        asignacionRepository.findAllByOrderByFechaAsignacionDesc();

                asignaciones.stream()
                        .filter(a -> a.getEstudiante().getId().equals(estudiante.getId()) && a.isActivo())
                        .findFirst()
                        .ifPresent(asignacion -> model.addAttribute("asignacion", asignacion));
            }
        } else {
            // SEGURIDAD PARA OTROS ROLES: Añadimos atributos nulos explícitos
            // para evitar que Thymeleaf lance NullPointerException en el index.html
            model.addAttribute("estudiante", null);
            model.addAttribute("asignacion", null);
            model.addAttribute("cursos", null);
        }

        return "index";
    }

    // AVCR-221: Nueva ruta dedicada para "Mis Cursos" del estudiante
    @GetMapping("/estudiante/mis-cursos")
    public String misCursos(HttpSession session, Model model) {
        Object usuarioObj = session.getAttribute("usuarioLogueado");
        if (usuarioObj == null) {
            return "redirect:/login";
        }

        String rol = (String) session.getAttribute("rolUsuario");
        if (!"ESTUDIANTE".equals(rol)) {
            return "redirect:/?error=No autorizado";
        }

        Usuario usuario = (Usuario) usuarioObj;
        String username = usuario.getUsername();

        Optional<Estudiante> estudianteOpt =
            estudianteRepository.findByCodigoIgnoreCaseOrDni(username, username);

        if (estudianteOpt.isPresent()) {
            Estudiante estudiante = estudianteOpt.get();
            model.addAttribute("estudiante", estudiante);

            List<AsignacionAcademicaEstudiante> asignaciones =
                asignacionRepository.findAllByOrderByFechaAsignacionDesc();

            asignaciones.stream()
                .filter(a -> a.getEstudiante().getId().equals(estudiante.getId()) && a.isActivo())
                .findFirst()
                .ifPresentOrElse(
                    asignacion -> {
                        model.addAttribute("asignacion", asignacion);
                        model.addAttribute("cursos",
                            cursoRepository.findByGradoIdAndActivoTrueOrderByNombreAsc(
                                asignacion.getGrado().getId()));
                    },
                    () -> {
                        // Fallback: buscar por texto del grado del estudiante
                        String gradoTexto = estudiante.getGrado();
                        if (gradoTexto != null && !gradoTexto.isBlank()) {
                            gradoRepository.findAll().stream()
                                .filter(g -> gradoTexto.toLowerCase().contains(g.getNombre().toLowerCase())
                                          || g.getNombre().toLowerCase().contains(gradoTexto.toLowerCase()))
                                .findFirst()
                                .ifPresent(grado -> model.addAttribute("cursos",
                                    cursoRepository.findByGradoIdAndActivoTrueOrderByNombreAsc(grado.getId())));
                        }
                    }
                );
        }

        return "estudiante/mis-cursos";
    }

    // AVCR-66: Restringir acceso a rutas después del cierre de sesión
    @GetMapping("/admin/panel")
    public String panelAdministrador(HttpSession session) {
        String rol = (String) session.getAttribute("rolUsuario");

        if (rol == null || !rol.equals("ADMINISTRADOR")) {
            return "redirect:/?error=No autorizado";
        }

        return "admin-panel";
    }
}