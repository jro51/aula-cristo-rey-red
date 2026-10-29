package com.virtual.aulacristo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.virtual.aulacristo.model.AsignacionAcademicaEstudiante;
import com.virtual.aulacristo.model.Estudiante;
import com.virtual.aulacristo.model.Grado;
import com.virtual.aulacristo.model.PeriodoAcademico;
import com.virtual.aulacristo.model.Seccion;
import com.virtual.aulacristo.repository.AsignacionAcademicaEstudianteRepository;
import com.virtual.aulacristo.repository.EstudianteRepository;
import com.virtual.aulacristo.repository.GradoRepository;
import com.virtual.aulacristo.repository.PeriodoAcademicoRepository;
import com.virtual.aulacristo.repository.SeccionRepository;

@Service
public class AsignacionAcademicaEstudianteService {

    private final AsignacionAcademicaEstudianteRepository
            asignacionRepository;

    private final EstudianteRepository estudianteRepository;
    private final GradoRepository gradoRepository;
    private final SeccionRepository seccionRepository;
    private final PeriodoAcademicoRepository periodoRepository;

    public AsignacionAcademicaEstudianteService(
            AsignacionAcademicaEstudianteRepository asignacionRepository,
            EstudianteRepository estudianteRepository,
            GradoRepository gradoRepository,
            SeccionRepository seccionRepository,
            PeriodoAcademicoRepository periodoRepository) {

        this.asignacionRepository = asignacionRepository;
        this.estudianteRepository = estudianteRepository;
        this.gradoRepository = gradoRepository;
        this.seccionRepository = seccionRepository;
        this.periodoRepository = periodoRepository;
    }

    // AVCR-155: Asignar o reasignar un estudiante
    @Transactional
    public AsignacionAcademicaEstudiante asignarOReasignar(
            Long estudianteId,
            Long gradoId,
            Long seccionId) {

        validarIdsObligatorios(
            estudianteId,
            gradoId,
            seccionId
        );

        Estudiante estudiante =
                buscarEstudianteActivo(estudianteId);

        Grado grado =
                buscarGradoActivo(gradoId);

        Seccion seccion =
                buscarSeccionActiva(seccionId);

        PeriodoAcademico periodoVigente =
                obtenerPeriodoVigente();

        validarSeccionPerteneceAlGrado(
            seccion,
            grado
        );

        // AVCR-158: Evitar exactamente la misma asignación
        validarAsignacionDuplicada(
            estudianteId,
            gradoId,
            seccionId,
            periodoVigente.getId()
        );

        Optional<AsignacionAcademicaEstudiante>
                asignacionExistente =
                    asignacionRepository
                        .findByEstudiante_IdAndPeriodoAcademico_Id(
                            estudianteId,
                            periodoVigente.getId()
                        );

        AsignacionAcademicaEstudiante asignacion;

        if (asignacionExistente.isPresent()) {

            // Reasignar modificando el registro existente
            asignacion = asignacionExistente.get();

            asignacion.setGrado(grado);
            asignacion.setSeccion(seccion);
            asignacion.setActivo(true);
            asignacion.setFechaAsignacion(LocalDate.now());

        } else {

            // Registrar la primera asignación del período
            asignacion =
                    new AsignacionAcademicaEstudiante();

            asignacion.setEstudiante(estudiante);
            asignacion.setGrado(grado);
            asignacion.setSeccion(seccion);
            asignacion.setPeriodoAcademico(periodoVigente);
            asignacion.setFechaAsignacion(LocalDate.now());
            asignacion.setActivo(true);
        }

        // Mantener compatibles los campos antiguos
        actualizarDatosAcademicosDelEstudiante(
            estudiante,
            grado,
            seccion
        );

        estudianteRepository.save(estudiante);

        return asignacionRepository.save(asignacion);
    }

    // AVCR-157: Listar estudiantes por grado y sección
    @Transactional(readOnly = true)
    public List<AsignacionAcademicaEstudiante>
            listarPorGradoYSeccion(
                Long gradoId,
                Long seccionId) {

        if (gradoId == null) {
            throw new IllegalArgumentException(
                "Debe seleccionar un grado."
            );
        }

        if (seccionId == null) {
            throw new IllegalArgumentException(
                "Debe seleccionar una sección."
            );
        }

        Grado grado =
                buscarGradoActivo(gradoId);

        Seccion seccion =
                buscarSeccionActiva(seccionId);

        validarSeccionPerteneceAlGrado(
            seccion,
            grado
        );

        PeriodoAcademico periodoVigente =
                obtenerPeriodoVigente();

        return asignacionRepository
                .findByGrado_IdAndSeccion_IdAndPeriodoAcademico_IdAndActivoTrueOrderByEstudiante_ApellidosAscEstudiante_NombresAsc(
                    gradoId,
                    seccionId,
                    periodoVigente.getId()
                );
    }

    // Obtener la asignación vigente de un estudiante
    @Transactional(readOnly = true)
    public Optional<AsignacionAcademicaEstudiante>
            buscarAsignacionVigenteDelEstudiante(
                Long estudianteId) {

        if (estudianteId == null) {
            throw new IllegalArgumentException(
                "Debe indicar el estudiante."
            );
        }

        PeriodoAcademico periodoVigente =
                obtenerPeriodoVigente();

        return asignacionRepository
                .findByEstudiante_IdAndPeriodoAcademico_Id(
                    estudianteId,
                    periodoVigente.getId()
                );
    }

    // Listar todas las asignaciones
    @Transactional(readOnly = true)
    public List<AsignacionAcademicaEstudiante>
            listarTodas() {

        return asignacionRepository
                .findAllByOrderByFechaAsignacionDesc();
    }

