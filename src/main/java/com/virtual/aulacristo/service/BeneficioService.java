package com.virtual.aulacristo.service;

import com.virtual.aulacristo.model.Beneficio;
import com.virtual.aulacristo.model.Estudiante;
import com.virtual.aulacristo.repository.BeneficioRepository;
import com.virtual.aulacristo.repository.EstudianteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// AVCR-202: Lógica de negocio para registrar tipo, valor y período de aplicación
@Service
public class BeneficioService {

    @Autowired
    private BeneficioRepository beneficioRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    // Listar todos los beneficios registrados
    public List<Beneficio> listarTodos() {
        return beneficioRepository.findAll();
    }

    // Buscar beneficios por estudiante
    public List<Beneficio> obtenerBeneficiosPorEstudiante(Long estudianteId) {
        return beneficioRepository.findByEstudianteId(estudianteId);
    }

    // Registrar o actualizar un beneficio/descuento
    public Beneficio guardarBeneficio(Long estudianteId, String tipo, Double valor, String anioPeriodo) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con ID: " + estudianteId));

        // Siempre creamos un nuevo registro sin sobreescribir el anterior
        Beneficio beneficio = new Beneficio();
        beneficio.setEstudiante(estudiante);
        beneficio.setTipo(tipo);
        beneficio.setValor(valor);
        beneficio.setAnioPeriodo(anioPeriodo);

        return beneficioRepository.save(beneficio);
    }

    // Eliminar un beneficio
    public void eliminarBeneficio(Long id) {
        beneficioRepository.deleteById(id);
    }
}
