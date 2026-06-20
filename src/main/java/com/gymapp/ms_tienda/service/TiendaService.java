package com.gymapp.ms_tienda.service;

import com.gymapp.ms_tienda.dto.VentaRequestDTO;
import com.gymapp.ms_tienda.dto.VentaResponseDTO;
import com.gymapp.ms_tienda.model.Producto;
import com.gymapp.ms_tienda.model.Venta;

import java.time.LocalDate;
import java.util.List;

public interface TiendaService {
    VentaResponseDTO procesarVenta(VentaRequestDTO dto);
    List<Venta> obtenerHistorial(Long miembroId);
    void eliminarProducto(Long id);
    List<Producto> listarProductosActivos();
    void reponerStock(Long id, int cantidad);
    Producto guardarProducto(Producto producto);


    List<Venta> reporteMiembroPorFecha(Long miembroId, LocalDate fecha);
    List<Venta> reporteVentasPorProducto(Long productoId);
    List<Venta> reporteMiembroEntreFechas(Long miembroId, LocalDate inicio, LocalDate fin);
    List<Venta> reporteProductoEntreFechas(Long productoId, LocalDate inicio, LocalDate fin);
    Integer reporteTotalUnidadesVendidas(Long productoId);
}
