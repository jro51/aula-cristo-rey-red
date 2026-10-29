package com.virtual.aulacristo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.virtual.aulacristo.model.Grado;
import com.virtual.aulacristo.model.Nivel;
import com.virtual.aulacristo.model.Seccion;
import com.virtual.aulacristo.repository.GradoRepository;
import com.virtual.aulacristo.repository.NivelRepository;
import com.virtual.aulacristo.repository.SeccionRepository;

@ExtendWith(MockitoExtension.class)
class EstructuraAcademicaServiceTest {

    @Mock
    private NivelRepository nivelRepository;

    @Mock
    private GradoRepository gradoRepository;

    @Mock
    private SeccionRepository seccionRepository;

    private NivelService nivelService;
    private GradoService gradoService;
    private SeccionService seccionService;

    @BeforeEach
    void configurarPruebas() {

        nivelService = new NivelService(
            nivelRepository
        );

        gradoService = new GradoService(
            gradoRepository,
            nivelRepository
        );

        seccionService = new SeccionService(
            seccionRepository,
            gradoRepository
        );
    }

    @Test
    void debeRegistrarNivelCorrectamente() {

        Nivel nivel = new Nivel();
        nivel.setNombre("Secundaria");
        nivel.setDescripcion(
            "Nivel de educación secundaria"
        );

        when(
            nivelRepository.existsByNombreIgnoreCase(
                "Secundaria"
            )
        ).thenReturn(false);

        when(nivelRepository.save(any(Nivel.class)))
                .thenAnswer(invocacion ->
                    invocacion.getArgument(0)
                );

        Nivel resultado =
                nivelService.registrarNivel(nivel);

        assertNotNull(resultado);

        assertEquals(
            "Secundaria",
            resultado.getNombre()
        );

        assertEquals(
            true,
            resultado.isActivo()
        );

        verify(nivelRepository)
                .existsByNombreIgnoreCase(
                    "Secundaria"
                );

        verify(nivelRepository)
                .save(nivel);
    }

    @Test
    void debeRelacionarGradoConNivel() {

        Nivel nivelSecundaria = crearNivel();

        Grado nuevoGrado = new Grado();
        nuevoGrado.setNombre("1ro");
        nuevoGrado.setDescripcion(
            "Primer grado de secundaria"
        );

        Nivel referenciaNivel = new Nivel();
        referenciaNivel.setId(1L);

        nuevoGrado.setNivel(referenciaNivel);

        when(nivelRepository.findById(1L))
                .thenReturn(
                    Optional.of(nivelSecundaria)
                );

        when(
            gradoRepository
                .existsByNombreIgnoreCaseAndNivelId(
                    "1ro",
                    1L
                )
        ).thenReturn(false);

        when(gradoRepository.save(any(Grado.class)))
                .thenAnswer(invocacion ->
                    invocacion.getArgument(0)
                );

        Grado resultado =
                gradoService.registrarGrado(
                    nuevoGrado
                );

        assertNotNull(resultado.getNivel());

        assertSame(
            nivelSecundaria,
            resultado.getNivel()
        );

        assertEquals(
            "Secundaria",
            resultado.getNivel().getNombre()
        );

        assertEquals(
            "1ro",
            resultado.getNombre()
        );

        verify(nivelRepository).findById(1L);

        verify(gradoRepository)
                .save(nuevoGrado);
    }

    @Test
    void debeRelacionarSeccionConGradoYNivel() {

        Nivel nivelSecundaria = crearNivel();

        Grado primerGrado =
                crearGrado(nivelSecundaria);

        Seccion nuevaSeccion = new Seccion();
        nuevaSeccion.setNombre("A");
        nuevaSeccion.setDescripcion(
            "Sección A del primer grado"
        );

        Grado referenciaGrado = new Grado();
        referenciaGrado.setId(10L);

        nuevaSeccion.setGrado(
            referenciaGrado
        );

        when(gradoRepository.findById(10L))
                .thenReturn(
                    Optional.of(primerGrado)
                );

        when(
            seccionRepository
                .existsByNombreIgnoreCaseAndGradoId(
                    "A",
                    10L
                )
        ).thenReturn(false);

        when(
            seccionRepository.save(
                any(Seccion.class)
            )
        ).thenAnswer(invocacion ->
            invocacion.getArgument(0)
        );

        Seccion resultado =
                seccionService.registrarSeccion(
                    nuevaSeccion
                );

        assertNotNull(resultado.getGrado());

        assertEquals(
            "1ro",
            resultado.getGrado().getNombre()
        );

        assertNotNull(
            resultado.getGrado().getNivel()
        );

        assertEquals(
            "Secundaria",
            resultado
                .getGrado()
                .getNivel()
                .getNombre()
        );

        assertEquals(
            "A",
            resultado.getNombre()
        );

        verify(gradoRepository)
                .findById(10L);

        verify(seccionRepository)
                .save(nuevaSeccion);
    }

