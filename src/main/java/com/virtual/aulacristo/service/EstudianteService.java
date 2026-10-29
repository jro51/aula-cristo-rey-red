package com.virtual.aulacristo.service;

import java.time.Year;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.virtual.aulacristo.model.Apoderado;
import com.virtual.aulacristo.model.Estudiante;
import com.virtual.aulacristo.repository.ApoderadoRepository;
import com.virtual.aulacristo.repository.EstudianteRepository;

@Service
public class EstudianteService {

    @Autowired
    private EstudianteRepository estudianteRepository;

    public void registrarEstudiante(Estudiante estudiante) {

        // AVCR-90: Validar duplicado por DNI
        if (estudianteRepository.existsByDni(estudiante.getDni())) {
            throw new IllegalArgumentException(
                    "Ya existe un estudiante registrado con el DNI: "
                            + estudiante.getDni());
        }

        // AVCR-91: Generar código del estudiante
        String codigoGenerado = "EST-" + Year.now().getValue() + "-" + estudiante.getDni();

        estudiante.setCodigo(codigoGenerado);
        estudianteRepository.save(estudiante);
    }

    // AVCR-93: Buscar estudiante por código o DNI
    public Estudiante buscarPorCodigoODni(String criterio) {

        // Validamos que se haya escrito algo
        if (criterio == null || criterio.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe ingresar el código o DNI del estudiante.");
        }

        // Quitamos espacios innecesarios
        String valorBusqueda = criterio.trim();

        // Buscamos el mismo valor como código y como DNI
        return estudianteRepository
                .findByCodigoIgnoreCaseOrDni(
                        valorBusqueda,
                        valorBusqueda)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró ningún estudiante con el código o DNI: "
                                + valorBusqueda));
    }

    // AVCR-103: Cargar información actual del estudiante
    public Estudiante obtenerEstudiantePorId(Long id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));
    }

    // AVCR-106: Guardar los cambios
    public void actualizarEstudiante(Long id, Estudiante estudianteActualizado) {
        Estudiante estudianteExistente = obtenerEstudiantePorId(id);

        // AVCR-105: Evitar modificación del identificador principal
        // Mapeamos solo los campos autorizados. El 'codigo' y el 'dni' se quedan
        // intactos.
        estudianteExistente.setNombres(estudianteActualizado.getNombres());
        estudianteExistente.setApellidos(estudianteActualizado.getApellidos());
        estudianteExistente.setCorreo(estudianteActualizado.getCorreo());
        estudianteExistente.setFechaNacimiento(estudianteActualizado.getFechaNacimiento());
        estudianteExistente.setTelefono(estudianteActualizado.getTelefono());
        estudianteExistente.setGrado(estudianteActualizado.getGrado());
        estudianteExistente.setSeccion(estudianteActualizado.getSeccion());

        estudianteRepository.save(estudianteExistente);
    }

    @Autowired
    private ApoderadoRepository apoderadoRepository;

    public void vincularApoderado(Long estudianteId, Long apoderadoId) {
        Estudiante estudiante = obtenerEstudiantePorId(estudianteId);
        Apoderado apoderado = apoderadoRepository.findById(apoderadoId)
                .orElseThrow(() -> new IllegalArgumentException("Apoderado no encontrado"));

        // AVCR-124: Validar relaciones duplicadas
        if (estudiante.getApoderado() != null && estudiante.getApoderado().getId().equals(apoderadoId)) {
            throw new IllegalArgumentException("El estudiante ya está vinculado a este apoderado.");
        }

        // AVCR-121 & AVCR-123: Registra o actualiza la relación
        estudiante.setApoderado(apoderado);
        estudianteRepository.save(estudiante);
    }

    // Método auxiliar para llenar el selector de la vista
    public java.util.List<Estudiante> listarTodos() {
        return estudianteRepository.findAll();
    }

    // AVCR-98: Filtrar estudiantes
    public List<Estudiante> filtrarEstudiantes(
            String texto,
            String grado,
            String seccion,
            Boolean activo) {

        String textoLimpio = texto == null ? null : texto.trim();

        return estudianteRepository.buscarConFiltros(
                textoLimpio,
                grado,
                seccion,
                activo);
    }

    // AVCR-206: Obtener estudiantes para el reporte
    public List<Estudiante> obtenerReporte(
            String nivel,
            String grado,
            String seccion) {

        return estudianteRepository.buscarParaReporte(
                nivel,
                grado,
                seccion);
    }
}