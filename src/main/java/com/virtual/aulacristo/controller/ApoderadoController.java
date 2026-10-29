package com.virtual.aulacristo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.virtual.aulacristo.model.Apoderado;
import com.virtual.aulacristo.service.ApoderadoService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/apoderados")
public class ApoderadoController {

    @Autowired
    private ApoderadoService apoderadoService;

    // Listar apoderados (Para AVCR-113)
    @GetMapping
    public String listarApoderados(HttpSession session, Model model) {
        if (session.getAttribute("usuarioLogueado") == null) {
            return "redirect:/login";
        }
        
        String rol = (String) session.getAttribute("rolUsuario");
        if (rol == null || (!rol.equals("ADMINISTRADOR") &&
                !rol.equals("DIRECCION_GENERAL") &&
                !rol.equals("SUBDIRECCION") &&
                !rol.equals("SECRETARIA") &&
                !rol.equals("DOCENTE"))) {
            return "redirect:/?error=No autorizado";
        }

        model.addAttribute("apoderados", apoderadoService.listarTodos());
        return "apoderados/lista";
    }

    // Mostrar formulario de registro (Para AVCR-112)
    @GetMapping("/nuevo")
    public String mostrarFormularioRegistro(HttpSession session, Model model) {
        String rol = (String) session.getAttribute("rolUsuario");
        if (rol == null || (!rol.equals("ADMINISTRADOR") &&
                !rol.equals("DIRECCION_GENERAL") &&
                !rol.equals("SUBDIRECCION") &&
                !rol.equals("SECRETARIA") &&
                !rol.equals("DOCENTE"))) {
            return "redirect:/?error=No autorizado";
        }
        model.addAttribute("apoderado", new Apoderado());
        return "apoderados/registro";
    }

    // Procesar registro (Para AVCR-112)
    @Transactional
    @PostMapping("/nuevo")
    public String procesarRegistro(
            HttpSession session,
            @Valid @ModelAttribute("apoderado") Apoderado apoderado,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttrs) {

        String rol = (String) session.getAttribute("rolUsuario");
        if (rol == null || (!rol.equals("ADMINISTRADOR") &&
                !rol.equals("DIRECCION_GENERAL") &&
                !rol.equals("SUBDIRECCION") &&
                !rol.equals("SECRETARIA") &&
                !rol.equals("DOCENTE"))) {
            return "redirect:/?error=No autorizado";
        }

        if (result.hasErrors()) {
            return "apoderados/registro";
        }

        try {
            apoderadoService.registrarApoderado(apoderado);
            redirectAttrs.addFlashAttribute("mensajeExito", "Apoderado registrado exitosamente.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorDni", e.getMessage());
            return "apoderados/registro";
        }

        return "redirect:/apoderados/nuevo";
    }

    // Mostrar formulario de edición (Para AVCR-114)
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(
            @PathVariable Long id, 
            HttpSession session, 
            Model model) {
            
        String rol = (String) session.getAttribute("rolUsuario");
        if (rol == null || (!rol.equals("ADMINISTRADOR") &&
                !rol.equals("DIRECCION_GENERAL") &&
                !rol.equals("SUBDIRECCION") &&
                !rol.equals("SECRETARIA") &&
                !rol.equals("DOCENTE"))) {
            return "redirect:/?error=No autorizado";
        }

        model.addAttribute("apoderado", apoderadoService.obtenerApoderadoPorId(id));
        return "apoderados/editar";
    }

    // Procesar edición (Para AVCR-114)
    @Transactional
    @PostMapping("/editar/{id}")
    public String procesarEdicion(
            @PathVariable Long id, 
            @Valid @ModelAttribute("apoderado") Apoderado apoderado,
            BindingResult result, 
            HttpSession session,
            RedirectAttributes redirectAttrs) {
            
        String rol = (String) session.getAttribute("rolUsuario");
        if (rol == null || (!rol.equals("ADMINISTRADOR") &&
                !rol.equals("DIRECCION_GENERAL") &&
                !rol.equals("SUBDIRECCION") &&
                !rol.equals("SECRETARIA") &&
                !rol.equals("DOCENTE"))) {
            return "redirect:/?error=No autorizado";
        }

        if (result.hasErrors()) {
            return "apoderados/editar";
        }

        apoderadoService.actualizarApoderado(id, apoderado);
        redirectAttrs.addFlashAttribute("mensajeExito", "Datos del apoderado actualizados correctamente.");
        return "redirect:/apoderados/editar/" + id;
    }

    @GetMapping("/parcial")
    public String listarApoderadosParcial(HttpSession session, Model model) {
        String rol = (String) session.getAttribute("rolUsuario");
        if (session.getAttribute("usuarioLogueado") == null || rol == null || (!rol.equals("ADMINISTRADOR") &&
                !rol.equals("DIRECCION_GENERAL") &&
                !rol.equals("SUBDIRECCION") &&
                !rol.equals("SECRETARIA") &&
                !rol.equals("DOCENTE"))) {
            return "";
        }

        model.addAttribute("apoderados", apoderadoService.listarTodos());
        return "apoderados/lista :: tablaApoderados";
    }
}