    // Listar asignaciones activas del período vigente
    @Transactional(readOnly = true)
    public List<AsignacionAcademicaEstudiante>
            listarAsignacionesVigentes() {

        PeriodoAcademico periodoVigente =
                obtenerPeriodoVigente();

        return asignacionRepository
                .findByPeriodoAcademico_IdAndActivoTrueOrderByEstudiante_ApellidosAscEstudiante_NombresAsc(
                    periodoVigente.getId()
                );
    }

    // Buscar el único período académico vigente
    @Transactional(readOnly = true)
    public PeriodoAcademico obtenerPeriodoVigente() {

        List<PeriodoAcademico> periodosVigentes =
                periodoRepository.findByVigenteTrue();

        if (periodosVigentes.isEmpty()) {
            throw new IllegalArgumentException(
                "No existe un período académico vigente."
            );
        }

        if (periodosVigentes.size() > 1) {
            throw new IllegalArgumentException(
                "Existe más de un período académico vigente. "
                + "Debe dejar solamente uno activo."
            );
        }

        PeriodoAcademico periodoVigente =
                periodosVigentes.get(0);

        if (!periodoVigente.isActivo()) {
            throw new IllegalArgumentException(
                "El período académico vigente está inactivo."
            );
        }

        return periodoVigente;
    }

    // Activar o desactivar una asignación
    @Transactional
    public AsignacionAcademicaEstudiante cambiarEstado(
            Long asignacionId,
            boolean activo) {

        AsignacionAcademicaEstudiante asignacion =
                buscarPorId(asignacionId);

        asignacion.setActivo(activo);

        return asignacionRepository.save(asignacion);
    }

    // Buscar asignación por ID
    @Transactional(readOnly = true)
    public AsignacionAcademicaEstudiante buscarPorId(
            Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                "Debe indicar una asignación académica."
            );
        }

        return asignacionRepository.findById(id)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "La asignación académica no existe."
                    )
                );
    }

    private Estudiante buscarEstudianteActivo(
            Long estudianteId) {

        Estudiante estudiante =
                estudianteRepository.findById(estudianteId)
                    .orElseThrow(() ->
                        new IllegalArgumentException(
                            "El estudiante seleccionado no existe."
                        )
                    );

        if (!estudiante.isActivo()) {
            throw new IllegalArgumentException(
                "El estudiante seleccionado está inactivo."
            );
        }

        return estudiante;
    }

    private Grado buscarGradoActivo(
            Long gradoId) {

        Grado grado =
                gradoRepository.findById(gradoId)
                    .orElseThrow(() ->
                        new IllegalArgumentException(
                            "El grado seleccionado no existe."
                        )
                    );

        if (!grado.isActivo()) {
            throw new IllegalArgumentException(
                "El grado seleccionado está inactivo."
            );
        }

        if (grado.getNivel() == null) {
            throw new IllegalArgumentException(
                "El grado seleccionado no tiene un nivel asignado."
            );
        }

        if (!grado.getNivel().isActivo()) {
            throw new IllegalArgumentException(
                "El nivel del grado seleccionado está inactivo."
            );
        }

        return grado;
    }

    private Seccion buscarSeccionActiva(
            Long seccionId) {

        Seccion seccion =
                seccionRepository.findById(seccionId)
                    .orElseThrow(() ->
                        new IllegalArgumentException(
                            "La sección seleccionada no existe."
                        )
                    );

        if (!seccion.isActivo()) {
            throw new IllegalArgumentException(
                "La sección seleccionada está inactiva."
            );
        }

        return seccion;
    }

    private void validarSeccionPerteneceAlGrado(
            Seccion seccion,
            Grado grado) {

        if (seccion.getGrado() == null
                || seccion.getGrado().getId() == null
                || !seccion.getGrado().getId().equals(
                    grado.getId()
                )) {

            throw new IllegalArgumentException(
                "La sección seleccionada no pertenece "
                + "al grado indicado."
            );
        }
    }

    private void actualizarDatosAcademicosDelEstudiante(
            Estudiante estudiante,
            Grado grado,
            Seccion seccion) {

        String nombreNivel =
                grado.getNivel().getNombre();

        String gradoCompleto =
                grado.getNombre() + " - " + nombreNivel;

        estudiante.setGrado(gradoCompleto);
        estudiante.setSeccion(seccion.getNombre());
    }

    private void validarIdsObligatorios(
            Long estudianteId,
            Long gradoId,
            Long seccionId) {

        if (estudianteId == null) {
            throw new IllegalArgumentException(
                "Debe seleccionar un estudiante."
            );
        }

        if (gradoId == null) {
            throw new IllegalArgumentException(
                "Debe seleccionar un grado."
            );
        }

        if (seccionId == null) {
            throw new IllegalArgumentException(
                "Debe seleccionar una sección."
            );
        }
    }

    // AVCR-158: Evitar asignaciones duplicadas
    private void validarAsignacionDuplicada(
            Long estudianteId,
            Long gradoId,
            Long seccionId,
            Long periodoAcademicoId) {

        boolean asignacionDuplicada =
                asignacionRepository
                    .existsByEstudiante_IdAndGrado_IdAndSeccion_IdAndPeriodoAcademico_IdAndActivoTrue(
                        estudianteId,
                        gradoId,
                        seccionId,
                        periodoAcademicoId
                    );

        if (asignacionDuplicada) {
            throw new IllegalArgumentException(
                "El estudiante ya se encuentra asignado "
                + "al grado y sección seleccionados "
                + "durante el período académico vigente."
            );
        }
    }
}