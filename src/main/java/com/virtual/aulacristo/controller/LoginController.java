package com.virtual.aulacristo.controller;

import com.virtual.aulacristo.entity.Usuario;
import com.virtual.aulacristo.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final UsuarioService usuarioService;

    public LoginController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String mostrarLogin() {
        // Retorna el nombre exacto de tu archivo HTML sin la extensión
        return "login"; 
    }

    //AVCR-56: Implementar autenticacion en backend
    @PostMapping("/login")
    public String procesarLogin(@RequestParam String username,
                                @RequestParam String password,
                                HttpSession session,
                                Model model) {
        try {
            Usuario usuario = usuarioService.autenticar(username, password);

            // AVCR-58: Implementar manejo de sesion/token
            // Guardamos el usuario completo o su rol en la sesión
            session.setAttribute("usuarioLogueado", usuario);
            session.setAttribute("rolUsuario", usuario.getRol().getNombre());

            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    // AVCR-64: Invalidar/Finalizar sesión del usuario
    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
