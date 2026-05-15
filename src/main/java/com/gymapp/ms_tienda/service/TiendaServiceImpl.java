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

    // Clientes Feign
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

    private void validarMiembroExterno(Long id) {
        try {
            Boolean esValido = miembroClient.validarMiembro(id);
            if (esValido == null || !esValido) {
                throw new BusinessException("La venta no puede procesarse: El miembro no está autorizado.");
            }
        } catch (FeignException e) {
            log.error("Error de conexión con MS-MIEMBROS: {}", e.getMessage());
            throw new BusinessException("Servicio de validación de miembros no disponible.");
        }
    }

    private void notificarSistemasExternos(Long miembroId, String nombreProducto) {

        try {
            Map<String, Object> evento = new HashMap<>();
            evento.put("miembroId", miembroId);
            evento.put("accion", "COMPRA_TIENDA");
            evento.put("puntosBase", 50);
            gamificacionClient.enviarEvento(evento);
            log.info("Puntos de gamificación solicitados para el miembro {}", miembroId);
        } catch (Exception e) {
            log.warn("No se pudieron otorgar puntos al miembro {}: {}", miembroId, e.getMessage());
        }


        try {
            Map<String, Object> noti = new HashMap<>();
            noti.put("miembroId", miembroId);
            noti.put("titulo", "¡Compra Exitosa!");
            noti.put("mensaje", "Tu pedido de " + nombreProducto + " está listo para retiro.");
            notificacionClient.enviarNotificacion(noti);
            log.info("Notificación de compra despachada.");
        } catch (Exception e) {
            log.warn("Fallo al enviar notificación de compra.");
        }
    }

    @Override
    @Transactional
    public void eliminarProducto(Long id) {
        Producto p = productoRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Producto no encontrado para eliminar."));

        if (!ventaRepo.findByProductoId(id).isEmpty()) {
            p.setActivo(false);
            productoRepo.save(p);
            log.info("Producto ID {} desactivado (borrado lógico) por tener historial de ventas.", id);
        } else {
            productoRepo.delete(p);
            log.info("Producto ID {} eliminado físicamente.", id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venta> obtenerHistorial(Long miembroId) {
        return ventaRepo.findByMiembroIdOrderByFechaVentaDesc(miembroId);
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
}