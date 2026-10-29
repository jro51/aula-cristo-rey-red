package com.virtual.aulacristo.controller;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.virtual.aulacristo.dto.MatriculaUpdateDto;
import com.virtual.aulacristo.model.Prematricula;
import com.virtual.aulacristo.service.DocumentoService;
import com.virtual.aulacristo.service.EstudianteService;
import com.virtual.aulacristo.service.PrematriculaService;
import com.virtual.aulacristo.model.EstadoPrematricula;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/prematriculas")
public class PrematriculaController {

    @Autowired
    private PrematriculaService prematriculaService;

    @Autowired
    private EstudianteService estudianteService;

    @Autowired
    private DocumentoService documentoService;

    private boolean noEsPersonalAutorizado(HttpSession session) {
        String rol = (String) session.getAttribute("rolUsuario");
        return rol == null || (!rol.equals("ADMINISTRADOR") &&
                !rol.equals("DIRECCION_GENERAL") &&
                !rol.equals("SUBDIRECCION") &&
                !rol.equals("SECRETARIA"));
    }

    @GetMapping("/nueva")
    public String mostrarFormulario(HttpSession session, Model model) {
        if (noEsPersonalAutorizado(session)) return "redirect:/?error=No autorizado";
        
        model.addAttribute("estudiantes", estudianteService.listarTodos());
        return "prematriculas/nueva";
    }

    @Transactional
    @PostMapping("/nueva")
    public String registrarPrematricula(@RequestParam Long estudianteId, 
                                        @RequestParam String anioAcademico, 
                                        HttpSession session,
                                        RedirectAttributes redirectAttrs) {
        if (noEsPersonalAutorizado(session)) return "redirect:/?error=No autorizado";

        try {
            var solicitud = prematriculaService.generarSolicitud(estudianteId, anioAcademico);
            redirectAttrs.addFlashAttribute("mensajeExito", "Solicitud generada con éxito. Por favor, adjunte los documentos requeridos.");
            
            // EL CAMBIO CLAVE: Redirigimos al paso 2 (subir documentos)
            return "redirect:/prematriculas/" + solicitud.getId() + "/documentos";
            
        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/prematriculas/nueva";
        }
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id, HttpSession session, Model model) {
        if (noEsPersonalAutorizado(session)) return "redirect:/?error=No autorizado";

        Prematricula solicitud = prematriculaService.obtenerSolicitudPorId(id);
        
        MatriculaUpdateDto dto = new MatriculaUpdateDto();
        dto.setAnioAcademico(solicitud.getAnioAcademico());
        dto.setEstudianteGrado(solicitud.getEstudiante().getGrado());
        dto.setEstudianteCorreo(solicitud.getEstudiante().getCorreo());
        dto.setEstudianteTelefono(solicitud.getEstudiante().getTelefono());
        dto.setApoderadoCorreo(solicitud.getApoderado().getEmail()); // Usando setEmail como corregimos
        dto.setApoderadoTelefono(solicitud.getApoderado().getTelefono());

        model.addAttribute("solicitudInfo", solicitud);
        model.addAttribute("matriculaDto", dto);
        return "prematriculas/editar";
    }

    @Transactional
    @PostMapping("/editar/{id}")
    public String procesarEdicion(@PathVariable Long id, 
                                  @Valid @ModelAttribute("matriculaDto") MatriculaUpdateDto dto, 
                                  BindingResult result, 
                                  HttpSession session,
                                  RedirectAttributes redirectAttrs,
                                  Model model) {
        if (noEsPersonalAutorizado(session)) return "redirect:/?error=No autorizado";

        if (result.hasErrors()) {
            model.addAttribute("solicitudInfo", prematriculaService.obtenerSolicitudPorId(id));
            return "prematriculas/editar";
        }

        try {
            prematriculaService.actualizarDatosMatricula(id, dto);
            redirectAttrs.addFlashAttribute("mensajeExito", "Los datos de contacto y matrícula han sido actualizados.");
        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("mensajeError", e.getMessage());
        }

        return "redirect:/prematriculas/editar/" + id;
    }
    // AVCR-176: Interfaz para subir y visualizar documentos
    @GetMapping("/{id}/documentos")
    public String mostrarGestorDocumentos(@PathVariable Long id, HttpSession session, Model model) {
        if (noEsPersonalAutorizado(session)) return "redirect:/?error=No autorizado";

        Prematricula solicitud = prematriculaService.obtenerSolicitudPorId(id);
        
        model.addAttribute("solicitud", solicitud);
        model.addAttribute("documentos", documentoService.listarDocumentosPorSolicitud(id));
        
        // Lista genérica de documentos sugeridos
        model.addAttribute("tiposSugeridos", java.util.Arrays.asList(
            "DNI del Estudiante", "DNI del Apoderado", "Libreta de Notas", 
            "Certificado de Estudios", "Recibo de Servicios", "Otro"
        ));

        return "prematriculas/documentos";
    }

