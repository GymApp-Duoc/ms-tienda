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
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TiendaServiceImpl implements TiendaService {

    private final ProductoRepository productoRepo;
    private final VentaRepository ventaRepo;

    private final MiembroClient miembroClient;
    private final GamificacionClient gamificacionClient;
    private final NotificacionClient notificacionClient;

    @Override
    @Transactional
    public VentaResponseDTO procesarVenta(VentaRequestDTO dto) {
        log.info("Iniciando proceso de venta para el producto ID: {} y miembro ID: {}", dto.getProductoId(), dto.getMiembroId());

        Producto producto = productoRepo.findById(dto.getProductoId())
                .filter(Producto::isActivo)
                .orElseThrow(() -> new BusinessException("El producto solicitado no está disponible o no existe."));

        if (producto.getStock() < dto.getCantidad()) {
            throw new BusinessException("Stock insuficiente. Disponible: " + producto.getStock());
        }

        validarMiembroExterno(dto.getMiembroId());

        BigDecimal total = producto.getPrecio().multiply(BigDecimal.valueOf(dto.getCantidad()));

        producto.setStock(producto.getStock() - dto.getCantidad());
        productoRepo.save(producto);

        Venta venta = new Venta(null, producto.getId(), dto.getMiembroId(),
                dto.getCantidad(), total, LocalDateTime.now());
        Venta guardada = ventaRepo.save(venta);

        notificarSistemasExternos(dto.getMiembroId(), producto.getNombre());

        return mapearAResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarProductosActivos() {
        log.info("Consultando todos los productos activos en catálogo");
        return productoRepo.findAll().stream()
                .filter(Producto::isActivo)
                .toList();
    }

    @Override
    @Transactional
    public void reponerStock(Long id, int cantidad) {
        Producto p = productoRepo.findById(id)
                .orElseThrow(() -> new BusinessException("No se encontró el producto ID " + id + " para reponer stock"));

        p.setStock(p.getStock() + cantidad);
        productoRepo.save(p);
        log.info("Stock actualizado exitosamente para {}. Cantidad sumada: {}. Stock total: {}", p.getNombre(), cantidad, p.getStock());
    }

    @Override
    @Transactional
    public void eliminarProducto(Long id) {
        Producto p = productoRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Producto no encontrado para eliminar."));

        if (!ventaRepo.findByProductoId(id).isEmpty()) {
            p.setActivo(false);
            productoRepo.save(p);
            log.info("Producto ID {} desactivado (borrado lógico) debido a historial de ventas existente.", id);
        } else {
            productoRepo.delete(p);
            log.info("Producto ID {} eliminado físicamente del sistema.", id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venta> obtenerHistorial(Long miembroId) {
        log.info("Consultando historial de ventas para el miembro ID: {}", miembroId);
        return ventaRepo.findByMiembroIdOrderByFechaVentaDesc(miembroId);
    }

    @Override
    @Transactional
    public Producto guardarProducto(Producto producto) {
        producto.setActivo(true);
        log.info("Guardando nuevo producto en catálogo: {}", producto.getNombre());
        return productoRepo.save(producto);
    }

    private void validarMiembroExterno(Long id) {
        try {
            Boolean esValido = miembroClient.validarMiembro(id);
            if (esValido == null || !esValido) {
                throw new BusinessException("La venta no puede procesarse: El miembro no está autorizado o no existe.");
            }
        } catch (FeignException e) {
            log.error("Error de conexión con MS-MIEMBROS a través de Feign: {}", e.getMessage());
            throw new BusinessException("Servicio de validación de miembros temporalmente no disponible.");
        }
    }

    private void notificarSistemasExternos(Long miembroId, String nombreProducto) {
        try {
            Map<String, Object> evento = new HashMap<>();
            evento.put("miembroId", miembroId);
            evento.put("accion", "COMPRA_TIENDA");
            evento.put("puntosBase", 50);
            gamificacionClient.enviarEvento(evento);
            log.info("Evento de gamificación enviado para miembro {}", miembroId);
        } catch (Exception e) {
            log.warn("No se pudieron otorgar puntos al miembro {}: {}", miembroId, e.getMessage());
        }

        try {
            Map<String, Object> noti = new HashMap<>();
            noti.put("miembroId", miembroId);
            noti.put("titulo", "¡Compra Exitosa!");
            noti.put("mensaje", "Tu pedido de " + nombreProducto + " ya está registrado.");
            notificacionClient.enviarNotificacion(noti);
            log.info("Notificación de compra enviada exitosamente.");
        } catch (Exception e) {
            log.warn("Fallo el envío de notificación de compra para el miembro {}.", miembroId);
        }
    }

    private VentaResponseDTO mapearAResponse(Venta v) {
        return VentaResponseDTO.builder()
                .id(v.getId())
                .productoId(v.getProductoId())
                .miembroId(v.getMiembroId())
                .cantidad(v.getCantidad())
                .total(v.getTotal())
                .fechaVenta(v.getFechaVenta())
                .build();
    }


    // IMPLEMENTACIÓN DE LOS 5 REPORTES


    @Override
    @Transactional(readOnly = true)
    public List<Venta> reporteMiembroPorFecha(Long miembroId, LocalDate fecha) {
        log.info("Reporte 1: Ventas del miembro {} en la fecha {}", miembroId, fecha);
        LocalDateTime inicioDia = fecha.atStartOfDay();
        LocalDateTime finDia = fecha.atTime(23, 59, 59);
        return ventaRepo.findByMiembroIdAndFechaVentaBetween(miembroId, inicioDia, finDia);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venta> reporteVentasPorProducto(Long productoId) {
        log.info("Reporte 2: Historial de ventas del producto {}", productoId);
        return ventaRepo.findByProductoId(productoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venta> reporteMiembroEntreFechas(Long miembroId, LocalDate inicio, LocalDate fin) {
        log.info("Reporte 3: Ventas del miembro {} entre {} y {}", miembroId, inicio, fin);
        return ventaRepo.findByMiembroIdAndFechaVentaBetween(miembroId, inicio.atStartOfDay(), fin.atTime(23, 59, 59));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venta> reporteProductoEntreFechas(Long productoId, LocalDate inicio, LocalDate fin) {
        log.info("Reporte 4: Ventas del producto {} entre {} y {}", productoId, inicio, fin);
        return ventaRepo.findByProductoIdAndFechaVentaBetween(productoId, inicio.atStartOfDay(), fin.atTime(23, 59, 59));
    }

    @Override
    @Transactional(readOnly = true)
    public Integer reporteTotalUnidadesVendidas(Long productoId) {
        log.info("Reporte 5: Calculando total de unidades vendidas para producto {}", productoId);
        Integer total = ventaRepo.sumarUnidadesVendidasPorProducto(productoId);
        return total != null ? total : 0;
    }
}