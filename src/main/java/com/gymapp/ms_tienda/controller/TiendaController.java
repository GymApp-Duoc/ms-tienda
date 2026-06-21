package com.gymapp.ms_tienda.controller;

import com.gymapp.ms_tienda.assembler.ProductoModelAssembler;
import com.gymapp.ms_tienda.assembler.VentaModelAssembler;
import com.gymapp.ms_tienda.dto.VentaRequestDTO;
import com.gymapp.ms_tienda.dto.VentaResponseDTO;
import com.gymapp.ms_tienda.model.Producto;
import com.gymapp.ms_tienda.model.Venta;
import com.gymapp.ms_tienda.service.TiendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping("/api/tienda")
@RequiredArgsConstructor
@Tag(name = "Tienda", description = "Operaciones de catálogo, control de stock y ventas del gimnasio")
public class TiendaController {

    private final TiendaService service;
    private final ProductoModelAssembler productoAssembler;
    private final VentaModelAssembler ventaAssembler;

    @GetMapping("/productos")
    @Operation(summary = "Listar productos ", description = "Obtiene el catálogo de productos activos con enlaces hipermedia.")
    public ResponseEntity<CollectionModel<EntityModel<Producto>>> listar() {
        List<EntityModel<Producto>> productos = service.listarProductosActivos().stream()
                .map(productoAssembler::toModel).collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(productos, linkTo(methodOn(TiendaController.class).listar()).withSelfRel()));
    }

    @PostMapping("/checkout")
    @Operation(summary = "Procesar una compra", description = "Valida el stock, verifica al miembro mediante Feign Client y procesa la venta.")
    public ResponseEntity<EntityModel<VentaResponseDTO>> comprar(@Valid @RequestBody VentaRequestDTO request) {
        VentaResponseDTO response = service.procesarVenta(request);
        EntityModel<VentaResponseDTO> resource = EntityModel.of(response,
                linkTo(methodOn(TiendaController.class).verHistorial(request.getMiembroId())).withRel("ver-historial"));
        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    @PostMapping("/productos")
    @Operation(summary = "Crear nuevo producto")
    public ResponseEntity<EntityModel<Producto>> crearProducto(@Valid @RequestBody Producto producto) {
        Producto guardado = service.guardarProducto(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(productoAssembler.toModel(guardado));
    }

    @GetMapping("/historial/{miembroId}")
    @Operation(summary = "Ver historial")
    public ResponseEntity<CollectionModel<EntityModel<Venta>>> verHistorial(@PathVariable Long miembroId) {
        List<EntityModel<Venta>> ventas = service.obtenerHistorial(miembroId).stream()
                .map(ventaAssembler::toModel).collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(ventas, linkTo(methodOn(TiendaController.class).verHistorial(miembroId)).withSelfRel()));
    }

    @DeleteMapping("/productos/{id}")
    @Operation(summary = "Eliminar producto")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/productos/{id}/stock")
    @Operation(summary = "Reponer stock")
    public ResponseEntity<Void> reponer(@PathVariable Long id, @RequestParam int cantidad) {
        service.reponerStock(id, cantidad);
        return ResponseEntity.ok().build();
    }

    // ENDPOINTS DE LOS 5 REPORTES

    @GetMapping("/reportes/miembro/{miembroId}/fecha")
    @Operation(summary = "Reporte 1: Ventas de miembro en fecha")
    public ResponseEntity<CollectionModel<EntityModel<Venta>>> reporte1(
            @PathVariable Long miembroId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        List<EntityModel<Venta>> ventas = service.reporteMiembroPorFecha(miembroId, fecha).stream()
                .map(ventaAssembler::toModel).collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(ventas));
    }

    @GetMapping("/reportes/producto/{productoId}")
    @Operation(summary = "Reporte 2: Ventas por producto")
    public ResponseEntity<CollectionModel<EntityModel<Venta>>> reporte2(@PathVariable Long productoId) {
        List<EntityModel<Venta>> ventas = service.reporteVentasPorProducto(productoId).stream()
                .map(ventaAssembler::toModel).collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(ventas));
    }

    @GetMapping("/reportes/miembro/{miembroId}/rango")
    @Operation(summary = "Reporte 3: Ventas de miembro entre fechas")
    public ResponseEntity<CollectionModel<EntityModel<Venta>>> reporte3(
            @PathVariable Long miembroId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        List<EntityModel<Venta>> ventas = service.reporteMiembroEntreFechas(miembroId, inicio, fin).stream()
                .map(ventaAssembler::toModel).collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(ventas));
    }

    @GetMapping("/reportes/producto/{productoId}/rango")
    @Operation(summary = "Reporte 4: Ventas de producto entre fechas")
    public ResponseEntity<CollectionModel<EntityModel<Venta>>> reporte4(
            @PathVariable Long productoId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        List<EntityModel<Venta>> ventas = service.reporteProductoEntreFechas(productoId, inicio, fin).stream()
                .map(ventaAssembler::toModel).collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(ventas));
    }

    @GetMapping("/reportes/producto/{productoId}/total-unidades")
    @Operation(summary = "Reporte 5: Total unidades vendidas")
    public ResponseEntity<EntityModel<String>> reporte5(@PathVariable Long productoId) {
        Integer total = service.reporteTotalUnidadesVendidas(productoId);
        String mensaje = "Se han vendido un total de " + total + " unidades del producto ID: " + productoId;
        return ResponseEntity.ok(EntityModel.of(mensaje, linkTo(methodOn(TiendaController.class).reporte2(productoId)).withRel("ver-detalle")));
    }
}