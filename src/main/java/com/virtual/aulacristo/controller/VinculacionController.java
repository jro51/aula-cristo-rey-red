package com.virtual.aulacristo.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.virtual.aulacristo.repository.ApoderadoRepository;
import com.virtual.aulacristo.service.EstudianteService;

@Controller
@RequestMapping("/vinculaciones")
public class VinculacionController {

    @Autowired
    private EstudianteService estudianteService;

    @Autowired
    private ApoderadoRepository apoderadoRepository;

    private boolean noEsPersonalAutorizado(HttpSession session) {
        String rol = (String) session.getAttribute("rolUsuario");
        return rol == null || (!rol.equals("ADMINISTRADOR") &&
                !rol.equals("DIRECCION_GENERAL") &&
                !rol.equals("SUBDIRECCION") &&
                !rol.equals("SECRETARIA"));
    }

    @GetMapping("/nueva")
    public String mostrarFormularioVinculacion(HttpSession session, Model model) {
        if (noEsPersonalAutorizado(session)) return "redirect:/?error=No autorizado";
        // AVCR-119 & AVCR-120: Enviamos las listas para los selectores
        model.addAttribute("estudiantes", estudianteService.listarTodos());
        model.addAttribute("apoderados", apoderadoRepository.findAll());
        return "vinculaciones/nueva";
    }

    @Transactional
    @PostMapping("/nueva")
    public String procesarVinculacion(@RequestParam Long estudianteId, 
                                      @RequestParam Long apoderadoId,
                                      HttpSession session,
                                      RedirectAttributes redirectAttrs) {
        if (noEsPersonalAutorizado(session)) return "redirect:/?error=No autorizado";

        try {
            estudianteService.vincularApoderado(estudianteId, apoderadoId);
            redirectAttrs.addFlashAttribute("mensajeExito", "Vínculo registrado correctamente.");
        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("errorVinculo", e.getMessage());
        }
        return "redirect:/vinculaciones/nueva";
    }
}