    @Test
    void debeListarGradosDeUnNivel() {

        Nivel nivel = crearNivel();
        Grado grado = crearGrado(nivel);

        when(nivelRepository.findById(1L))
                .thenReturn(Optional.of(nivel));

        when(gradoRepository.findByNivelId(1L))
                .thenReturn(List.of(grado));

        List<Grado> resultado =
                gradoService
                    .listarGradosPorNivel(1L);

        assertEquals(1, resultado.size());

        assertEquals(
            "Secundaria",
            resultado.get(0)
                    .getNivel()
                    .getNombre()
        );

        verify(gradoRepository)
                .findByNivelId(1L);
    }

    @Test
    void debeListarSeccionesDeUnGrado() {

        Nivel nivel = crearNivel();
        Grado grado = crearGrado(nivel);

        Seccion seccion = new Seccion();
        seccion.setId(100L);
        seccion.setNombre("A");
        seccion.setGrado(grado);
        seccion.setActivo(true);

        when(gradoRepository.findById(10L))
                .thenReturn(Optional.of(grado));

        when(
            seccionRepository.findByGradoId(10L)
        ).thenReturn(List.of(seccion));

        List<Seccion> resultado =
                seccionService
                    .listarSeccionesPorGrado(10L);

        assertEquals(1, resultado.size());

        assertEquals(
            "A",
            resultado.get(0).getNombre()
        );

        assertEquals(
            "1ro",
            resultado.get(0)
                    .getGrado()
                    .getNombre()
        );

        assertEquals(
            "Secundaria",
            resultado.get(0)
                    .getGrado()
                    .getNivel()
                    .getNombre()
        );

        verify(seccionRepository)
                .findByGradoId(10L);
    }

    @Test
    void noDebeRegistrarGradoSinNivel() {

        Grado grado = new Grado();
        grado.setNombre("1ro");
        grado.setNivel(null);

        IllegalArgumentException error =
                assertThrows(
                    IllegalArgumentException.class,
                    () -> gradoService
                            .registrarGrado(grado)
                );

        assertEquals(
            "Debe seleccionar un nivel para el grado.",
            error.getMessage()
        );

        verifyNoInteractions(nivelRepository);
        verifyNoInteractions(gradoRepository);
    }

    @Test
    void noDebeRegistrarSeccionSinGrado() {

        Seccion seccion = new Seccion();
        seccion.setNombre("A");
        seccion.setGrado(null);

        IllegalArgumentException error =
                assertThrows(
                    IllegalArgumentException.class,
                    () -> seccionService
                            .registrarSeccion(seccion)
                );

        assertEquals(
            "Debe seleccionar un grado para la sección.",
            error.getMessage()
        );

        verifyNoInteractions(gradoRepository);
        verifyNoInteractions(seccionRepository);
    }

    private Nivel crearNivel() {

        Nivel nivel = new Nivel();

        nivel.setId(1L);
        nivel.setNombre("Secundaria");
        nivel.setDescripcion(
            "Nivel de educación secundaria"
        );
        nivel.setActivo(true);

        return nivel;
    }

    private Grado crearGrado(Nivel nivel) {

        Grado grado = new Grado();

        grado.setId(10L);
        grado.setNombre("1ro");
        grado.setDescripcion(
            "Primer grado de secundaria"
        );
        grado.setNivel(nivel);
        grado.setActivo(true);

        return grado;
    }
}