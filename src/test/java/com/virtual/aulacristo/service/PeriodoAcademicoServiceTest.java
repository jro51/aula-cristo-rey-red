package com.virtual.aulacristo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.virtual.aulacristo.model.AnioAcademico;
import com.virtual.aulacristo.model.PeriodoAcademico;
import com.virtual.aulacristo.repository.AnioAcademicoRepository;
import com.virtual.aulacristo.repository.PeriodoAcademicoRepository;

@ExtendWith(MockitoExtension.class)
class PeriodoAcademicoServiceTest {

    @Mock
    private AnioAcademicoRepository anioAcademicoRepository;

    @Mock
    private PeriodoAcademicoRepository periodoAcademicoRepository;

    @InjectMocks
    private AnioAcademicoService anioAcademicoService;

    @InjectMocks
    private PeriodoAcademicoService periodoAcademicoService;

    // AVCR-141: Probar registro de año académico
    @Test
    void debeRegistrarAnioAcademico() {

        AnioAcademico anioAcademico =
                new AnioAcademico(
                        2026,
                        "Año académico 2026");

        when(anioAcademicoRepository.existsByAnio(2026))
                .thenReturn(false);

        when(anioAcademicoRepository.save(
                any(AnioAcademico.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0));

        AnioAcademico resultado =
                anioAcademicoService.registrar(
                        anioAcademico);

        assertEquals(2026, resultado.getAnio());
        assertEquals(
                "Año académico 2026",
                resultado.getDescripcion());

        assertTrue(resultado.isActivo());

        verify(anioAcademicoRepository)
                .existsByAnio(2026);

        verify(anioAcademicoRepository)
                .save(anioAcademico);
    }

    // AVCR-141: No permitir años duplicados
    @Test
    void noDebeRegistrarAnioAcademicoDuplicado() {

        AnioAcademico anioAcademico =
                new AnioAcademico(
                        2026,
                        "Año académico duplicado");

        when(anioAcademicoRepository.existsByAnio(2026))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> anioAcademicoService
                                .registrar(anioAcademico));

        assertTrue(
                exception.getMessage()
                        .contains("ya se encuentra registrado"));

        verify(anioAcademicoRepository, never())
                .save(any(AnioAcademico.class));
    }

    // AVCR-141: Registrar correctamente un periodo
    @Test
    void debeRegistrarPeriodoAcademico() {

        AnioAcademico anioAcademico =
                crearAnioAcademico();

        PeriodoAcademico periodoAcademico =
                crearPeriodoAcademico(anioAcademico);

        when(anioAcademicoRepository.findById(1L))
                .thenReturn(Optional.of(anioAcademico));

        when(periodoAcademicoRepository
                .existsByAnioAcademicoIdAndNumero(
                        1L,
                        1))
                .thenReturn(false);

        when(periodoAcademicoRepository
                .existsByAnioAcademicoIdAndNombreIgnoreCase(
                        1L,
                        "Primer Bimestre"))
                .thenReturn(false);

        when(periodoAcademicoRepository.save(
                any(PeriodoAcademico.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0));

        PeriodoAcademico resultado =
                periodoAcademicoService.registrar(
                        periodoAcademico);

        assertEquals(
                "Primer Bimestre",
                resultado.getNombre());

        assertEquals(1, resultado.getNumero());
        assertTrue(resultado.isActivo());
        assertFalse(resultado.isVigente());

        assertSame(
                anioAcademico,
                resultado.getAnioAcademico());

        verify(periodoAcademicoRepository)
                .save(periodoAcademico);
    }

    // AVCR-141 y AVCR-140:
    // No permitir número de periodo duplicado
    @Test
    void noDebeRegistrarNumeroDePeriodoDuplicado() {

        AnioAcademico anioAcademico =
                crearAnioAcademico();

        PeriodoAcademico periodoAcademico =
                crearPeriodoAcademico(anioAcademico);

        when(anioAcademicoRepository.findById(1L))
                .thenReturn(Optional.of(anioAcademico));

        when(periodoAcademicoRepository
                .existsByAnioAcademicoIdAndNumero(
                        1L,
                        1))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> periodoAcademicoService
                                .registrar(periodoAcademico));

        assertTrue(
                exception.getMessage()
                        .contains("periodo número"));

        verify(periodoAcademicoRepository, never())
                .save(any(PeriodoAcademico.class));
    }

