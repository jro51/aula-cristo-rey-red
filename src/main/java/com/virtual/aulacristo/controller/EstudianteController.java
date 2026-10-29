package com.virtual.aulacristo.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.virtual.aulacristo.model.Estudiante;
import com.virtual.aulacristo.service.EstudianteService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/estudiantes")
public class EstudianteController {

    @Autowired
    private EstudianteService estudianteService;

    // AVCR-95, AVCR-98 y AVCR-99:
    // Listar y filtrar estudiantes con validación de permisos
    @GetMapping
    public String listarEstudiantes(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) String grado,
            @RequestParam(required = false) String seccion,
            @RequestParam(required = false) Boolean activo,
            HttpSession session,
            Model model) {

        // Verificar si inició sesión
        if (session.getAttribute("usuarioLogueado") == null) {
            return "redirect:/login";
        }

        // Verificar el rol
        if (!puedeConsultarEstudiantes(session)) {
            return "redirect:/?error=No autorizado";
        }

        model.addAttribute(
                "estudiantes",
                estudianteService.filtrarEstudiantes(
                        texto,
                        grado,
                        seccion,
                        activo));

        model.addAttribute("texto", texto);
        model.addAttribute("gradoSeleccionado", grado);
        model.addAttribute("seccionSeleccionada", seccion);
        model.addAttribute("estadoSeleccionado", activo);

        return "estudiantes/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioRegistro(HttpSession session, Model model) {
        String rol = (String) session.getAttribute("rolUsuario");
        if (rol == null || (!rol.equals("ADMINISTRADOR") && !rol.equals("DOCENTE"))) {
            return "redirect:/?error=No autorizado";
        }
        model.addAttribute("estudiante", new Estudiante());
        return "estudiantes/registro";
    }

    @PostMapping("/nuevo")
    public String procesarRegistro(
            HttpSession session,
            @Valid @ModelAttribute("estudiante") Estudiante estudiante,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttrs) {

        String rol = (String) session.getAttribute("rolUsuario");
        if (rol == null || (!rol.equals("ADMINISTRADOR") && !rol.equals("DOCENTE"))) {
            return "redirect:/?error=No autorizado";
        }

        if (result.hasErrors()) {
            return "estudiantes/registro";
        }

        try {
            estudianteService.registrarEstudiante(estudiante);

            redirectAttrs.addFlashAttribute(
                    "mensajeExito",
                    "Estudiante registrado exitosamente. Código: "
                            + estudiante.getCodigo());

        } catch (IllegalArgumentException e) {
            model.addAttribute("errorDni", e.getMessage());
            return "estudiantes/registro";
        }

        return "redirect:/estudiantes/nuevo";
    }

    // AVCR-94 y AVCR-99:
    // Consultar estudiante validando sesión y rol
    @GetMapping("/buscar")
    @ResponseBody
    public ResponseEntity<?> buscarEstudiante(
            @RequestParam String criterio,
            HttpSession session) {

        // No ha iniciado sesión
        if (session.getAttribute("usuarioLogueado") == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "mensaje",
                            "Debe iniciar sesión para consultar estudiantes."));
        }

        // Tiene sesión, pero no tiene autorización
        if (!puedeConsultarEstudiantes(session)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "mensaje",
                            "No tiene permisos para consultar estudiantes."));
        }

        if (criterio == null || criterio.trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "mensaje",
                            "Debe ingresar el código o DNI del estudiante."));
        }

        try {
            Estudiante estudiante = estudianteService.buscarPorCodigoODni(criterio);

            return ResponseEntity.ok(estudiante);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "mensaje",
                            e.getMessage()));
        }
    }

    // AVCR-101: Crear endpoint para actualizar estudiante (Carga la vista)
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        model.addAttribute("estudiante", estudianteService.obtenerEstudiantePorId(id));
        return "estudiantes/editar";
    }

    // AVCR-104: Validar campos modificados al enviar el formulario
    @PostMapping("/editar/{id}")
    public String procesarEdicion(@PathVariable Long id, @Valid @ModelAttribute("estudiante") Estudiante estudiante,
            BindingResult result, RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) {
            return "estudiantes/editar";
        }

        estudianteService.actualizarEstudiante(id, estudiante);

        // AVCR-107: Mostrar confirmación de actualización
        redirectAttrs.addFlashAttribute("mensajeExito", "Datos del estudiante actualizados correctamente.");
        return "redirect:/estudiantes/editar/" + id;
    }

    // AVCR-96 y AVCR-99:
    // Mostrar ficha por código o DNI con validación de permisos
    @GetMapping("/ficha")
    public String mostrarFichaPorCriterio(
            @RequestParam(required = false) String criterio,
            HttpSession session,
            Model model) {

        // Verificar si inició sesión
        if (session.getAttribute("usuarioLogueado") == null) {
            return "redirect:/login";
        }

        // Verificar si su rol está autorizado
        if (!puedeConsultarEstudiantes(session)) {
            return "redirect:/?error=No autorizado";
        }

        // Abrir la pantalla antes de buscar
        if (criterio == null || criterio.trim().isEmpty()) {
            return "estudiantes/ficha";
        }

        try {
            Estudiante estudiante = estudianteService.buscarPorCodigoODni(criterio);

            model.addAttribute("estudiante", estudiante);
            model.addAttribute("criterio", criterio.trim());

        } catch (IllegalArgumentException e) {
            model.addAttribute("errorBusqueda", e.getMessage());
            model.addAttribute("criterio", criterio.trim());
        }

        return "estudiantes/ficha";
    }

    // AVCR-96 y AVCR-99:
// Mostrar ficha por ID con validación de permisos
@GetMapping("/ficha/{id}")
public String mostrarFichaPorId(
        @PathVariable Long id,
        HttpSession session,
        Model model) {

    // Verificar si inició sesión
    if (session.getAttribute("usuarioLogueado") == null) {
        return "redirect:/login";
    }

    // Verificar si tiene autorización
    if (!puedeConsultarEstudiantes(session)) {
        return "redirect:/?error=No autorizado";
    }

    try {
        Estudiante estudiante =
                estudianteService.obtenerEstudiantePorId(id);

        model.addAttribute("estudiante", estudiante);

    } catch (IllegalArgumentException e) {
        model.addAttribute("errorBusqueda", e.getMessage());
    }

    return "estudiantes/ficha";
}

    // AVCR-99: Validar si el usuario puede consultar estudiantes
    private boolean puedeConsultarEstudiantes(HttpSession session) {

        String rol = (String) session.getAttribute("rolUsuario");

        return "ADMINISTRADOR".equals(rol) ||
                "DIRECCION_GENERAL".equals(rol) ||
                "SUBDIRECCION".equals(rol) ||
                "SECRETARIA".equals(rol);
    }

    // Endpoint para actualizar solo la tabla en tiempo real vía AJAX
    @GetMapping("/parcial")
    public String listarEstudiantesParcial(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) String grado,
            @RequestParam(required = false) String seccion,
            @RequestParam(required = false) Boolean activo,
            HttpSession session,
            Model model) {

        if (session.getAttribute("usuarioLogueado") == null || !puedeConsultarEstudiantes(session)) {
            return "";
        }

        model.addAttribute("estudiantes", estudianteService.filtrarEstudiantes(texto, grado, seccion, activo));
        return "estudiantes/tabla-estudiantes :: tablaEstudiantes";
    }
}