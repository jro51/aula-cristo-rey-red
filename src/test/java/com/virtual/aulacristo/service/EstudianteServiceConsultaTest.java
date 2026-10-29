package com.virtual.aulacristo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import com.virtual.aulacristo.model.Apoderado;
import com.virtual.aulacristo.model.Estudiante;
import com.virtual.aulacristo.repository.ApoderadoRepository;
import com.virtual.aulacristo.repository.EstudianteRepository;

@ExtendWith(MockitoExtension.class)
class EstudianteServiceConsultaTest {

    @Mock
    private EstudianteRepository estudianteRepository;

    @Mock
    private ApoderadoRepository apoderadoRepository;

    @InjectMocks
    private EstudianteService estudianteService;

    @Test
    void buscarEstudiantePorDniDebeRetornarEstudiante() {

        Estudiante estudiante = crearEstudiante();

        when(estudianteRepository.findByCodigoIgnoreCaseOrDni(
                "73829182",
                "73829182"
        )).thenReturn(Optional.of(estudiante));

        Estudiante resultado =
                estudianteService.buscarPorCodigoODni("73829182");

        assertSame(estudiante, resultado);

        verify(estudianteRepository)
                .findByCodigoIgnoreCaseOrDni(
                    "73829182",
                    "73829182"
                );
    }

    @Test
    void buscarEstudiantePorCodigoDebeRetornarEstudiante() {

        Estudiante estudiante = crearEstudiante();

        String codigo = "EST-2026-73829182";

        when(estudianteRepository.findByCodigoIgnoreCaseOrDni(
                codigo,
                codigo
        )).thenReturn(Optional.of(estudiante));

        Estudiante resultado =
                estudianteService.buscarPorCodigoODni(codigo);

        assertEquals(codigo, resultado.getCodigo());
    }

    @Test
    void buscarConEspaciosDebeLimpiarElCriterio() {

        Estudiante estudiante = crearEstudiante();

        when(estudianteRepository.findByCodigoIgnoreCaseOrDni(
                "73829182",
                "73829182"
        )).thenReturn(Optional.of(estudiante));

        Estudiante resultado =
                estudianteService.buscarPorCodigoODni(
                    "   73829182   "
                );

        assertSame(estudiante, resultado);

        verify(estudianteRepository)
                .findByCodigoIgnoreCaseOrDni(
                    "73829182",
                    "73829182"
                );
    }

    @Test
    void buscarConCriterioVacioDebeGenerarError() {

        IllegalArgumentException error =
                assertThrows(
                    IllegalArgumentException.class,
                    () -> estudianteService
                            .buscarPorCodigoODni("   ")
                );

        assertEquals(
            "Debe ingresar el código o DNI del estudiante.",
            error.getMessage()
        );

        verifyNoInteractions(estudianteRepository);
    }

    @Test
    void buscarEstudianteInexistenteDebeGenerarError() {

        when(estudianteRepository.findByCodigoIgnoreCaseOrDni(
                "11111111",
                "11111111"
        )).thenReturn(Optional.empty());

        IllegalArgumentException error =
                assertThrows(
                    IllegalArgumentException.class,
                    () -> estudianteService
                            .buscarPorCodigoODni("11111111")
                );

        assertEquals(
            "No se encontró ningún estudiante con el código o DNI: "
            + "11111111",
            error.getMessage()
        );
    }

    @Test
    void filtrarEstudiantesDebeRetornarResultados() {

        Estudiante estudiante = crearEstudiante();

        when(estudianteRepository.buscarConFiltros(
                "Miluska",
                "3ro Secundaria",
                "A",
                true
        )).thenReturn(List.of(estudiante));

        List<Estudiante> resultados =
                estudianteService.filtrarEstudiantes(
                    "Miluska",
                    "3ro Secundaria",
                    "A",
                    true
                );

        assertEquals(1, resultados.size());
        assertSame(estudiante, resultados.get(0));

        verify(estudianteRepository).buscarConFiltros(
            "Miluska",
            "3ro Secundaria",
            "A",
            true
        );
    }

    @Test
    void listarTodosDebeRetornarEstudiantes() {

        Estudiante estudiante = crearEstudiante();

        when(estudianteRepository.findAll())
                .thenReturn(List.of(estudiante));

        List<Estudiante> resultados =
                estudianteService.listarTodos();

        assertEquals(1, resultados.size());
        assertSame(estudiante, resultados.get(0));

        verify(estudianteRepository).findAll();
    }

    private Estudiante crearEstudiante() {

        Estudiante estudiante = new Estudiante();

        estudiante.setId(1L);
        estudiante.setCodigo("EST-2026-73829182");
        estudiante.setDni("73829182");
        estudiante.setNombres("Miluska");
        estudiante.setApellidos("Barboza");
        estudiante.setCorreo("miluska@correo.com");
        estudiante.setGrado("3ro Secundaria");
        estudiante.setSeccion("A");
        estudiante.setActivo(true);

        return estudiante;
    }
}