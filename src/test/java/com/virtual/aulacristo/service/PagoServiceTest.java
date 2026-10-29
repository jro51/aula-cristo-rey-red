package com.virtual.aulacristo.service;
import com.virtual.aulacristo.dto.MensualidadEstadoDto;
import com.virtual.aulacristo.model.Pago;
import com.virtual.aulacristo.repository.PagoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

//AVCR-199: Probar estados de mensualidades
@ExtendWith(MockitoExtension.class)
class PagoServiceTest {
    @Mock
    private PagoRepository pagoRepository;

    @InjectMocks
    private PagoService pagoService;

    @Test
    public void testObtenerEstadoMensualidadesPorAnio() {
        // Arrange
        Long estudianteId = 1L;
        Integer anio = 2026;

        Pago pagoMatricula = new Pago();
        pagoMatricula.setConcepto("Matricula 2026");
        pagoMatricula.setMonto(150.0);
        pagoMatricula.setFechaPago(LocalDate.of(2026, 9, 21));

        when(pagoRepository.findByEstudianteId(estudianteId)).thenReturn(Arrays.asList(pagoMatricula));

        // Act
        List<MensualidadEstadoDto> resultado = pagoService.obtenerEstadoMensualidadesPorAnio(estudianteId, anio);

        // Assert
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());

        // Verificamos que la matrícula aparezca como Pagada
        MensualidadEstadoDto matriculaDto = resultado.stream()
                .filter(m -> m.getConcepto().contains("Matricula"))
                .findFirst()
                .orElse(null);

        assertNotNull(matriculaDto);
        assertEquals("Pagado", matriculaDto.getEstado());
        assertEquals(150.0, matriculaDto.getMonto());

        // Verificamos que una pensión (ej. Marzo) aparezca como Pendiente
        MensualidadEstadoDto pensionDto = resultado.stream()
                .filter(m -> m.getConcepto().contains("Pension Marzo"))
                .findFirst()
                .orElse(null);

        assertNotNull(pensionDto);
        assertEquals("Pendiente", pensionDto.getEstado());
        assertEquals(0.0, pensionDto.getMonto());
    }
}