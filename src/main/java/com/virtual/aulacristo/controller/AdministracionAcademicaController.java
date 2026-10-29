package com.virtual.aulacristo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.virtual.aulacristo.model.Grado;
import com.virtual.aulacristo.model.Nivel;
import com.virtual.aulacristo.model.Seccion;
import com.virtual.aulacristo.service.GradoService;
import com.virtual.aulacristo.service.NivelService;
import com.virtual.aulacristo.service.SeccionService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/academico")
public class AdministracionAcademicaController {

    private final NivelService nivelService;
    private final GradoService gradoService;
    private final SeccionService seccionService;

    public AdministracionAcademicaController(
            NivelService nivelService,
            GradoService gradoService,
            SeccionService seccionService) {

        this.nivelService = nivelService;
        this.gradoService = gradoService;
        this.seccionService = seccionService;
    }

    // AVCR-131: Mostrar panel académico
    @GetMapping
    public String mostrarPanel(
            HttpSession session,
            Model model) {

        String validacion = validarAdministrador(session);

        if (validacion != null) {
            return validacion;
        }

        Nivel nuevoNivel = new Nivel();

        Grado nuevoGrado = new Grado();
        nuevoGrado.setNivel(new Nivel());

        Seccion nuevaSeccion = new Seccion();
        nuevaSeccion.setGrado(new Grado());

        model.addAttribute("nuevoNivel", nuevoNivel);
        model.addAttribute("nuevoGrado", nuevoGrado);
        model.addAttribute("nuevaSeccion", nuevaSeccion);

        model.addAttribute(
            "niveles",
            nivelService.listarNiveles()
        );

        model.addAttribute(
            "grados",
            gradoService.listarGrados()
        );

        model.addAttribute(
            "secciones",
            seccionService.listarSecciones()
        );

        return "academico/panel";
    }

    // Registrar nivel
    @Transactional
    @PostMapping("/niveles/guardar")
    public String guardarNivel(
            HttpSession session,
            @Valid @ModelAttribute("nuevoNivel") Nivel nivel,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        String validacion = validarAdministrador(session);

        if (validacion != null) {
            return validacion;
        }

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                "mensajeError",
                "Debe ingresar correctamente los datos del nivel."
            );

            return "redirect:/academico";
        }

        try {
            nivelService.registrarNivel(nivel);

            redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "Nivel registrado correctamente."
            );

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                "mensajeError",
                e.getMessage()
            );
        }

        return "redirect:/academico";
    }

    // Registrar grado
    @Transactional
    @PostMapping("/grados/guardar")
    public String guardarGrado(
            HttpSession session,
            @Valid @ModelAttribute("nuevoGrado") Grado grado,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        String validacion = validarAdministrador(session);

        if (validacion != null) {
            return validacion;
        }

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                "mensajeError",
                "Debe completar correctamente los datos del grado."
            );

            return "redirect:/academico";
        }

        try {
            gradoService.registrarGrado(grado);

            redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "Grado registrado correctamente."
            );

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                "mensajeError",
                e.getMessage()
            );
        }

        return "redirect:/academico";
    }

    // Registrar sección
    @Transactional
    @PostMapping("/secciones/guardar")
    public String guardarSeccion(
            HttpSession session,
            @Valid @ModelAttribute("nuevaSeccion") Seccion seccion,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        String validacion = validarAdministrador(session);

        if (validacion != null) {
            return validacion;
        }

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                "mensajeError",
                "Debe completar correctamente los datos de la sección."
            );

            return "redirect:/academico";
        }

        try {
            seccionService.registrarSeccion(seccion);

            redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "Sección registrada correctamente."
            );

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                "mensajeError",
                e.getMessage()
            );
        }

        return "redirect:/academico";
    }

    // Activar o desactivar nivel
    @Transactional
    @PostMapping("/niveles/{id}/estado")
    public String cambiarEstadoNivel(
            @PathVariable Long id,
            @RequestParam boolean activo,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String validacion = validarAdministrador(session);

        if (validacion != null) {
            return validacion;
        }

        nivelService.cambiarEstado(id, activo);

        redirectAttributes.addFlashAttribute(
            "mensajeExito",
            "Estado del nivel actualizado."
        );

        return "redirect:/academico";
    }

    // Activar o desactivar grado
    @Transactional
    @PostMapping("/grados/{id}/estado")
    public String cambiarEstadoGrado(
            @PathVariable Long id,
            @RequestParam boolean activo,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String validacion = validarAdministrador(session);

        if (validacion != null) {
            return validacion;
        }

        gradoService.cambiarEstado(id, activo);

        redirectAttributes.addFlashAttribute(
            "mensajeExito",
            "Estado del grado actualizado."
        );

        return "redirect:/academico";
    }

    // Activar o desactivar sección
    @Transactional
    @PostMapping("/secciones/{id}/estado")
    public String cambiarEstadoSeccion(
            @PathVariable Long id,
            @RequestParam boolean activo,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String validacion = validarAdministrador(session);

        if (validacion != null) {
            return validacion;
        }

        seccionService.cambiarEstado(id, activo);

        redirectAttributes.addFlashAttribute(
            "mensajeExito",
            "Estado de la sección actualizado."
        );

        return "redirect:/academico";
    }

    // Validar sesión y rol
    private String validarAdministrador(HttpSession session) {

        if (session.getAttribute("usuarioLogueado") == null) {
            return "redirect:/login";
        }

        String rol = (String) session.getAttribute("rolUsuario");
        if (!"ADMINISTRADOR".equals(rol) &&
                !"DIRECCION_GENERAL".equals(rol) &&
                !"SUBDIRECCION".equals(rol)) {
            return "redirect:/?error=No autorizado";
        }

        return null;
    }

    @GetMapping("/parcial/niveles")
    public String listarNivelesParcial(HttpSession session, Model model) {
        if (validarAdministrador(session) != null) return "";
        model.addAttribute("niveles", nivelService.listarNiveles());
        return "academico/panel :: tablaNiveles";
    }

    @GetMapping("/parcial/grados")
    public String listarGradosParcial(HttpSession session, Model model) {
        if (validarAdministrador(session) != null) return "";
        model.addAttribute("grados", gradoService.listarGrados());
        model.addAttribute("niveles", nivelService.listarNiveles());
        return "academico/panel :: tablaGrados";
    }

    @GetMapping("/parcial/secciones")
    public String listarSeccionesParcial(HttpSession session, Model model) {
        if (validarAdministrador(session) != null) return "";
        model.addAttribute("secciones", seccionService.listarSecciones());
        model.addAttribute("grados", gradoService.listarGrados());
        return "academico/panel :: tablaSecciones";
    }
}