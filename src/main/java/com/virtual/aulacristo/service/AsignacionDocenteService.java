package com.virtual.aulacristo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.virtual.aulacristo.entity.Usuario;
import com.virtual.aulacristo.model.AsignacionDocente;
import com.virtual.aulacristo.model.Curso;
import com.virtual.aulacristo.model.Grado;
import com.virtual.aulacristo.model.Seccion;
import com.virtual.aulacristo.repository.AsignacionDocenteRepository;
import com.virtual.aulacristo.repository.CursoRepository;
import com.virtual.aulacristo.repository.GradoRepository;
import com.virtual.aulacristo.repository.SeccionRepository;
import com.virtual.aulacristo.repository.UsuarioRepository;

@Service
public class AsignacionDocenteService {

    private final AsignacionDocenteRepository asignacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;
    private final GradoRepository gradoRepository;
    private final SeccionRepository seccionRepository;

    public AsignacionDocenteService(
            AsignacionDocenteRepository asignacionRepository,
            UsuarioRepository usuarioRepository,
            CursoRepository cursoRepository,
            GradoRepository gradoRepository,
            SeccionRepository seccionRepository) {

        this.asignacionRepository = asignacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.cursoRepository = cursoRepository;
        this.gradoRepository = gradoRepository;
        this.seccionRepository = seccionRepository;
    }

