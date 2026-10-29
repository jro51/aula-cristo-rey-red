package com.virtual.aulacristo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.virtual.aulacristo.model.Estudiante;
import com.virtual.aulacristo.service.EstudianteService;
import com.virtual.aulacristo.service.ReporteExportService;

import jakarta.servlet.http.HttpSession;

// AVCR-51: Controlador para el reporte de estudiantes
@Controller
@RequestMapping("/reportes")
public class ReporteController {

    @Autowired
    private EstudianteService estudianteService;

    @Autowired
    private ReporteExportService reporteExportService;

    // AVCR-206 y AVCR-207: Consulta general con filtros
    @Transactional(readOnly = true)
    @GetMapping("/estudiantes")
    public String mostrarReporteEstudiantes(
            @RequestParam(required = false) String nivel,
            @RequestParam(required = false) String grado,
            @RequestParam(required = false) String seccion,
            HttpSession session,
            Model model) {

        // Verificar si inició sesión
        if (session.getAttribute("usuarioLogueado") == null) {
            return "redirect:/login";
        }

        // Verificar el rol
        if (!puedeAccederReportes(session)) {
            return "redirect:/?error=No autorizado";
        }

        // AVCR-206: Consultar estudiantes aplicando los filtros
        List<Estudiante> estudiantes = estudianteService.obtenerReporte(
                nivel,
                grado,
                seccion);

        model.addAttribute("estudiantes", estudiantes);

        // AVCR-209: Mostrar total de estudiantes según filtros
        model.addAttribute("totalEstudiantes", estudiantes.size());

        // Persistir los filtros en la vista
        model.addAttribute("nivelSeleccionado", nivel);
        model.addAttribute("gradoSeleccionado", grado);
        model.addAttribute("seccionSeleccionada", seccion);

        return "reportes/reporte";
    }

    @Transactional(readOnly = true)
    @GetMapping("/estudiantes/parcial")
    public String mostrarReporteEstudiantesParcial(
            @RequestParam(required = false) String nivel,
            @RequestParam(required = false) String grado,
            @RequestParam(required = false) String seccion,
            HttpSession session,
            Model model) {

        if (session.getAttribute("usuarioLogueado") == null || !puedeAccederReportes(session)) {
            return "";
        }

        List<Estudiante> estudiantes = estudianteService.obtenerReporte(
                nivel,
                grado,
                seccion);

        model.addAttribute("estudiantes", estudiantes);
        model.addAttribute("totalEstudiantes", estudiantes.size());

        return "reportes/reporte :: tablaReporteContainer";
    }

    // AVCR-210: Exportar reporte a Excel (solo ADMINISTRADOR)
    @Transactional(readOnly = true)
    @GetMapping("/estudiantes/excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) String nivel,
            @RequestParam(required = false) String grado,
            @RequestParam(required = false) String seccion,
            HttpSession session) {

        // Verificar sesión
        if (session.getAttribute("usuarioLogueado") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Solo ADMINISTRADOR puede exportar
        if (!puedeExportarReportes(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Estudiante> estudiantes = estudianteService.obtenerReporte(
                nivel, grado, seccion);

        byte[] archivo = reporteExportService.generarExcel(estudiantes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=reporte_estudiantes.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument"
                                + ".spreadsheetml.sheet"))
                .body(archivo);
    }

    // AVCR-210: Exportar reporte a PDF (solo ADMINISTRADOR)
    @Transactional(readOnly = true)
    @GetMapping("/estudiantes/pdf")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam(required = false) String nivel,
            @RequestParam(required = false) String grado,
            @RequestParam(required = false) String seccion,
            HttpSession session) {

        // Verificar sesión
        if (session.getAttribute("usuarioLogueado") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Solo ADMINISTRADOR puede exportar
        if (!puedeExportarReportes(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Estudiante> estudiantes = estudianteService.obtenerReporte(
                nivel, grado, seccion);

        byte[] archivo = reporteExportService.generarPdf(estudiantes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=reporte_estudiantes.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(archivo);
    }

    // Validar si el usuario puede ver el reporte
    private boolean puedeAccederReportes(HttpSession session) {

        String rol = (String) session.getAttribute("rolUsuario");

        return "ADMINISTRADOR".equals(rol) ||
                "DIRECCION_GENERAL".equals(rol) ||
                "SECRETARIA".equals(rol);
    }

    // AVCR-210: Validar si el usuario puede exportar (solo ADMINISTRADOR)
    private boolean puedeExportarReportes(HttpSession session) {

        String rol = (String) session.getAttribute("rolUsuario");

        return "ADMINISTRADOR".equals(rol) ||
                "DIRECCION_GENERAL".equals(rol) ||
                "SECRETARIA".equals(rol);
    }
}
