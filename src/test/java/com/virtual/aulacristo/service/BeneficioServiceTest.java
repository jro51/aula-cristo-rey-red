package com.virtual.aulacristo.service;

import com.virtual.aulacristo.model.Beneficio;
import com.virtual.aulacristo.repository.BeneficioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

//AVCR-205: Probar aplicacion de becas y descuentos
@ExtendWith(MockitoExtension.class)
class BeneficioServiceTest {

    @Mock
    private BeneficioRepository beneficioRepository;

    @InjectMocks
    private PagoService pagoService; // Asumiendo que el cálculo está en PagoService

    @Test
    public void testCalcularMontoConMediaBeca() {
        Long estudianteId = 1L;
        String anioPeriodo = "2026";

        // Creamos un beneficio simulado de Media Beca (50%)
        Beneficio beneficio = new Beneficio();
        beneficio.setTipo("Media Beca (50%)");
        beneficio.setValor(50.0);
        beneficio.setAnioPeriodo(anioPeriodo);

        when(beneficioRepository.findByEstudianteIdAndAnioPeriodo(estudianteId, anioPeriodo))
                .thenReturn(Optional.of(beneficio));

        // Ejecutamos el cálculo (Monto base 150.0 menos el 50%)
        double montoFinal = pagoService.calcularMontoConBeneficio(estudianteId, anioPeriodo);

        // Verificamos que el monto final sea 75.0
        assertEquals(75.0, montoFinal);
    }
}