package com.gymapp.ms_tienda.controller;

import com.gymapp.ms_tienda.dto.VentaRequestDTO;
import com.gymapp.ms_tienda.dto.VentaResponseDTO;
import com.gymapp.ms_tienda.model.Producto;
import com.gymapp.ms_tienda.model.Venta;
import com.gymapp.ms_tienda.service.TiendaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tienda")
@RequiredArgsConstructor
public class TiendaController {

    private final TiendaService service;

    @GetMapping("/productos")
    public ResponseEntity<List<Producto>> listar() {
        log.info("Petición REST: Listar productos disponibles");
        return ResponseEntity.ok(service.listarProductosActivos());
    }

    @PostMapping("/checkout")
    public ResponseEntity<VentaResponseDTO> comprar(@Valid @RequestBody VentaRequestDTO request) {
        log.info("Petición REST: Procesar compra para miembro ID {}", request.getMiembroId());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.procesarVenta(request));
    }


    @PostMapping("/productos")
    public ResponseEntity<Producto> crearProducto(@Valid @RequestBody Producto producto) {
        log.info("Petición REST: Crear nuevo producto '{}'", producto.getNombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardarProducto(producto));
    }

    @GetMapping("/historial/{miembroId}")
    public ResponseEntity<List<Venta>> verHistorial(@PathVariable Long miembroId) {
        log.info("Petición REST: Consultar historial para miembro {}", miembroId);
        return ResponseEntity.ok(service.obtenerHistorial(miembroId));
    }

    @DeleteMapping("/productos/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("Petición REST: Eliminar producto ID {}", id);
        service.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/productos/{id}/stock")
    public ResponseEntity<Void> reponer(@PathVariable Long id, @RequestParam int cantidad) {
        log.info("Petición REST: Reponer stock para producto ID {} (Cantidad: {})", id, cantidad);
        service.reponerStock(id, cantidad);
        return ResponseEntity.ok().build();
    }

}