    @GetMapping("/{id}/documentos/parcial")
    public String mostrarGestorDocumentosParcial(@PathVariable Long id, HttpSession session, Model model) {
        if (noEsPersonalAutorizado(session)) return "";
        model.addAttribute("documentos", documentoService.listarDocumentosPorSolicitud(id));
        return "prematriculas/documentos :: listaDocumentosContainer";
    }

    // AVCR-174: Procesar la carga
    @Transactional
    @PostMapping("/{id}/documentos")
    public String procesarSubidaDocumento(@PathVariable Long id,
                                          @RequestParam("tipoDocumento") String tipoDocumento,
                                          @RequestParam("tipoDocumentoOtro") String tipoDocumentoOtro,
                                          @RequestParam("archivo") MultipartFile archivo,
                                          HttpSession session,
                                          RedirectAttributes redirectAttrs) {
        if (noEsPersonalAutorizado(session)) return "redirect:/?error=No autorizado";

        // Lógica genérica: si elige "Otro", usamos el texto que haya escrito manualmente
        String tipoFinal = tipoDocumento.equals("Otro") ? tipoDocumentoOtro : tipoDocumento;

        try {
            documentoService.subirDocumento(id, tipoFinal, archivo);
            redirectAttrs.addFlashAttribute("mensajeExito", "El documento '" + tipoFinal + "' se adjuntó correctamente.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("mensajeError", e.getMessage());
        }

        return "redirect:/prematriculas/" + id + "/documentos";
    }
    // AVCR-180 y AVCR-181: Listado y Filtros
    @GetMapping("/lista")
    public String listarSolicitudes(@RequestParam(required = false) String estado, HttpSession session, Model model) {
        if (noEsPersonalAutorizado(session)) return "redirect:/?error=No autorizado";
        
        List<Prematricula> lista;
        if (estado != null && !estado.isEmpty()) {
            lista = prematriculaService.listarPorEstado(EstadoPrematricula.valueOf(estado));
        } else {
            lista = prematriculaService.listarTodas();
        }
        
        model.addAttribute("solicitudes", lista);
        model.addAttribute("estadoSeleccionado", estado);
        return "prematriculas/lista";
    }

    @GetMapping("/lista/parcial")
    public String listarSolicitudesParcial(@RequestParam(required = false) String estado, HttpSession session, Model model) {
        if (noEsPersonalAutorizado(session)) return "";

        List<Prematricula> lista;
        if (estado != null && !estado.isEmpty()) {
            lista = prematriculaService.listarPorEstado(EstadoPrematricula.valueOf(estado));
        } else {
            lista = prematriculaService.listarTodas();
        }

        model.addAttribute("solicitudes", lista);
        model.addAttribute("estadoSeleccionado", estado);
        return "prematriculas/lista :: tablaSolicitudesContainer";
    }

    // AVCR-182 y AVCR-183: Vista de Detalle
    @GetMapping("/revisar/{id}")
    public String mostrarRevision(@PathVariable Long id, HttpSession session, Model model) {
        if (noEsPersonalAutorizado(session)) return "redirect:/?error=No autorizado";
        
        model.addAttribute("solicitud", prematriculaService.obtenerSolicitudPorId(id));
        model.addAttribute("documentos", documentoService.listarDocumentosPorSolicitud(id));
        return "prematriculas/revisar";
    }

    // AVCR-184, AVCR-185, AVCR-186: Procesar Evaluación
    @Transactional
    @PostMapping("/revisar/{id}")
    public String procesarRevision(@PathVariable Long id, 
                                   @RequestParam String accion, 
                                   @RequestParam(required = false) String motivo, 
                                   HttpSession session, 
                                   RedirectAttributes redirectAttrs) {
        if (noEsPersonalAutorizado(session)) return "redirect:/?error=No autorizado";

        try {
            if ("APROBAR".equals(accion)) {
                prematriculaService.evaluarSolicitud(id, EstadoPrematricula.APROBADA, null);
                redirectAttrs.addFlashAttribute("mensajeExito", "Solicitud APROBADA exitosamente. El estudiante ya puede matricularse.");
            } else if ("OBSERVAR".equals(accion)) {
                prematriculaService.evaluarSolicitud(id, EstadoPrematricula.OBSERVADA, motivo);
                redirectAttrs.addFlashAttribute("mensajeWarning", "Solicitud OBSERVADA. El apoderado deberá corregir los documentos.");
            }
        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/prematriculas/revisar/" + id;
        }

        return "redirect:/prematriculas/lista";
    }
}