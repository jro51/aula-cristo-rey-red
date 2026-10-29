package com.virtual.aulacristo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

import com.virtual.aulacristo.entity.Rol;
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

@ExtendWith(MockitoExtension.class)
class AsignacionDocenteServiceTest {

    @Mock
    private AsignacionDocenteRepository asignacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private GradoRepository gradoRepository;

    @Mock
    private SeccionRepository seccionRepository;

    @InjectMocks
    private AsignacionDocenteService asignacionService;

    private Usuario docente;
    private Curso curso;
    private Grado grado;
    private Seccion seccion;

    @BeforeEach
    void prepararDatos() {

        Rol rolDocente = new Rol();
        rolDocente.setId(2L);
        rolDocente.setNombre("DOCENTE");

        docente = new Usuario();
        docente.setIdUsuario(10L);
        docente.setUsername("docente.matematica");
        docente.setEstado(true);
        docente.setRol(rolDocente);

        grado = new Grado();
        grado.setId(20L);
        grado.setNombre("Primero");
        grado.setActivo(true);

        curso = new Curso();
        curso.setId(30L);
        curso.setCodigo("MAT-001");
        curso.setNombre("Matemática");
        curso.setHorasSemanales(5);
        curso.setActivo(true);
        curso.setGrado(grado);

        seccion = new Seccion();
        seccion.setId(40L);
        seccion.setNombre("A");
        seccion.setActivo(true);
        seccion.setGrado(grado);
    }

    @Test
    void debeAsignarDocenteCorrectamente() {

        prepararRepositorios();

        when(asignacionRepository.save(
                any(AsignacionDocente.class)
        )).thenAnswer(invocacion -> {

            AsignacionDocente asignacion =
                    invocacion.getArgument(0);

            asignacion.setId(1L);

            return asignacion;
        });

        AsignacionDocente resultado =
                asignacionService.asignarDocente(
                    docente.getIdUsuario(),
                    curso.getId(),
                    grado.getId(),
                    seccion.getId()
                );

        assertEquals(1L, resultado.getId());
        assertSame(docente, resultado.getDocente());
        assertSame(curso, resultado.getCurso());
        assertSame(grado, resultado.getGrado());
        assertSame(seccion, resultado.getSeccion());
        assertTrue(resultado.isActivo());

        verify(asignacionRepository).save(
            any(AsignacionDocente.class)
        );
    }

    @Test
    void debeRechazarUsuarioQueNoSeaDocente() {

        Rol rolAdministrador = new Rol();
        rolAdministrador.setId(1L);
        rolAdministrador.setNombre("ADMINISTRADOR");

        docente.setRol(rolAdministrador);

        prepararRepositorios();

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> asignacionService.asignarDocente(
                docente.getIdUsuario(),
                curso.getId(),
                grado.getId(),
                seccion.getId()
            )
        );

        assertEquals(
            "El usuario seleccionado no tiene el rol DOCENTE.",
            error.getMessage()
        );