    // AVCR-141 y AVCR-140:
    // No permitir nombre de periodo duplicado
    @Test
    void noDebeRegistrarNombreDePeriodoDuplicado() {

        AnioAcademico anioAcademico =
                crearAnioAcademico();

        PeriodoAcademico periodoAcademico =
                crearPeriodoAcademico(anioAcademico);

        when(anioAcademicoRepository.findById(1L))
                .thenReturn(Optional.of(anioAcademico));

        when(periodoAcademicoRepository
                .existsByAnioAcademicoIdAndNumero(
                        1L,
                        1))
                .thenReturn(false);

        when(periodoAcademicoRepository
                .existsByAnioAcademicoIdAndNombreIgnoreCase(
                        1L,
                        "Primer Bimestre"))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> periodoAcademicoService
                                .registrar(periodoAcademico));

        assertTrue(
                exception.getMessage()
                        .contains("Ya existe un periodo llamado"));

        verify(periodoAcademicoRepository, never())
                .save(any(PeriodoAcademico.class));
    }

    // AVCR-141: Validar el rango de fechas
    @Test
    void noDebeRegistrarPeriodoConFechasIncorrectas() {

        AnioAcademico anioAcademico =
                crearAnioAcademico();

        PeriodoAcademico periodoAcademico =
                crearPeriodoAcademico(anioAcademico);

        periodoAcademico.setFechaInicio(
                LocalDate.of(2026, 5, 10));

        periodoAcademico.setFechaFin(
                LocalDate.of(2026, 3, 2));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> periodoAcademicoService
                                .registrar(periodoAcademico));

        assertTrue(
                exception.getMessage()
                        .contains("fecha de fin"));

        verifyNoInteractions(
                anioAcademicoRepository,
                periodoAcademicoRepository);
    }

    // AVCR-141 y AVCR-139:
    // Activar un periodo y desactivar el anterior
    @Test
    void debeActivarPeriodoVigenteYDesactivarAnterior() {

        AnioAcademico anioAcademico =
                crearAnioAcademico();

        PeriodoAcademico periodoAnterior =
                crearPeriodoAcademico(anioAcademico);

        periodoAnterior.setId(1L);
        periodoAnterior.setNombre("Primer Bimestre");
        periodoAnterior.setNumero(1);
        periodoAnterior.setVigente(true);

        PeriodoAcademico periodoNuevo =
                crearPeriodoAcademico(anioAcademico);

        periodoNuevo.setId(2L);
        periodoNuevo.setNombre("Segundo Bimestre");
        periodoNuevo.setNumero(2);
        periodoNuevo.setVigente(false);

        when(periodoAcademicoRepository.findById(2L))
                .thenReturn(Optional.of(periodoNuevo));

        when(periodoAcademicoRepository.findByVigenteTrue())
                .thenReturn(List.of(periodoAnterior));

        when(periodoAcademicoRepository.saveAll(anyList()))
                .thenReturn(List.of(periodoAnterior));

        when(periodoAcademicoRepository.save(periodoNuevo))
                .thenReturn(periodoNuevo);

        PeriodoAcademico resultado =
                periodoAcademicoService
                        .activarComoVigente(2L);

        assertFalse(periodoAnterior.isVigente());
        assertTrue(resultado.isVigente());

        verify(periodoAcademicoRepository)
                .saveAll(anyList());

        verify(periodoAcademicoRepository)
                .save(periodoNuevo);
    }

    // AVCR-141: No activar un periodo inactivo
    @Test
    void noDebeActivarPeriodoInactivo() {

        AnioAcademico anioAcademico =
                crearAnioAcademico();

        PeriodoAcademico periodoAcademico =
                crearPeriodoAcademico(anioAcademico);

        periodoAcademico.setId(1L);
        periodoAcademico.setActivo(false);

        when(periodoAcademicoRepository.findById(1L))
                .thenReturn(Optional.of(periodoAcademico));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> periodoAcademicoService
                                .activarComoVigente(1L));

        assertEquals(
                "No se puede activar un periodo inactivo.",
                exception.getMessage());

        verify(periodoAcademicoRepository, never())
                .findByVigenteTrue();

        verify(periodoAcademicoRepository, never())
                .save(any(PeriodoAcademico.class));
    }

    private AnioAcademico crearAnioAcademico() {

        AnioAcademico anioAcademico =
                new AnioAcademico(
                        2026,
                        "Año académico 2026");

        anioAcademico.setId(1L);
        anioAcademico.setActivo(true);

        return anioAcademico;
    }

    private PeriodoAcademico crearPeriodoAcademico(
            AnioAcademico anioAcademico) {

        PeriodoAcademico periodoAcademico =
                new PeriodoAcademico();

        periodoAcademico.setNombre("Primer Bimestre");
        periodoAcademico.setNumero(1);

        periodoAcademico.setFechaInicio(
                LocalDate.of(2026, 3, 2));

        periodoAcademico.setFechaFin(
                LocalDate.of(2026, 5, 8));

        periodoAcademico.setAnioAcademico(anioAcademico);
        periodoAcademico.setActivo(true);
        periodoAcademico.setVigente(false);

        return periodoAcademico;
    }
}