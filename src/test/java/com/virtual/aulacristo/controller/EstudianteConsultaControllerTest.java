package com.virtual.aulacristo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import com.virtual.aulacristo.model.Estudiante;
import com.virtual.aulacristo.service.EstudianteService;

import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
class EstudianteConsultaControllerTest {

    @Mock
    private EstudianteService estudianteService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @InjectMocks
    private EstudianteController estudianteController;

    @Test
    void listadoSinSesionDebeRedirigirAlLogin() {

        when(session.getAttribute("usuarioLogueado"))
                .thenReturn(null);

        String resultado =
                estudianteController.listarEstudiantes(
                    null,
                    null,
                    null,
                    null,
                    session,
                    model
                );

        assertEquals("redirect:/login", resultado);
        verifyNoInteractions(estudianteService);
    }

    @Test
    void listadoConRolNoAutorizadoDebeDenegarAcceso() {

        when(session.getAttribute("usuarioLogueado"))
                .thenReturn(new Object());

        when(session.getAttribute("rolUsuario"))
                .thenReturn("ESTUDIANTE");

        String resultado =
                estudianteController.listarEstudiantes(
                    null,
                    null,
                    null,
                    null,
                    session,
                    model
                );

        assertEquals(
            "redirect:/?error=No autorizado",
            resultado
        );

        verifyNoInteractions(estudianteService);
    }

    @Test
    void administradorDebeVisualizarElListado() {

        Estudiante estudiante = crearEstudiante();

        when(session.getAttribute("usuarioLogueado"))
                .thenReturn(new Object());

        when(session.getAttribute("rolUsuario"))
                .thenReturn("ADMINISTRADOR");

        when(estudianteService.filtrarEstudiantes(
                null,
                null,
                null,
                null
        )).thenReturn(List.of(estudiante));

        String resultado =
                estudianteController.listarEstudiantes(
                    null,
                    null,
                    null,
                    null,
                    session,
                    model
                );

        assertEquals("estudiantes/lista", resultado);

        verify(model).addAttribute(
            "estudiantes",
            List.of(estudiante)
        );
    }

    @Test
    void docenteDebeVisualizarFichaPorId() {

        when(session.getAttribute("usuarioLogueado"))
                .thenReturn(new Object());

        when(session.getAttribute("rolUsuario"))
                .thenReturn("DOCENTE");

        String resultado =
                estudianteController.mostrarFichaPorId(
                        1L,
                        session,
                        model
                );

        // Ahora esperamos que lo redirija por no estar autorizado
        assertEquals("redirect:/?error=No autorizado", resultado);
        verifyNoInteractions(estudianteService);
    }

    @Test
    void endpointSinSesionDebeResponder401() {

        when(session.getAttribute("usuarioLogueado"))
                .thenReturn(null);

        ResponseEntity<?> respuesta =
                estudianteController.buscarEstudiante(
                    "73829182",
                    session
                );

        assertEquals(
            HttpStatus.UNAUTHORIZED,
            respuesta.getStatusCode()
        );

        verifyNoInteractions(estudianteService);
    }

    @Test
    void endpointConRolNoAutorizadoDebeResponder403() {

        when(session.getAttribute("usuarioLogueado"))
                .thenReturn(new Object());

        when(session.getAttribute("rolUsuario"))
                .thenReturn("ESTUDIANTE");

        ResponseEntity<?> respuesta =
                estudianteController.buscarEstudiante(
                    "73829182",
                    session
                );

        assertEquals(
            HttpStatus.FORBIDDEN,
            respuesta.getStatusCode()
        );

        verifyNoInteractions(estudianteService);
    }

    @Test
    void endpointAutorizadoDebeResponder200() {

        Estudiante estudiante = crearEstudiante();

        when(session.getAttribute("usuarioLogueado"))
                .thenReturn(new Object());

        when(session.getAttribute("rolUsuario"))
                .thenReturn("ADMINISTRADOR");

        when(estudianteService.buscarPorCodigoODni("73829182"))
                .thenReturn(estudiante);

        ResponseEntity<?> respuesta =
                estudianteController.buscarEstudiante(
                    "73829182",
                    session
                );

        assertEquals(
            HttpStatus.OK,
            respuesta.getStatusCode()
        );

        assertSame(
            estudiante,
            respuesta.getBody()
        );
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