package com.gymapp.ms_tienda.controller;

import com.gymapp.ms_tienda.dto.VentaRequestDTO;
import com.gymapp.ms_tienda.dto.VentaResponseDTO;
import com.gymapp.ms_tienda.model.Producto;
import com.gymapp.ms_tienda.model.Venta;
import com.gymapp.ms_tienda.repository.ProductoRepository;
import com.gymapp.ms_tienda.service.TiendaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tienda")
@RequiredArgsConstructor
public class TiendaController {

    private final TiendaService service;
    private final ProductoRepository productoRepo;

    @GetMapping("/productos")
    public List<Producto> listar() {
        return productoRepo.findAll().stream().filter(Producto::isActivo).toList();
    }

    @PostMapping("/checkout")
    public ResponseEntity<VentaResponseDTO> comprar(@Valid @RequestBody VentaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.procesarVenta(request));
    }

    @GetMapping("/historial/{miembroId}")
    public ResponseEntity<List<Venta>> verHistorial(@PathVariable Long miembroId) {
        return ResponseEntity.ok(service.obtenerHistorial(miembroId));
    }

    @DeleteMapping("/productos/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/productos/{id}/stock")
    public ResponseEntity<Void> reponer(@PathVariable Long id, @RequestParam int cantidad) {

        Producto p = productoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado para reponer stock"));

        p.setStock(p.getStock() + cantidad);
        productoRepo.save(p);
        return ResponseEntity.ok().build();
    }
}