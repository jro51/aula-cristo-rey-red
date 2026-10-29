package com.virtual.aulacristo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.virtual.aulacristo.model.Curso;
import com.virtual.aulacristo.model.Grado;
import com.virtual.aulacristo.repository.CursoRepository;
import com.virtual.aulacristo.repository.GradoRepository;

@ExtendWith(MockitoExtension.class)
class CursoServiceTest {

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private GradoRepository gradoRepository;

    @InjectMocks
    private CursoService cursoService;

    // AVCR-147: Registrar correctamente un curso
    @Test
    void debeRegistrarCursoCorrectamente() {

        Grado grado = crearGradoActivo();
        Curso curso = crearCurso(grado);

        when(gradoRepository.findById(1L))
                .thenReturn(Optional.of(grado));

        when(cursoRepository
                .existsByCodigoIgnoreCase("MAT-1SEC"))
                .thenReturn(false);

        when(cursoRepository
                .existsByNombreIgnoreCaseAndGradoId(
                        "Matemática",
                        1L))
                .thenReturn(false);

        when(cursoRepository.save(any(Curso.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0));

        Curso resultado =
                cursoService.registrarCurso(curso);

        assertEquals("MAT-1SEC", resultado.getCodigo());
        assertEquals("Matemática", resultado.getNombre());
        assertEquals(5, resultado.getHorasSemanales());
        assertTrue(resultado.isActivo());
        assertSame(grado, resultado.getGrado());

        verify(cursoRepository).save(curso);
    }

    // AVCR-146 y AVCR-147:
    // No permitir códigos duplicados
    @Test
    void noDebeRegistrarCodigoDuplicado() {

        Grado grado = crearGradoActivo();
        Curso curso = crearCurso(grado);

        when(gradoRepository.findById(1L))
                .thenReturn(Optional.of(grado));

        when(cursoRepository
                .existsByCodigoIgnoreCase("MAT-1SEC"))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> cursoService
                                .registrarCurso(curso));

        assertTrue(
                exception.getMessage()
                        .contains("Ya existe un curso"));

        verify(cursoRepository, never())
                .save(any(Curso.class));
    }

    // AVCR-146 y AVCR-147:
    // No permitir nombres repetidos en el mismo grado
    @Test
    void noDebeRegistrarNombreDuplicadoEnMismoGrado() {

        Grado grado = crearGradoActivo();
        Curso curso = crearCurso(grado);

        when(gradoRepository.findById(1L))
                .thenReturn(Optional.of(grado));

        when(cursoRepository
                .existsByCodigoIgnoreCase("MAT-1SEC"))
                .thenReturn(false);

        when(cursoRepository
                .existsByNombreIgnoreCaseAndGradoId(
                        "Matemática",
                        1L))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> cursoService
                                .registrarCurso(curso));

        assertTrue(
                exception.getMessage()
                        .contains("grado seleccionado"));

