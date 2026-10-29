package com.virtual.aulacristo.controller;

import com.virtual.aulacristo.model.Docente;
import com.virtual.aulacristo.repository.DocenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profesores")
public class DocenteController {

    @Autowired
    private DocenteRepository docenteRepository;

    @GetMapping
    public String listarDocentes(Model model) {
        model.addAttribute("docentes", docenteRepository.findAll());
        return "profesores/lista";
    }

    @GetMapping("/parcial")
    public String listarDocentesParcial(Model model) {
        model.addAttribute("docentes", docenteRepository.findAll());
        return "profesores/lista :: tablaDocentesContainer";
    }

    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("docente", new Docente());
        return "profesores/crear";
    }

    @Transactional
    @PostMapping("/guardar")
    public String guardarDocente(@ModelAttribute Docente docente) {
        docenteRepository.save(docente);
        return "redirect:/profesores";
    }
}
