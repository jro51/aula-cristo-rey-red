package com.virtual.aulacristo.controller;

import com.virtual.aulacristo.model.Beneficio;
import com.virtual.aulacristo.service.BeneficioService;
import com.virtual.aulacristo.service.EstudianteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/beneficios")
public class BeneficioController {

    @Autowired
    private BeneficioService beneficioService;

    @Autowired
    private EstudianteService estudianteService;

    // Vista principal: Listar y formulario de registro
    @GetMapping
    public String listarBeneficios(Model model, @RequestParam(required = false) Long estudianteId) {
        model.addAttribute("estudiantes", estudianteService.listarTodos());
        model.addAttribute("beneficio", new Beneficio());

        if (estudianteId != null) {
            model.addAttribute("beneficiosEstudiante", beneficioService.obtenerBeneficiosPorEstudiante(estudianteId));
            model.addAttribute("estudianteSeleccionadoId", estudianteId);
        } else {
            model.addAttribute("beneficiosEstudiante", beneficioService.listarTodos());
        }

        return "beneficios/gestion"; // Apunta a src/main/resources/templates/beneficios/gestion.html
    }

    // Registrar o actualizar un beneficio
    @Transactional
    @PostMapping("/guardar")
    public String guardarBeneficio(@RequestParam Long estudianteId,
                                   @RequestParam String tipo,
                                   @RequestParam Double valor,
                                   @RequestParam String anioPeriodo) {
        beneficioService.guardarBeneficio(estudianteId, tipo, valor, anioPeriodo);
        return "redirect:/beneficios";
    }

    // Eliminar beneficio
    @Transactional
    @GetMapping("/eliminar/{id}")
    public String eliminarBeneficio(@PathVariable Long id, @RequestParam(required = false) Long estudianteId) {
        beneficioService.eliminarBeneficio(id);
        return "redirect:/beneficios";
    }

    @GetMapping("/parcial")
    public String listarBeneficiosParcial(@RequestParam(required = false) Long estudianteId, Model model) {
        if (estudianteId != null) {
            model.addAttribute("beneficiosEstudiante", beneficioService.obtenerBeneficiosPorEstudiante(estudianteId));
            model.addAttribute("estudianteSeleccionadoId", estudianteId);
        } else {
            model.addAttribute("beneficiosEstudiante", beneficioService.listarTodos());
        }
        return "beneficios/gestion :: tablaBeneficiosContainer";
    }
}
