package com.gymapp.ms_tienda.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymapp.ms_tienda.assembler.ProductoModelAssembler;
import com.gymapp.ms_tienda.assembler.VentaModelAssembler;
import com.gymapp.ms_tienda.dto.VentaRequestDTO;
import com.gymapp.ms_tienda.dto.VentaResponseDTO;
import com.gymapp.ms_tienda.model.Producto;
import com.gymapp.ms_tienda.service.TiendaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TiendaController.class)
@AutoConfigureMockMvc(addFilters = false)
class TiendaControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private TiendaService service;
    @MockitoBean private ProductoModelAssembler productoAssembler;
    @MockitoBean private VentaModelAssembler ventaAssembler;

    @Test
    void comprar_Retorna201() throws Exception {
        VentaRequestDTO request = new VentaRequestDTO(100L, 1L, 2);
        VentaResponseDTO response = new VentaResponseDTO(1L, 1L, 100L, 2, BigDecimal.valueOf(200), LocalDateTime.now());

        when(service.procesarVenta(any(VentaRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/tienda/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(200));
    }
}