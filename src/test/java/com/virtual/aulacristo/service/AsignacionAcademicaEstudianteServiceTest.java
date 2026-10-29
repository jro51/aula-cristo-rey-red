package com.virtual.aulacristo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.virtual.aulacristo.model.AsignacionAcademicaEstudiante;
import com.virtual.aulacristo.model.Estudiante;
import com.virtual.aulacristo.model.Grado;
import com.virtual.aulacristo.model.Nivel;
import com.virtual.aulacristo.model.PeriodoAcademico;
import com.virtual.aulacristo.model.Seccion;
import com.virtual.aulacristo.repository.AsignacionAcademicaEstudianteRepository;
import com.virtual.aulacristo.repository.EstudianteRepository;
import com.virtual.aulacristo.repository.GradoRepository;
import com.virtual.aulacristo.repository.PeriodoAcademicoRepository;
import com.virtual.aulacristo.repository.SeccionRepository;

@ExtendWith(MockitoExtension.class)
class AsignacionAcademicaEstudianteServiceTest {

    @Mock
    private AsignacionAcademicaEstudianteRepository
            asignacionRepository;

    @Mock
    private EstudianteRepository estudianteRepository;

    @Mock
    private GradoRepository gradoRepository;

    @Mock
    private SeccionRepository seccionRepository;

    @Mock
    private PeriodoAcademicoRepository periodoRepository;

    @InjectMocks
    private AsignacionAcademicaEstudianteService
            asignacionService;

    private Estudiante estudiante;
    private Nivel nivel;
    private Grado grado;
    private Seccion seccion;
    private PeriodoAcademico periodo;

    @BeforeEach
    void prepararDatos() {

        estudiante = new Estudiante();
        estudiante.setId(1L);
        estudiante.setCodigo("EST-2026-12345678");
        estudiante.setNombres("María");
        estudiante.setApellidos("Torres");
        estudiante.setDni("12345678");
        estudiante.setActivo(true);

        nivel = new Nivel();
        nivel.setId(10L);
        nivel.setNombre("Primaria");
        nivel.setActivo(true);

        grado = new Grado();
        grado.setId(20L);
        grado.setNombre("Primero");
        grado.setNivel(nivel);
        grado.setActivo(true);

        seccion = new Seccion();
        seccion.setId(30L);
        seccion.setNombre("A");
        seccion.setGrado(grado);
        seccion.setActivo(true);

        periodo = new PeriodoAcademico();
        periodo.setId(40L);
        periodo.setNombre("Primer Bimestre");
        periodo.setNumero(1);
        periodo.setActivo(true);
        periodo.setVigente(true);
    }

    @Test
    void debeAsignarEstudianteCorrectamente() {

        prepararFlujoValido();

        when(
            asignacionRepository
                .findByEstudiante_IdAndPeriodoAcademico_Id(
                    estudiante.getId(),
                    periodo.getId()
                )
        ).thenReturn(Optional.empty());

        when(
            asignacionRepository.save(
                any(AsignacionAcademicaEstudiante.class)
            )
        ).thenAnswer(invocacion -> {

            AsignacionAcademicaEstudiante asignacion =
                    invocacion.getArgument(0);

            asignacion.setId(100L);

            return asignacion;
        });

        AsignacionAcademicaEstudiante resultado =
                asignacionService.asignarOReasignar(
                    estudiante.getId(),
                    grado.getId(),
                    seccion.getId()
                );

        assertEquals(100L, resultado.getId());
        assertSame(estudiante, resultado.getEstudiante());
        assertSame(grado, resultado.getGrado());
        assertSame(seccion, resultado.getSeccion());
        assertSame(periodo, resultado.getPeriodoAcademico());
        assertTrue(resultado.isActivo());

        assertEquals(
            "Primero - Primaria",
            estudiante.getGrado()
        );

        assertEquals(
            "A",
            estudiante.getSeccion()
        );

        verify(estudianteRepository).save(estudiante);

        verify(asignacionRepository).save(
            any(AsignacionAcademicaEstudiante.class)
        );
    }

