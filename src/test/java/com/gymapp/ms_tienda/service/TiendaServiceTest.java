package com.gymapp.ms_tienda.service;

import com.gymapp.ms_tienda.client.GamificacionClient;
import com.gymapp.ms_tienda.client.MiembroClient;
import com.gymapp.ms_tienda.client.NotificacionClient;
import com.gymapp.ms_tienda.dto.VentaRequestDTO;
import com.gymapp.ms_tienda.dto.VentaResponseDTO;
import com.gymapp.ms_tienda.exception.BusinessException;
import com.gymapp.ms_tienda.model.Producto;
import com.gymapp.ms_tienda.model.Venta;
import com.gymapp.ms_tienda.repository.ProductoRepository;
import com.gymapp.ms_tienda.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TiendaServiceTest {

    @Mock private ProductoRepository productoRepo;
    @Mock private VentaRepository ventaRepo;
    @Mock private MiembroClient miembroClient;
    @Mock private GamificacionClient gamificacionClient;
    @Mock private NotificacionClient notificacionClient;

    @InjectMocks
    private TiendaServiceImpl tiendaService;

    private Producto producto;
    private VentaRequestDTO ventaRequest;

    @BeforeEach
    void setUp() {
        producto = new Producto(1L, "Proteína", "Whey", BigDecimal.valueOf(1000), 10, true);
        ventaRequest = new VentaRequestDTO(100L, 1L, 2);
    }

    @Test
    void testProcesarVenta_Exito() {
        when(productoRepo.findById(1L)).thenReturn(Optional.of(producto));
        when(miembroClient.validarMiembro(100L)).thenReturn(true);
        when(ventaRepo.save(any(Venta.class))).thenReturn(new Venta(1L, 1L, 100L, 2, BigDecimal.valueOf(2000), null));

        VentaResponseDTO response = tiendaService.procesarVenta(ventaRequest);

        assertNotNull(response);
        assertEquals(8, producto.getStock()); // Verifica descuento de stock
        verify(gamificacionClient, times(1)).enviarEvento(any());
    }

    @Test
    void testProcesarVenta_StockInsuficiente() {
        ventaRequest.setCantidad(20);
        when(productoRepo.findById(1L)).thenReturn(Optional.of(producto));

        BusinessException ex = assertThrows(BusinessException.class, () -> tiendaService.procesarVenta(ventaRequest));
        assertTrue(ex.getMessage().contains("Stock insuficiente"));
    }
}