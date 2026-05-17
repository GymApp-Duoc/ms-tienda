package com.gymapp.ms_tienda.service;

import com.gymapp.ms_tienda.dto.VentaRequestDTO;
import com.gymapp.ms_tienda.dto.VentaResponseDTO;
import com.gymapp.ms_tienda.model.Producto;
import com.gymapp.ms_tienda.model.Venta;
import java.util.List;

public interface TiendaService {
    VentaResponseDTO procesarVenta(VentaRequestDTO dto);
    List<Venta> obtenerHistorial(Long miembroId);
    void eliminarProducto(Long id);

    List<Producto> listarProductosActivos();
    void reponerStock(Long id, int cantidad);
    Producto guardarProducto(Producto producto);
}