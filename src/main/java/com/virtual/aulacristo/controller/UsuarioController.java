package com.virtual.aulacristo.controller;

import com.virtual.aulacristo.entity.Rol;
import com.virtual.aulacristo.entity.Usuario;
import com.virtual.aulacristo.repository.RolRepository;
import com.virtual.aulacristo.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final RolRepository rolRepository;

    public UsuarioController(UsuarioService usuarioService, RolRepository rolRepository) {
        this.usuarioService = usuarioService;
        this.rolRepository = rolRepository;
    }

    private boolean noEsAdmin(HttpSession session) {
        String rol = (String) session.getAttribute("rolUsuario");
        return !"ADMINISTRADOR".equals(rol);
    }

    @GetMapping
    public String listarUsuarios(HttpSession session, Model model) {
        if (noEsAdmin(session)) return "redirect:/?error=No autorizado";

        model.addAttribute("usuarios", usuarioService.listarUsuarios());
        return "usuarios/lista";
    }

    @GetMapping("/parcial")
    public String listarUsuariosParcial(HttpSession session, Model model) {
        if (noEsAdmin(session)) return "";
        model.addAttribute("usuarios", usuarioService.listarUsuarios());
        return "usuarios/lista :: tablaUsuariosContainer";
    }

    @GetMapping("/nuevo")
    public String nuevoUsuario(HttpSession session, Model model) {
        if (noEsAdmin(session)) return "redirect:/?error=No autorizado";

        Usuario usuario = new Usuario();
        usuario.setRol(new Rol()); // Se inicializa el Rol para evitar NullPointerException en Thymeleaf

        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", rolRepository.findAll());
        return "usuarios/formulario";
    }

    @Transactional
    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute("usuario") Usuario usuario, HttpSession session, RedirectAttributes redirectAttributes) {
        if (noEsAdmin(session)) return "redirect:/?error=No autorizado";

        try {
            usuarioService.registrarUsuario(usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario registrado exitosamente.");
            return "redirect:/usuarios";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/usuarios/nuevo";
        }
    }

    @GetMapping("/editar/{id}")
    public String editarUsuario(@PathVariable("id") Long id,HttpSession session,  Model model, RedirectAttributes redirectAttributes) {
        if (noEsAdmin(session)) return "redirect:/?error=No autorizado";

        return usuarioService.buscarPorId(id)
                .map(u -> {
                    model.addAttribute("usuario", u);
                    model.addAttribute("roles", rolRepository.findAll());
                    return "usuarios/formulario";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("mensajeError", "El usuario solicitado no existe.");
                    return "redirect:/usuarios";
                });
    }

    @Transactional
    @PostMapping("/actualizar/{id}")
    public String actualizarUsuario(@PathVariable("id") Long id, HttpSession session, @ModelAttribute("usuario") Usuario usuario,
            RedirectAttributes redirectAttributes) {
        if (noEsAdmin(session)) return "redirect:/?error=No autorizado";

        try {
            usuarioService.actualizarUsuario(id, usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario actualizado correctamente.");
            return "redirect:/usuarios";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/usuarios/editar/" + id;
        }
    }

    @Transactional
    @GetMapping("/estado/{id}/{nuevoEstado}")
    public String cambiarEstado(@PathVariable("id") Long id, HttpSession session, @PathVariable("nuevoEstado") boolean nuevoEstado,
            RedirectAttributes redirectAttributes) {
        if (noEsAdmin(session)) return "redirect:/?error=No autorizado";

        try {
            usuarioService.cambiarEstado(id, nuevoEstado);
            redirectAttributes.addFlashAttribute("mensajeExito", "Estado actualizado con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "No se pudo cambiar el estado.");
        }
        return "redirect:/usuarios";
    }
}