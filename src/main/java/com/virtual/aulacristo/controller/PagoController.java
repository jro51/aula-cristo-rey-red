package com.virtual.aulacristo.controller;

import com.virtual.aulacristo.model.Pago;
import com.virtual.aulacristo.service.EstudianteService;
import com.virtual.aulacristo.service.PagoService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

//AVCR-191: Crear formulario de registro de pago
@Controller
@RequestMapping("/pagos")
public class PagoController {

    @Autowired
    private PagoService pagoService;

    @Autowired
    private EstudianteService estudianteService;

    private boolean noEsPersonalTesoreria(HttpSession session) {
        String rol = (String) session.getAttribute("rolUsuario");
        return rol == null || (!rol.equals("ADMINISTRADOR") &&
                !rol.equals("DIRECCION_GENERAL") &&
                !rol.equals("TESORERIA"));
    }

    // Mostrar la vista con el formulario de registro y la tabla de pagos
    //AVCR-194: Implementar consulta de pagos por estudiante
    @GetMapping
    public String listarPagos(@RequestParam(value = "estudianteId", required = false) Long estudianteId,
                              @RequestParam(value = "anio", required = false) Integer anio,
                              HttpSession session,
                              Model model) {
        if (noEsPersonalTesoreria(session)) return "redirect:/?error=No autorizado";

        model.addAttribute("pagos", pagoService.listarPorEstudiante(estudianteId));
        model.addAttribute("estudiantes", estudianteService.listarTodos());
        model.addAttribute("nuevoPago", new Pago());
        model.addAttribute("estudianteSeleccionadoId", estudianteId);
        model.addAttribute("anioSeleccionado", anio != null ? anio : 2026);

        if (estudianteId != null) {
            model.addAttribute("estadoMensualidades", pagoService.obtenerEstadoMensualidadesPorAnio(estudianteId, anio));
        }

        return "pagos/pagos";
    }

    @GetMapping("/parcial")
    public String listarPagosParcial(@RequestParam(value = "estudianteId", required = false) Long estudianteId,
                                     @RequestParam(value = "anio", required = false) Integer anio,
                                     HttpSession session,
                                     Model model) {
        if (noEsPersonalTesoreria(session)) return "";

        model.addAttribute("pagos", pagoService.listarPorEstudiante(estudianteId));
        model.addAttribute("estudiantes", estudianteService.listarTodos());
        model.addAttribute("estudianteSeleccionadoId", estudianteId);
        model.addAttribute("anioSeleccionado", anio != null ? anio : 2026);

        if (estudianteId != null) {
            model.addAttribute("estadoMensualidades", pagoService.obtenerEstadoMensualidadesPorAnio(estudianteId, anio));
        }

        return "pagos/pagos :: pagosContainer";
    }

    // Registrar un nuevo pago
    @Transactional
    @PostMapping("/guardar")
    public String guardarPago(@Valid @ModelAttribute("nuevoPago") Pago pago,
                              BindingResult result,
                              @RequestParam("estudianteId") Long estudianteId,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        if (noEsPersonalTesoreria(session)) return "redirect:/?error=No autorizado";

        //AVCR-192: Validar datos obligatorios y montos
        if (result.hasErrors()) {
            model.addAttribute("pagos", pagoService.listarTodos());
            model.addAttribute("estudiantes", estudianteService.listarTodos());
            String mensajeErrorValidacion = result.getAllErrors().get(0).getDefaultMessage();
            model.addAttribute("mensajeError", "Validación: " + mensajeErrorValidacion);
            return "pagos/pagos";
        }

        try {
            pagoService.guardarPago(pago, estudianteId);
            redirectAttributes.addFlashAttribute("mensajeExito", "¡Pago registrado exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al registrar el pago: " + e.getMessage());
        }

        return "redirect:/pagos";
    }
}