        verify(cursoRepository, never())
                .save(any(Curso.class));
    }

    // AVCR-147: Validar campos obligatorios
    @Test
    void noDebeRegistrarCursoSinCodigo() {

        Grado grado = crearGradoActivo();
        Curso curso = crearCurso(grado);

        curso.setCodigo(" ");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> cursoService
                                .registrarCurso(curso));

        assertEquals(
                "El código del curso es obligatorio.",
                exception.getMessage());

        verifyNoInteractions(
                gradoRepository,
                cursoRepository);
    }

    // AVCR-147: Validar horas semanales
    @Test
    void noDebeRegistrarCursoConHorasInvalidas() {

        Grado grado = crearGradoActivo();
        Curso curso = crearCurso(grado);

        curso.setHorasSemanales(0);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> cursoService
                                .registrarCurso(curso));

        assertEquals(
                "Las horas semanales deben ser mayores que cero.",
                exception.getMessage());

        verifyNoInteractions(
                gradoRepository,
                cursoRepository);
    }

    // AVCR-147: No registrar en un grado inactivo
    @Test
    void noDebeRegistrarCursoEnGradoInactivo() {

        Grado grado = crearGradoActivo();
        grado.setActivo(false);

        Curso curso = crearCurso(grado);

        when(gradoRepository.findById(1L))
                .thenReturn(Optional.of(grado));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> cursoService
                                .registrarCurso(curso));

        assertEquals(
                "No se puede registrar un curso en un grado inactivo.",
                exception.getMessage());

        verify(cursoRepository, never())
                .save(any(Curso.class));
    }

    // AVCR-147: Actualizar un curso
    @Test
    void debeActualizarCursoCorrectamente() {

        Grado grado = crearGradoActivo();

        Curso cursoExistente = crearCurso(grado);
        cursoExistente.setId(10L);

        Curso cursoActualizado = crearCurso(grado);
        cursoActualizado.setCodigo("MAT-2SEC");
        cursoActualizado.setNombre("Matemática Avanzada");
        cursoActualizado.setDescripcion(
                "Curso actualizado");
        cursoActualizado.setHorasSemanales(6);

        when(cursoRepository.findById(10L))
                .thenReturn(Optional.of(cursoExistente));

        when(gradoRepository.findById(1L))
                .thenReturn(Optional.of(grado));

        when(cursoRepository
                .existsByCodigoIgnoreCaseAndIdNot(
                        "MAT-2SEC",
                        10L))
                .thenReturn(false);

        when(cursoRepository
                .existsByNombreIgnoreCaseAndGradoIdAndIdNot(
                        "Matemática Avanzada",
                        1L,
                        10L))
                .thenReturn(false);

        when(cursoRepository.save(cursoExistente))
                .thenReturn(cursoExistente);

        Curso resultado =
                cursoService.actualizarCurso(
                        10L,
                        cursoActualizado);

        assertEquals(
                "MAT-2SEC",
                resultado.getCodigo());

        assertEquals(
                "Matemática Avanzada",
                resultado.getNombre());

        assertEquals(
                "Curso actualizado",
                resultado.getDescripcion());

        assertEquals(
                6,
                resultado.getHorasSemanales());

        verify(cursoRepository)
                .save(cursoExistente);
    }

    // AVCR-147: Desactivar un curso
    @Test
    void debeDesactivarCurso() {

        Grado grado = crearGradoActivo();

        Curso curso = crearCurso(grado);
        curso.setId(10L);
        curso.setActivo(true);

        when(cursoRepository.findById(10L))
                .thenReturn(Optional.of(curso));

        when(cursoRepository.save(curso))
                .thenReturn(curso);

        Curso resultado =
                cursoService.cambiarEstado(
                        10L,
                        false);

        assertFalse(resultado.isActivo());

        verify(cursoRepository).save(curso);
    }

    // AVCR-147: Baja lógica del curso
    @Test
    void eliminarCursoDebeRealizarBajaLogica() {

        Grado grado = crearGradoActivo();

        Curso curso = crearCurso(grado);
        curso.setId(10L);
        curso.setActivo(true);

        when(cursoRepository.findById(10L))
                .thenReturn(Optional.of(curso));

        cursoService.eliminarCurso(10L);

        assertFalse(curso.isActivo());

        verify(cursoRepository).save(curso);
        verify(cursoRepository, never()).deleteById(10L);
    }

    // AVCR-147: Listar solamente cursos activos
    @Test
    void debeListarSolamenteCursosActivos() {

        Grado grado = crearGradoActivo();
        Curso curso = crearCurso(grado);

        when(cursoRepository
                .findByActivoTrueOrderByNombreAsc())
                .thenReturn(List.of(curso));

        List<Curso> resultado =
                cursoService.listarCursosActivos();

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).isActivo());

        verify(cursoRepository)
                .findByActivoTrueOrderByNombreAsc();
    }

    private Grado crearGradoActivo() {

        Grado grado = new Grado();

        grado.setId(1L);
        grado.setNombre("Primero de Secundaria");
        grado.setActivo(true);

        return grado;
    }

    private Curso crearCurso(Grado grado) {

        Curso curso = new Curso();

        curso.setCodigo("MAT-1SEC");
        curso.setNombre("Matemática");

        curso.setDescripcion(
                "Curso de Matemática");

        curso.setHorasSemanales(5);
        curso.setActivo(true);
        curso.setGrado(grado);

        return curso;
    }
}