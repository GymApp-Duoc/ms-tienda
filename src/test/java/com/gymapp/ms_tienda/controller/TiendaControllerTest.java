package com.gymapp.ms_tienda.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymapp.ms_tienda.dto.VentaRequestDTO;
import com.gymapp.ms_tienda.dto.VentaResponseDTO;
import com.gymapp.ms_tienda.service.TiendaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TiendaController.class)
@AutoConfigureMockMvc(addFilters = false) // Desactiva seguridad para testear solo el controller
public class TiendaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TiendaService tiendaService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testComprar_DebeRetornar201() throws Exception {
        VentaRequestDTO request = new VentaRequestDTO(1L, 1L, 2);
        VentaResponseDTO response = new VentaResponseDTO(1L, 1L, 1L, 2, BigDecimal.valueOf(2000), LocalDateTime.now());

        when(tiendaService.procesarVenta(any())).thenReturn(response);

        mockMvc.perform(post("/api/tienda/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()); // Validamos el 201 Created correcto
    }
}