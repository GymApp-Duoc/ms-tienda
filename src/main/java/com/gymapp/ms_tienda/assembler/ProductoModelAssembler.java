package com.gymapp.ms_tienda.assembler;

import com.gymapp.ms_tienda.controller.TiendaController;
import com.gymapp.ms_tienda.model.Producto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProductoModelAssembler implements RepresentationModelAssembler<Producto, EntityModel<Producto>> {

    @Override
    public EntityModel<Producto> toModel(Producto producto) {
        return EntityModel.of(producto,
                linkTo(methodOn(TiendaController.class).listar()).withRel("catalogo"),
                linkTo(methodOn(TiendaController.class).reporte2(producto.getId())).withRel("ver-ventas-producto")
        );
    }
}
