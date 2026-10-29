package com.virtual.aulacristo.controller;
import com.virtual.aulacristo.service.EstudianteService;
import com.virtual.aulacristo.service.PagoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PagoController.class)

//AVCR-193: Probar registro y consulta de pago
class PagoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PagoService pagoService;

    @MockitoBean
    private EstudianteService estudianteService;

    @Test
    public void testListarPagosView() throws Exception {
        mockMvc.perform(get("/pagos"))
                .andExpect(status().isOk())
                .andExpect(view().name("pagos/pagos"));
    }
}