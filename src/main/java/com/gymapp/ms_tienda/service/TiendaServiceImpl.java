package com.gymapp.ms_tienda.service;

import com.gymapp.ms_tienda.dto.VentaRequestDTO;
import com.gymapp.ms_tienda.dto.VentaResponseDTO;
import com.gymapp.ms_tienda.model.Producto;
import com.gymapp.ms_tienda.model.Venta;
import com.gymapp.ms_tienda.repository.ProductoRepository;
import com.gymapp.ms_tienda.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TiendaServiceImpl implements TiendaService {

    private final ProductoRepository productoRepo;
    private final VentaRepository ventaRepo;
    private final RestTemplate restTemplate;

    @Value("${ms.miembros.url}")
    private String miembrosUrl;


    @Value("${ms.gamificacion.url}")
    private String gamificacionUrl;

    @Override
    @Transactional
    public VentaResponseDTO procesarVenta(VentaRequestDTO dto) {

        Producto producto = productoRepo.findById(dto.getProductoId())
                .filter(Producto::isActivo)
                .orElseThrow(() -> new RuntimeException("Producto no disponible o inexistente"));

        if (producto.getStock() < dto.getCantidad()) {
            throw new RuntimeException("Sin stock suficiente para " + producto.getNombre());
        }


        validarMiembroExterno(dto.getMiembroId());


        BigDecimal total = producto.getPrecio().multiply(BigDecimal.valueOf(dto.getCantidad()));


        producto.setStock(producto.getStock() - dto.getCantidad());
        productoRepo.save(producto);


        Venta venta = new Venta(null, producto.getId(), dto.getMiembroId(),
                dto.getCantidad(), total, LocalDateTime.now());
        Venta guardada = ventaRepo.save(venta);


        try {
            Map<String, Object> evento = new HashMap<>();
            evento.put("miembroId", dto.getMiembroId());
            evento.put("accion", "COMPRA_TIENDA");
            evento.put("puntosBase", 50);

            restTemplate.postForObject(gamificacionUrl + "/api/gamificacion/eventos", evento, Object.class);
            System.out.println("Evento enviado a Gamificación exitosamente.");
        } catch (Exception e) {

            System.err.println("Aviso: No se pudieron enviar los puntos a Gamificación. " + e.getMessage());
        }

        return VentaResponseDTO.builder()
                .ventaId(guardada.getId())
                .producto(producto.getNombre())
                .total(total)
                .estado("EXITOSA")
                .build();
    }

    @Override
    @Transactional
    public void eliminarProducto(Long id) {
        Producto p = productoRepo.findById(id).orElseThrow();
        if (!ventaRepo.findByProductoId(id).isEmpty()) {
            p.setActivo(false);
            productoRepo.save(p);
        } else {
            productoRepo.delete(p);
        }
    }

    private void validarMiembroExterno(Long id) {
        try {
            Boolean ok = restTemplate.getForObject(miembrosUrl + "/api/miembros/validar/" + id, Boolean.class);
            if (ok == null || !ok) throw new RuntimeException("Miembro no autorizado");
        } catch (Exception e) {
            throw new RuntimeException("Error al conectar con MS-MIEMBROS");
        }
    }

    @Override
    public List<Venta> obtenerHistorial(Long miembroId) {
        return ventaRepo.findByMiembroIdOrderByFechaVentaDesc(miembroId);
    }
}