    // AVCR-149 y AVCR-152:
    // Asignar docente validando duplicados e incompatibilidades
    @Transactional
    public AsignacionDocente asignarDocente(
            Long docenteId,
            Long cursoId,
            Long gradoId,
            Long seccionId) {

        validarIdsObligatorios(
            docenteId,
            cursoId,
            gradoId,
            seccionId
        );

        Usuario docente = usuarioRepository.findById(docenteId)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "El docente seleccionado no existe."
                    )
                );

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "El curso seleccionado no existe."
                    )
                );

        Grado grado = gradoRepository.findById(gradoId)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "El grado seleccionado no existe."
                    )
                );

        Seccion seccion = seccionRepository.findById(seccionId)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "La sección seleccionada no existe."
                    )
                );

        validarDocente(docente);
        validarElementosActivos(curso, grado, seccion);
        validarCompatibilidad(curso, grado, seccion);

        validarAsignacionDuplicada(
            docenteId,
            cursoId,
            gradoId,
            seccionId
        );

        validarCursoSinOtroDocente(
            cursoId,
            gradoId,
            seccionId
        );

        AsignacionDocente asignacion = new AsignacionDocente();

        asignacion.setDocente(docente);
        asignacion.setCurso(curso);
        asignacion.setGrado(grado);
        asignacion.setSeccion(seccion);
        asignacion.setActivo(true);

        return asignacionRepository.save(asignacion);
    }

    @Transactional(readOnly = true)
    public List<AsignacionDocente> listarAsignaciones() {
        return asignacionRepository.findAllByOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public AsignacionDocente buscarPorId(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                "Debe indicar la asignación."
            );
        }

        return asignacionRepository.findById(id)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "La asignación docente no existe."
                    )
                );
    }

    @Transactional(readOnly = true)
    public List<AsignacionDocente> listarPorDocente(
            Long docenteId) {

        if (docenteId == null) {
            throw new IllegalArgumentException(
                "Debe indicar el docente."
            );
        }

        return asignacionRepository
                .findByDocente_IdUsuarioOrderByCurso_NombreAsc(
                    docenteId
                );
    }

    @Transactional(readOnly = true)
    public List<AsignacionDocente> listarActivasPorDocente(
            Long docenteId) {

        if (docenteId == null) {
            throw new IllegalArgumentException(
                "Debe indicar el docente."
            );
        }

        return asignacionRepository
                .findByDocente_IdUsuarioAndActivoTrueOrderByCurso_NombreAsc(
                    docenteId
                );
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarDocentesActivos() {

        return usuarioRepository
                .findByRol_NombreIgnoreCaseAndEstadoTrueOrderByUsernameAsc(
                    "DOCENTE"
                );
    }

    @Transactional
    public AsignacionDocente cambiarEstado(
            Long asignacionId,
            boolean activo) {

        AsignacionDocente asignacion =
                buscarPorId(asignacionId);

        // Si se intenta activar nuevamente, se validan las relaciones
        if (activo) {
            validarDocente(asignacion.getDocente());

            validarElementosActivos(
                asignacion.getCurso(),
                asignacion.getGrado(),
                asignacion.getSeccion()
            );

            validarCompatibilidad(
                asignacion.getCurso(),
                asignacion.getGrado(),
                asignacion.getSeccion()
            );

            boolean existeOtraAsignacionActiva =
                    asignacionRepository
                        .existsByCurso_IdAndGrado_IdAndSeccion_IdAndActivoTrueAndIdNot(
                            asignacion.getCurso().getId(),
                            asignacion.getGrado().getId(),
                            asignacion.getSeccion().getId(),
                            asignacion.getId()
                        );

            if (existeOtraAsignacionActiva) {
                throw new IllegalArgumentException(
                    "No se puede activar la asignación porque "
                    + "el curso ya tiene otro docente asignado "
                    + "en ese grado y sección."
                );
            }
        }

        asignacion.setActivo(activo);

        return asignacionRepository.save(asignacion);
    }

    private void validarIdsObligatorios(
            Long docenteId,
            Long cursoId,
            Long gradoId,
            Long seccionId) {

        if (docenteId == null) {
            throw new IllegalArgumentException(
                "Debe seleccionar un docente."
            );
        }

        if (cursoId == null) {
            throw new IllegalArgumentException(
                "Debe seleccionar un curso."
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

    private void validarDocente(Usuario docente) {

        if (docente.getRol() == null
                || docente.getRol().getNombre() == null
                || !"DOCENTE".equalsIgnoreCase(
                    docente.getRol().getNombre()
                )) {

            throw new IllegalArgumentException(
                "El usuario seleccionado no tiene el rol DOCENTE."
            );
        }

        if (!Boolean.TRUE.equals(docente.getEstado())) {
            throw new IllegalArgumentException(
                "El docente seleccionado está inactivo."
            );
        }
    }

    private void validarElementosActivos(
            Curso curso,
            Grado grado,
            Seccion seccion) {

        if (!curso.isActivo()) {
            throw new IllegalArgumentException(
                "El curso seleccionado está inactivo."
            );
        }

        if (!grado.isActivo()) {
            throw new IllegalArgumentException(
                "El grado seleccionado está inactivo."
            );
        }

        if (!seccion.isActivo()) {
            throw new IllegalArgumentException(
                "La sección seleccionada está inactiva."
            );
        }
    }

    private void validarCompatibilidad(
            Curso curso,
            Grado grado,
            Seccion seccion) {

        if (curso.getGrado() == null
                || curso.getGrado().getId() == null
                || !curso.getGrado().getId().equals(grado.getId())) {

            throw new IllegalArgumentException(
                "El curso seleccionado no pertenece al grado indicado."
            );
        }

        if (seccion.getGrado() == null
                || seccion.getGrado().getId() == null
                || !seccion.getGrado().getId().equals(grado.getId())) {

            throw new IllegalArgumentException(
                "La sección seleccionada no pertenece al grado indicado."
            );
        }
    }

    private void validarAsignacionDuplicada(
            Long docenteId,
            Long cursoId,
            Long gradoId,
            Long seccionId) {

        boolean existeAsignacion =
                asignacionRepository
                    .existsByDocente_IdUsuarioAndCurso_IdAndGrado_IdAndSeccion_Id(
                        docenteId,
                        cursoId,
                        gradoId,
                        seccionId
                    );

        if (existeAsignacion) {
            throw new IllegalArgumentException(
                "El docente ya tiene registrado este curso "
                + "en el grado y sección seleccionados."
            );
        }
    }

    private void validarCursoSinOtroDocente(
            Long cursoId,
            Long gradoId,
            Long seccionId) {

        boolean yaTieneDocente =
                asignacionRepository
                    .existsByCurso_IdAndGrado_IdAndSeccion_IdAndActivoTrue(
                        cursoId,
                        gradoId,
                        seccionId
                    );

        if (yaTieneDocente) {
            throw new IllegalArgumentException(
                "El curso ya tiene un docente activo "
                + "asignado en ese grado y sección."
            );
        }
    }
}