    @Test
    void debeReasignarEstudianteSinCrearOtroRegistro() {

        Seccion nuevaSeccion = new Seccion();
        nuevaSeccion.setId(31L);
        nuevaSeccion.setNombre("B");
        nuevaSeccion.setGrado(grado);
        nuevaSeccion.setActivo(true);

        AsignacionAcademicaEstudiante existente =
                crearAsignacionExistente();

        when(
            estudianteRepository.findById(estudiante.getId())
        ).thenReturn(Optional.of(estudiante));

        when(
            gradoRepository.findById(grado.getId())
        ).thenReturn(Optional.of(grado));

        when(
            seccionRepository.findById(nuevaSeccion.getId())
        ).thenReturn(Optional.of(nuevaSeccion));

        when(
            periodoRepository.findByVigenteTrue()
        ).thenReturn(List.of(periodo));

        when(
            asignacionRepository
                .findByEstudiante_IdAndPeriodoAcademico_Id(
                    estudiante.getId(),
                    periodo.getId()
                )
        ).thenReturn(Optional.of(existente));

        when(
            asignacionRepository.save(existente)
        ).thenReturn(existente);

        AsignacionAcademicaEstudiante resultado =
                asignacionService.asignarOReasignar(
                    estudiante.getId(),
                    grado.getId(),
                    nuevaSeccion.getId()
                );

        assertSame(existente, resultado);
        assertSame(nuevaSeccion, resultado.getSeccion());
        assertEquals("B", estudiante.getSeccion());
        assertTrue(resultado.isActivo());

        verify(asignacionRepository).save(existente);
        verify(estudianteRepository).save(estudiante);
    }

    @Test
    void debeRechazarAsignacionDuplicada() {

        prepararFlujoValido();

        when(
            asignacionRepository
                .existsByEstudiante_IdAndGrado_IdAndSeccion_IdAndPeriodoAcademico_IdAndActivoTrue(
                    estudiante.getId(),
                    grado.getId(),
                    seccion.getId(),
                    periodo.getId()
                )
        ).thenReturn(true);

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> asignacionService.asignarOReasignar(
                estudiante.getId(),
                grado.getId(),
                seccion.getId()
            )
        );

        assertTrue(
            error.getMessage().contains(
                "ya se encuentra asignado"
            )
        );

        verify(estudianteRepository, never()).save(
            any(Estudiante.class)
        );