        verify(asignacionRepository, never()).save(
            any(AsignacionDocente.class)
        );
    }

    @Test
    void debeRechazarDocenteInactivo() {

        docente.setEstado(false);

        prepararRepositorios();

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> asignacionService.asignarDocente(
                docente.getIdUsuario(),
                curso.getId(),
                grado.getId(),
                seccion.getId()
            )
        );

        assertEquals(
            "El docente seleccionado está inactivo.",
            error.getMessage()
        );

        verify(asignacionRepository, never()).save(
            any(AsignacionDocente.class)
        );
    }

    @Test
    void debeRechazarAsignacionDuplicada() {

        prepararRepositorios();

        when(
            asignacionRepository
                .existsByDocente_IdUsuarioAndCurso_IdAndGrado_IdAndSeccion_Id(
                    docente.getIdUsuario(),
                    curso.getId(),
                    grado.getId(),
                    seccion.getId()
                )
        ).thenReturn(true);

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> asignacionService.asignarDocente(
                docente.getIdUsuario(),
                curso.getId(),
                grado.getId(),
                seccion.getId()
            )
        );

        assertTrue(
            error.getMessage().contains(
                "ya tiene registrado este curso"
            )
        );

        verify(asignacionRepository, never()).save(
            any(AsignacionDocente.class)
        );
    }

    @Test
    void debeRechazarCursoQuePerteneceAOtroGrado() {

        Grado otroGrado = new Grado();
        otroGrado.setId(99L);
        otroGrado.setNombre("Segundo");
        otroGrado.setActivo(true);

        curso.setGrado(otroGrado);

        prepararRepositorios();

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> asignacionService.asignarDocente(
                docente.getIdUsuario(),
                curso.getId(),
                grado.getId(),
                seccion.getId()
            )
        );

        assertEquals(
            "El curso seleccionado no pertenece al grado indicado.",
            error.getMessage()
        );

        verify(asignacionRepository, never()).save(
            any(AsignacionDocente.class)
        );
    }

    @Test
    void debeRechazarSeccionQuePerteneceAOtroGrado() {

        Grado otroGrado = new Grado();
        otroGrado.setId(99L);
        otroGrado.setNombre("Segundo");
        otroGrado.setActivo(true);

        seccion.setGrado(otroGrado);

        prepararRepositorios();

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> asignacionService.asignarDocente(
                docente.getIdUsuario(),
                curso.getId(),
                grado.getId(),
                seccion.getId()
            )
        );

        assertEquals(
            "La sección seleccionada no pertenece al grado indicado.",
            error.getMessage()
        );

        verify(asignacionRepository, never()).save(
            any(AsignacionDocente.class)
        );
    }

    @Test
    void debeRechazarCursoConOtroDocenteActivo() {

        prepararRepositorios();

        when(
            asignacionRepository
                .existsByCurso_IdAndGrado_IdAndSeccion_IdAndActivoTrue(
                    curso.getId(),
                    grado.getId(),
                    seccion.getId()
                )
        ).thenReturn(true);

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> asignacionService.asignarDocente(
                docente.getIdUsuario(),
                curso.getId(),
                grado.getId(),
                seccion.getId()
            )
        );

        assertTrue(
            error.getMessage().contains(
                "ya tiene un docente activo"
            )
        );

        verify(asignacionRepository, never()).save(
            any(AsignacionDocente.class)
        );
    }

    @Test
    void debeConsultarCursosActivosDelDocente() {

        AsignacionDocente asignacion =
                crearAsignacionValida();

        when(
            asignacionRepository
                .findByDocente_IdUsuarioAndActivoTrueOrderByCurso_NombreAsc(
                    docente.getIdUsuario()
                )
        ).thenReturn(List.of(asignacion));

        List<AsignacionDocente> resultado =
                asignacionService.listarActivasPorDocente(
                    docente.getIdUsuario()
                );

        assertEquals(1, resultado.size());
        assertEquals(
            "Matemática",
            resultado.get(0).getCurso().getNombre()
        );

        assertEquals(
            "A",
            resultado.get(0).getSeccion().getNombre()
        );

        verify(asignacionRepository)
            .findByDocente_IdUsuarioAndActivoTrueOrderByCurso_NombreAsc(
                docente.getIdUsuario()
            );
    }

    @Test
    void debeDesactivarAsignacion() {

        AsignacionDocente asignacion =
                crearAsignacionValida();

        when(
            asignacionRepository.findById(1L)
        ).thenReturn(Optional.of(asignacion));

        when(
            asignacionRepository.save(asignacion)
        ).thenReturn(asignacion);

        AsignacionDocente resultado =
                asignacionService.cambiarEstado(
                    1L,
                    false
                );

        assertFalse(resultado.isActivo());

        verify(asignacionRepository).save(asignacion);
    }

    @Test
    void debeImpedirReactivarSiExisteOtroDocenteActivo() {

        AsignacionDocente asignacion =
                crearAsignacionValida();

        asignacion.setActivo(false);

        when(
            asignacionRepository.findById(1L)
        ).thenReturn(Optional.of(asignacion));

        when(
            asignacionRepository
                .existsByCurso_IdAndGrado_IdAndSeccion_IdAndActivoTrueAndIdNot(
                    curso.getId(),
                    grado.getId(),
                    seccion.getId(),
                    asignacion.getId()
                )
        ).thenReturn(true);

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> asignacionService.cambiarEstado(
                1L,
                true
            )
        );

        assertTrue(
            error.getMessage().contains(
                "ya tiene otro docente asignado"
            )
        );

        assertFalse(asignacion.isActivo());

        verify(asignacionRepository, never()).save(
            any(AsignacionDocente.class)
        );
    }

    private void prepararRepositorios() {

        when(
            usuarioRepository.findById(
                docente.getIdUsuario()
            )
        ).thenReturn(Optional.of(docente));

        when(
            cursoRepository.findById(
                curso.getId()
            )
        ).thenReturn(Optional.of(curso));

        when(
            gradoRepository.findById(
                grado.getId()
            )
        ).thenReturn(Optional.of(grado));

        when(
            seccionRepository.findById(
                seccion.getId()
            )
        ).thenReturn(Optional.of(seccion));
    }

    private AsignacionDocente crearAsignacionValida() {

        AsignacionDocente asignacion =
                new AsignacionDocente();

        asignacion.setId(1L);
        asignacion.setDocente(docente);
        asignacion.setCurso(curso);
        asignacion.setGrado(grado);
        asignacion.setSeccion(seccion);
        asignacion.setActivo(true);

        return asignacion;
    }
}