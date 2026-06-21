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
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TiendaServiceTest {

    @Mock private ProductoRepository productoRepo;
    @Mock private VentaRepository ventaRepo;
    @Mock private MiembroClient miembroClient;
    @Mock private GamificacionClient gamificacionClient;
    @Mock private NotificacionClient notificacionClient;

    @InjectMocks
    private TiendaServiceImpl service;

    private Producto producto;
    private Venta venta;

    @BeforeEach
    void setUp() {
        producto = new Producto(1L, "Proteina", "Desc", BigDecimal.valueOf(100), 10, true);
        venta = new Venta(1L, 1L, 100L, 2, BigDecimal.valueOf(200), LocalDateTime.now());
    }

    @Test
    void procesarVenta_Exito() {
        VentaRequestDTO request = new VentaRequestDTO(100L, 1L, 2);

        when(productoRepo.findById(1L)).thenReturn(Optional.of(producto));
        when(miembroClient.validarMiembro(100L)).thenReturn(true);
        when(ventaRepo.save(any(Venta.class))).thenReturn(venta);

        VentaResponseDTO response = service.procesarVenta(request);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(200), response.getTotal());
        assertEquals(8, producto.getStock());
        verify(ventaRepo, times(1)).save(any(Venta.class));
    }

    @Test
    void procesarVenta_LanzaExcepcionPorStockInsuficiente() {
        VentaRequestDTO request = new VentaRequestDTO(100L, 1L, 20);
        when(productoRepo.findById(1L)).thenReturn(Optional.of(producto));

        assertThrows(BusinessException.class, () -> service.procesarVenta(request));
        verify(ventaRepo, never()).save(any(Venta.class));
    }
}