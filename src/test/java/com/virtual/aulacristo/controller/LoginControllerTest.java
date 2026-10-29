package com.virtual.aulacristo.controller;

import org.junit.jupiter.api.Test;
import com.virtual.aulacristo.entity.Rol;
import com.virtual.aulacristo.entity.Usuario;
import com.virtual.aulacristo.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//AVCR-62: Realizar pruebas de inicio de sesion

@SpringBootTest
@AutoConfigureMockMvc
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @Test
    public void testLoginExitoso() throws Exception {
        Rol rol = new Rol();
        rol.setNombre("ADMINISTRADOR");

        Usuario usuarioMock = new Usuario();
        usuarioMock.setUsername("admin");
        usuarioMock.setPassword("1234");
        usuarioMock.setRol(rol);
        usuarioMock.setEstado(true);

        when(usuarioService.autenticar("admin", "1234")).thenReturn(usuarioMock);

        mockMvc.perform(post("/login")
                        .param("username", "admin")
                        .param("password", "1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(request().sessionAttribute("usuarioLogueado", usuarioMock));
    }

    @Test
    public void testLoginFallidoCredencialesInvalidas() throws Exception {
        when(usuarioService.autenticar("admin", "wrong"))
                .thenThrow(new IllegalArgumentException("Credenciales inválidas."));

        mockMvc.perform(post("/login")
                        .param("username", "admin")
                        .param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("login"));
    }
}