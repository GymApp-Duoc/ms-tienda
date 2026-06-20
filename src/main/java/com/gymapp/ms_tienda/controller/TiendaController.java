package com.gymapp.ms_tienda.controller;

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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping("/api/tienda")
@RequiredArgsConstructor
@Tag(name = "Tienda", description = "Operaciones de catálogo, control de stock y ventas del gimnasio")
public class TiendaController {

    private final TiendaService service;

    @GetMapping("/productos")
    @Operation(summary = "Listar productos ", description = "Obtiene el catálogo de productos activos con enlaces hipermedia.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Catálogo obtenido exitosamente")})
    public ResponseEntity<CollectionModel<EntityModel<Producto>>> listar() {
        log.info("Petición REST: Listar productos disponibles");
        List<EntityModel<Producto>> productos = service.listarProductosActivos().stream()
                .map(p -> EntityModel.of(p,
                        linkTo(methodOn(TiendaController.class).listar()).withSelfRel(),
                        linkTo(methodOn(TiendaController.class).eliminar(p.getId())).withRel("eliminar-producto")))
                .toList();

        return ResponseEntity.ok(CollectionModel.of(productos, linkTo(methodOn(TiendaController.class).listar()).withSelfRel()));
    }

    @PostMapping("/checkout")
    @Operation(summary = "Procesar una compra", description = "Valida el stock, verifica al miembro mediante Feign Client y procesa la venta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Venta procesada con éxito"),
            @ApiResponse(responseCode = "400", description = "Error de validación o regla de negocio (ej. sin stock)"),
            @ApiResponse(responseCode = "500", description = "Fallo de comunicación con microservicios")
    })
    public ResponseEntity<EntityModel<VentaResponseDTO>> comprar(@Valid @RequestBody VentaRequestDTO request) {
        log.info("Petición REST: Procesar compra para miembro ID {}", request.getMiembroId());
        VentaResponseDTO response = service.procesarVenta(request);

        EntityModel<VentaResponseDTO> resource = EntityModel.of(response,
                linkTo(methodOn(TiendaController.class).comprar(request)).withSelfRel(),
                linkTo(methodOn(TiendaController.class).verHistorial(request.getMiembroId())).withRel("ver-historial"));

        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    @PostMapping("/productos")
    @Operation(summary = "Crear nuevo producto", description = "Registra un nuevo producto en la tienda.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto creado"),
            @ApiResponse(responseCode = "400", description = "Validación fallida")
    })
    public ResponseEntity<EntityModel<Producto>> crearProducto(@Valid @RequestBody Producto producto) {
        log.info("Petición REST: Crear nuevo producto '{}'", producto.getNombre());
        Producto guardado = service.guardarProducto(producto);

        return ResponseEntity.status(HttpStatus.CREATED).body(EntityModel.of(guardado,
                linkTo(methodOn(TiendaController.class).listar()).withRel("catalogo")));
    }

    @GetMapping("/historial/{miembroId}")
    @Operation(summary = "Ver historial", description = "Obtiene las transacciones de un miembro.")
    public ResponseEntity<CollectionModel<EntityModel<Venta>>> verHistorial(@PathVariable Long miembroId) {
        log.info("Petición REST: Consultar historial para miembro {}", miembroId);
        List<EntityModel<Venta>> ventas = service.obtenerHistorial(miembroId).stream()
                .map(v -> EntityModel.of(v, linkTo(methodOn(TiendaController.class).verHistorial(miembroId)).withSelfRel()))
                .toList();

        return ResponseEntity.ok(CollectionModel.of(ventas, linkTo(methodOn(TiendaController.class).verHistorial(miembroId)).withSelfRel()));
    }

    @DeleteMapping("/productos/{id}")
    @Operation(summary = "Eliminar producto", description = "Borrado lógico si tiene historial, físico si no.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Producto eliminado/desactivado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("Petición REST: Eliminar producto ID {}", id);
        service.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/productos/{id}/stock")
    @Operation(summary = "Reponer stock", description = "Suma cantidad al inventario.")
    public ResponseEntity<Void> reponer(@PathVariable Long id, @RequestParam int cantidad) {
        log.info("Petición REST: Reponer stock para producto ID {} (Cantidad: {})", id, cantidad);
        service.reponerStock(id, cantidad);
        return ResponseEntity.ok().build();
    }

    // ENDPOINTS DE LOS 5 REPORTES


    @GetMapping("/reportes/miembro/{miembroId}/fecha")
    @Operation(summary = "Reporte 1: Ventas de miembro en fecha", description = "Devuelve las compras de un miembro en un día específico (Formato YYYY-MM-DD).")
    public ResponseEntity<CollectionModel<EntityModel<Venta>>> reporte1(
            @PathVariable Long miembroId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        List<EntityModel<Venta>> ventas = service.reporteMiembroPorFecha(miembroId, fecha).stream()
                .map(v -> EntityModel.of(v, linkTo(methodOn(TiendaController.class).verHistorial(miembroId)).withRel("ver-historial-completo")))
                .toList();

        return ResponseEntity.ok(CollectionModel.of(ventas,
                linkTo(methodOn(TiendaController.class).reporte1(miembroId, fecha)).withSelfRel()));
    }

    @GetMapping("/reportes/producto/{productoId}")
    @Operation(summary = "Reporte 2: Ventas por producto", description = "Devuelve todo el historial de ventas asociado a un producto específico.")
    public ResponseEntity<CollectionModel<EntityModel<Venta>>> reporte2(@PathVariable Long productoId) {

        List<EntityModel<Venta>> ventas = service.reporteVentasPorProducto(productoId).stream()
                .map(v -> EntityModel.of(v, linkTo(methodOn(TiendaController.class).listar()).withRel("ver-catalogo")))
                .toList();

        return ResponseEntity.ok(CollectionModel.of(ventas,
                linkTo(methodOn(TiendaController.class).reporte2(productoId)).withSelfRel()));
    }

    @GetMapping("/reportes/miembro/{miembroId}/rango")
    @Operation(summary = "Reporte 3: Ventas de miembro entre fechas", description = "Devuelve las compras de un miembro en un rango de fechas.")
    public ResponseEntity<CollectionModel<EntityModel<Venta>>> reporte3(
            @PathVariable Long miembroId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        List<EntityModel<Venta>> ventas = service.reporteMiembroEntreFechas(miembroId, inicio, fin).stream()
                .map(v -> EntityModel.of(v, linkTo(methodOn(TiendaController.class).verHistorial(miembroId)).withRel("ver-historial-completo")))
                .toList();

        return ResponseEntity.ok(CollectionModel.of(ventas,
                linkTo(methodOn(TiendaController.class).reporte3(miembroId, inicio, fin)).withSelfRel()));
    }

    @GetMapping("/reportes/producto/{productoId}/rango")
    @Operation(summary = "Reporte 4: Ventas de producto entre fechas", description = "Devuelve el movimiento de un producto en un rango de fechas.")
    public ResponseEntity<CollectionModel<EntityModel<Venta>>> reporte4(
            @PathVariable Long productoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        List<EntityModel<Venta>> ventas = service.reporteProductoEntreFechas(productoId, inicio, fin).stream()
                .map(v -> EntityModel.of(v, linkTo(methodOn(TiendaController.class).listar()).withRel("ver-catalogo")))
                .toList();

        return ResponseEntity.ok(CollectionModel.of(ventas,
                linkTo(methodOn(TiendaController.class).reporte4(productoId, inicio, fin)).withSelfRel()));
    }

    @GetMapping("/reportes/producto/{productoId}/total-unidades")
    @Operation(summary = "Reporte 5: Total unidades vendidas", description = "Devuelve la suma total de unidades que se han vendido de un producto.")
    public ResponseEntity<EntityModel<String>> reporte5(@PathVariable Long productoId) {

        Integer total = service.reporteTotalUnidadesVendidas(productoId);
        String mensaje = "Se han vendido un total de " + total + " unidades del producto ID: " + productoId;

        EntityModel<String> resource = EntityModel.of(mensaje,
                linkTo(methodOn(TiendaController.class).reporte5(productoId)).withSelfRel(),
                linkTo(methodOn(TiendaController.class).reporte2(productoId)).withRel("ver-detalle-ventas"));

        return ResponseEntity.ok(resource);
    }
}