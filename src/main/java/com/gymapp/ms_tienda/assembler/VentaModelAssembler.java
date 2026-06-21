package com.gymapp.ms_tienda.assembler;

import com.gymapp.ms_tienda.controller.TiendaController;
import com.gymapp.ms_tienda.model.Venta;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class VentaModelAssembler implements RepresentationModelAssembler<Venta, EntityModel<Venta>> {

    @Override
    public EntityModel<Venta> toModel(Venta venta) {
        return EntityModel.of(venta,
                linkTo(methodOn(TiendaController.class).verHistorial(venta.getMiembroId())).withRel("historial-miembro"),
                linkTo(methodOn(TiendaController.class).listar()).withRel("catalogo-productos")
        );
    }
}