        verify(asignacionRepository, never()).save(
            any(AsignacionAcademicaEstudiante.class)
        );
    }

    @Test
    void debeRechazarEstudianteInactivo() {

        estudiante.setActivo(false);

        when(
            estudianteRepository.findById(estudiante.getId())
        ).thenReturn(Optional.of(estudiante));

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> asignacionService.asignarOReasignar(
                estudiante.getId(),
                grado.getId(),
                seccion.getId()
            )
        );

        assertEquals(
            "El estudiante seleccionado está inactivo.",
            error.getMessage()
        );

        verify(asignacionRepository, never()).save(
            any(AsignacionAcademicaEstudiante.class)
        );
    }

    @Test
    void debeRechazarGradoInactivo() {

        grado.setActivo(false);

        when(
            estudianteRepository.findById(estudiante.getId())
        ).thenReturn(Optional.of(estudiante));

        when(
            gradoRepository.findById(grado.getId())
        ).thenReturn(Optional.of(grado));

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> asignacionService.asignarOReasignar(
                estudiante.getId(),
                grado.getId(),
                seccion.getId()
            )
        );

        assertEquals(
            "El grado seleccionado está inactivo.",
            error.getMessage()
        );

        verify(asignacionRepository, never()).save(
            any(AsignacionAcademicaEstudiante.class)
        );
    }

    @Test
    void debeRechazarCuandoNoExistePeriodoVigente() {

        when(
            estudianteRepository.findById(estudiante.getId())
        ).thenReturn(Optional.of(estudiante));

        when(
            gradoRepository.findById(grado.getId())
        ).thenReturn(Optional.of(grado));

        when(
            seccionRepository.findById(seccion.getId())
        ).thenReturn(Optional.of(seccion));

        when(
            periodoRepository.findByVigenteTrue()
        ).thenReturn(List.of());

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> asignacionService.asignarOReasignar(
                estudiante.getId(),
                grado.getId(),
                seccion.getId()
            )
        );

        assertEquals(
            "No existe un período académico vigente.",
            error.getMessage()
        );

        verify(asignacionRepository, never()).save(
            any(AsignacionAcademicaEstudiante.class)
        );
    }

    @Test
    void debeRechazarSeccionDeOtroGrado() {

        Grado otroGrado = new Grado();
        otroGrado.setId(99L);
        otroGrado.setNombre("Segundo");
        otroGrado.setNivel(nivel);
        otroGrado.setActivo(true);

        seccion.setGrado(otroGrado);

        prepararFlujoValido();

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> asignacionService.asignarOReasignar(
                estudiante.getId(),
                grado.getId(),
                seccion.getId()
            )
        );

        assertEquals(
            "La sección seleccionada no pertenece al grado indicado.",
            error.getMessage()
        );

        verify(asignacionRepository, never()).save(
            any(AsignacionAcademicaEstudiante.class)
        );
    }

    @Test
    void debeListarEstudiantesPorGradoYSeccion() {

        AsignacionAcademicaEstudiante asignacion =
                crearAsignacionExistente();

        when(
            gradoRepository.findById(grado.getId())
        ).thenReturn(Optional.of(grado));

        when(
            seccionRepository.findById(seccion.getId())
        ).thenReturn(Optional.of(seccion));

        when(
            periodoRepository.findByVigenteTrue()
        ).thenReturn(List.of(periodo));

        when(
            asignacionRepository
                .findByGrado_IdAndSeccion_IdAndPeriodoAcademico_IdAndActivoTrueOrderByEstudiante_ApellidosAscEstudiante_NombresAsc(
                    grado.getId(),
                    seccion.getId(),
                    periodo.getId()
                )
        ).thenReturn(List.of(asignacion));

        List<AsignacionAcademicaEstudiante> resultado =
                asignacionService.listarPorGradoYSeccion(
                    grado.getId(),
                    seccion.getId()
                );

        assertEquals(1, resultado.size());
        assertSame(
            estudiante,
            resultado.get(0).getEstudiante()
        );

        assertEquals(
            "María",
            resultado.get(0).getEstudiante().getNombres()
        );

        verify(asignacionRepository)
            .findByGrado_IdAndSeccion_IdAndPeriodoAcademico_IdAndActivoTrueOrderByEstudiante_ApellidosAscEstudiante_NombresAsc(
                grado.getId(),
                seccion.getId(),
                periodo.getId()
            );
    }

    @Test
    void debeListarAsignacionesDelPeriodoVigente() {

        AsignacionAcademicaEstudiante asignacion =
                crearAsignacionExistente();

        when(
            periodoRepository.findByVigenteTrue()
        ).thenReturn(List.of(periodo));

        when(
            asignacionRepository
                .findByPeriodoAcademico_IdAndActivoTrueOrderByEstudiante_ApellidosAscEstudiante_NombresAsc(
                    periodo.getId()
                )
        ).thenReturn(List.of(asignacion));

        List<AsignacionAcademicaEstudiante> resultado =
                asignacionService.listarAsignacionesVigentes();

        assertEquals(1, resultado.size());
        assertSame(asignacion, resultado.get(0));
    }

    @Test
    void debeRechazarMasDeUnPeriodoVigente() {

        PeriodoAcademico segundoPeriodo =
                new PeriodoAcademico();

        segundoPeriodo.setId(41L);
        segundoPeriodo.setNombre("Segundo Bimestre");
        segundoPeriodo.setActivo(true);
        segundoPeriodo.setVigente(true);

        when(
            periodoRepository.findByVigenteTrue()
        ).thenReturn(
            List.of(periodo, segundoPeriodo)
        );

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> asignacionService.obtenerPeriodoVigente()
        );

        assertTrue(
            error.getMessage().contains(
                "más de un período académico vigente"
            )
        );
    }

    private void prepararFlujoValido() {

        when(
            estudianteRepository.findById(estudiante.getId())
        ).thenReturn(Optional.of(estudiante));

        when(
            gradoRepository.findById(grado.getId())
        ).thenReturn(Optional.of(grado));

        when(
            seccionRepository.findById(seccion.getId())
        ).thenReturn(Optional.of(seccion));

        when(
            periodoRepository.findByVigenteTrue()
        ).thenReturn(List.of(periodo));
    }

    private AsignacionAcademicaEstudiante
            crearAsignacionExistente() {

        AsignacionAcademicaEstudiante asignacion =
                new AsignacionAcademicaEstudiante();

        asignacion.setId(100L);
        asignacion.setEstudiante(estudiante);
        asignacion.setGrado(grado);
        asignacion.setSeccion(seccion);
        asignacion.setPeriodoAcademico(periodo);
        asignacion.setActivo(true);

        return asignacion;
    }
}