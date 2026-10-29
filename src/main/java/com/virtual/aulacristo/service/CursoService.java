package com.virtual.aulacristo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.virtual.aulacristo.model.Curso;
import com.virtual.aulacristo.model.Grado;
import com.virtual.aulacristo.repository.CursoRepository;
import com.virtual.aulacristo.repository.GradoRepository;

@Service
public class CursoService {

    private final CursoRepository cursoRepository;
    private final GradoRepository gradoRepository;

    public CursoService(
            CursoRepository cursoRepository,
            GradoRepository gradoRepository) {

        this.cursoRepository = cursoRepository;
        this.gradoRepository = gradoRepository;
    }

    public Curso registrarCurso(Curso curso) {

        validarDatosObligatorios(curso);
        normalizarDatos(curso);

        Grado grado = obtenerGradoDelCurso(curso);

        if (!grado.isActivo()) {
            throw new IllegalArgumentException(
                    "No se puede registrar un curso en un grado inactivo.");
        }

        validarDuplicadosAlRegistrar(
                curso,
                grado.getId());

        curso.setId(null);
        curso.setGrado(grado);
        curso.setActivo(true);

        return cursoRepository.save(curso);
    }

    public List<Curso> listarCursos() {
        return cursoRepository
                .findAllByOrderByNombreAsc();
    }

    public List<Curso> listarCursosActivos() {
        return cursoRepository
                .findByActivoTrueOrderByNombreAsc();
    }

    public List<Curso> listarPorGrado(Long gradoId) {

        validarGradoExistente(gradoId);

        return cursoRepository
                .findByGradoIdOrderByNombreAsc(gradoId);
    }

    public List<Curso> listarActivosPorGrado(
            Long gradoId) {

        validarGradoExistente(gradoId);

        return cursoRepository
                .findByGradoIdAndActivoTrueOrderByNombreAsc(
                        gradoId);
    }

    public Curso buscarPorId(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El identificador del curso es obligatorio.");
        }

        return cursoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró el curso solicitado."));
    }

    public Curso actualizarCurso(
            Long id,
            Curso cursoActualizado) {

        Curso cursoExistente = buscarPorId(id);

        validarDatosObligatorios(cursoActualizado);
        normalizarDatos(cursoActualizado);

        Grado grado =
                obtenerGradoDelCurso(cursoActualizado);

        if (!grado.isActivo()) {
            throw new IllegalArgumentException(
                    "No se puede asignar el curso a un grado inactivo.");
        }

        validarDuplicadosAlActualizar(
                id,
                cursoActualizado,
                grado.getId());

        cursoExistente.setCodigo(
                cursoActualizado.getCodigo());

        cursoExistente.setNombre(
                cursoActualizado.getNombre());

        cursoExistente.setDescripcion(
                cursoActualizado.getDescripcion());

        cursoExistente.setHorasSemanales(
                cursoActualizado.getHorasSemanales());

        cursoExistente.setGrado(grado);

        return cursoRepository.save(cursoExistente);
    }

    public Curso cambiarEstado(
            Long id,
            boolean nuevoEstado) {

        Curso curso = buscarPorId(id);
        curso.setActivo(nuevoEstado);

        return cursoRepository.save(curso);
    }

    public void eliminarCurso(Long id) {

        Curso curso = buscarPorId(id);

        // Baja lógica
        curso.setActivo(false);

        cursoRepository.save(curso);
    }

    // AVCR-146: Validar campos obligatorios
    private void validarDatosObligatorios(Curso curso) {

        if (curso == null) {
            throw new IllegalArgumentException(
                    "Los datos del curso son obligatorios.");
        }

        if (curso.getCodigo() == null
                || curso.getCodigo().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El código del curso es obligatorio.");
        }

        if (curso.getNombre() == null
                || curso.getNombre().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El nombre del curso es obligatorio.");
        }

        if (curso.getHorasSemanales() == null
                || curso.getHorasSemanales() < 1) {

            throw new IllegalArgumentException(
                    "Las horas semanales deben ser mayores que cero.");
        }

        if (curso.getGrado() == null
                || curso.getGrado().getId() == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un grado.");
        }
    }

    // AVCR-146: Validar duplicados al registrar
    private void validarDuplicadosAlRegistrar(
            Curso curso,
            Long gradoId) {

        if (cursoRepository.existsByCodigoIgnoreCase(
                curso.getCodigo())) {

            throw new IllegalArgumentException(
                    "Ya existe un curso registrado con el código: "
                            + curso.getCodigo());
        }

        if (cursoRepository
                .existsByNombreIgnoreCaseAndGradoId(
                        curso.getNombre(),
                        gradoId)) {

            throw new IllegalArgumentException(
                    "Ya existe el curso "
                            + curso.getNombre()
                            + " registrado en el grado seleccionado.");
        }
    }

    // AVCR-146: Validar duplicados al actualizar
    private void validarDuplicadosAlActualizar(
            Long cursoId,
            Curso curso,
            Long gradoId) {

        if (cursoRepository
                .existsByCodigoIgnoreCaseAndIdNot(
                        curso.getCodigo(),
                        cursoId)) {

            throw new IllegalArgumentException(
                    "Ya existe otro curso registrado con el código: "
                            + curso.getCodigo());
        }

        if (cursoRepository
                .existsByNombreIgnoreCaseAndGradoIdAndIdNot(
                        curso.getNombre(),
                        gradoId,
                        cursoId)) {

            throw new IllegalArgumentException(
                    "Ya existe otro curso llamado "
                            + curso.getNombre()
                            + " en el grado seleccionado.");
        }
    }

    private Grado obtenerGradoDelCurso(Curso curso) {

        return gradoRepository
                .findById(curso.getGrado().getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El grado seleccionado no existe."));
    }

    private void validarGradoExistente(Long gradoId) {

        if (gradoId == null) {
            throw new IllegalArgumentException(
                    "El identificador del grado es obligatorio.");
        }

        if (!gradoRepository.existsById(gradoId)) {
            throw new IllegalArgumentException(
                    "El grado seleccionado no existe.");
        }
    }

    private void normalizarDatos(Curso curso) {

        curso.setCodigo(
                curso.getCodigo()
                        .trim()
                        .toUpperCase());

        curso.setNombre(
                curso.getNombre().trim());

        if (curso.getDescripcion() != null) {
            curso.setDescripcion(
                    curso.getDescripcion().trim());
        }
    }
}