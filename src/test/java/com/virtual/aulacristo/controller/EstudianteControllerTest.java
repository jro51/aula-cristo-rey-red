package com.virtual.aulacristo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EstudianteControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testAccesoDenegadoSinSesion() throws Exception {
        // Intentar entrar al formulario de estudiantes sin iniciar sesión
        mockMvc.perform(get("/estudiantes/nuevo"))
                .andExpect(status().is3xxRedirection()) // Debe redirigir
                .andExpect(redirectedUrl("/?error=No autorizado")); // O a donde envíe tu validación
    }

    @Test
    public void testAccesoPermitidoConRolAdministrador() throws Exception {
        // Simular una petición inyectando atributos en la HttpSession
        mockMvc.perform(get("/estudiantes/nuevo")
                        .sessionAttr("usuarioLogueado", new Object())
                        .sessionAttr("rolUsuario", "ADMINISTRADOR"))
                .andExpect(status().isOk()); // Debe dejar pasar (HTTP 200)
    }

    @Test
    public void testAccesoPermitidoConRolDocente() throws Exception {
        mockMvc.perform(get("/estudiantes/nuevo")
                        .sessionAttr("usuarioLogueado", new Object())
                        .sessionAttr("rolUsuario", "DOCENTE"))
                .andExpect(status().isOk()); // Debe dejar pasar (HTTP 200)
    }
}