package com.virtual.aulacristo.service;

import java.time.Year;
import java.util.UUID;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.virtual.aulacristo.dto.MatriculaUpdateDto;
import com.virtual.aulacristo.model.Apoderado;
import com.virtual.aulacristo.model.EstadoPrematricula;
import com.virtual.aulacristo.model.Estudiante;
import com.virtual.aulacristo.model.Prematricula;
import com.virtual.aulacristo.repository.PrematriculaRepository;

@Service
public class PrematriculaService {

    @Autowired
    private PrematriculaRepository prematriculaRepository;
    
    @Autowired
    private EstudianteService estudianteService;

    public Prematricula obtenerSolicitudPorId(Long id) {
        return prematriculaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada."));
    }

    @Transactional
    public void actualizarDatosMatricula(Long id, MatriculaUpdateDto dto) {
        Prematricula prematricula = obtenerSolicitudPorId(id);

        if (prematricula.getEstado() != EstadoPrematricula.PENDIENTE && prematricula.getEstado() != EstadoPrematricula.OBSERVADA) {
            throw new IllegalArgumentException("No se pueden modificar datos en una solicitud que ya está " + prematricula.getEstado());
        }

        // 1. Actualizamos datos de la Solicitud
        prematricula.setAnioAcademico(dto.getAnioAcademico());

        // 2. Actualizamos datos del Estudiante
        Estudiante estudiante = prematricula.getEstudiante();
        estudiante.setGrado(dto.getEstudianteGrado());
        estudiante.setCorreo(dto.getEstudianteCorreo());
        estudiante.setTelefono(dto.getEstudianteTelefono());

        // 3. Actualizamos datos del Apoderado
        Apoderado apoderado = prematricula.getApoderado();
        apoderado.setEmail(dto.getApoderadoCorreo());
        apoderado.setTelefono(dto.getApoderadoTelefono());

        // Hibernate sincronizará los cambios automáticamente al terminar el método gracias a @Transactional
    }

    public Prematricula generarSolicitud(Long estudianteId, String anioAcademico) {
        // Validar que el estudiante no tenga ya una solicitud para este año
        if (prematriculaRepository.existsByEstudianteIdAndAnioAcademico(estudianteId, anioAcademico)) {
            throw new IllegalArgumentException("El estudiante ya tiene una solicitud de prematrícula en curso para el año " + anioAcademico);
        }

        Estudiante estudiante = estudianteService.obtenerEstudiantePorId(estudianteId);
        
        if (estudiante.getApoderado() == null) {
            throw new IllegalArgumentException("El estudiante debe tener un apoderado vinculado antes de iniciar la matrícula.");
        }

        Prematricula solicitud = new Prematricula();
        solicitud.setEstudiante(estudiante);
        solicitud.setApoderado(estudiante.getApoderado()); // Auto-asigna el apoderado vinculado
        solicitud.setAnioAcademico(anioAcademico);
        
        // AVCR-170: Generar código  único de solicitud (Ej: MAT-2026-A1B2C)
        String codigoGenerado = "MAT-" + Year.now().getValue() + "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        solicitud.setCodigoSolicitud(codigoGenerado);

        return prematriculaRepository.save(solicitud);
    }
    public List<Prematricula> listarTodas() {
        return prematriculaRepository.findAll();
    }

    public List<Prematricula> listarPorEstado(EstadoPrematricula estado) {
        return prematriculaRepository.findByEstado(estado);
    }

    @Transactional
    public void evaluarSolicitud(Long id, EstadoPrematricula nuevoEstado, String motivo) {
        Prematricula solicitud = obtenerSolicitudPorId(id);
        solicitud.setEstado(nuevoEstado);
        
        if (nuevoEstado == EstadoPrematricula.OBSERVADA) {
            if (motivo == null || motivo.trim().isEmpty()) {
                throw new IllegalArgumentException("Debe especificar un motivo para observar la solicitud.");
            }
            solicitud.setMotivoObservacion(motivo);
        } else {
            solicitud.setMotivoObservacion(null);
        }
    }
}
