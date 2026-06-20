package com.gymapp.ms_tienda.repository;

import com.gymapp.ms_tienda.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    List<Venta> findByProductoId(Long productoId);

    List<Venta> findByMiembroIdOrderByFechaVentaDesc(Long miembroId);


    List<Venta> findByMiembroIdAndFechaVentaBetween(Long miembroId, LocalDateTime inicio, LocalDateTime fin);


    List<Venta> findByProductoIdAndFechaVentaBetween(Long productoId, LocalDateTime inicio, LocalDateTime fin);


    @Query("SELECT SUM(v.cantidad) FROM Venta v WHERE v.productoId = :productoId")
    Integer sumarUnidadesVendidasPorProducto(@Param("productoId") Long productoId);
}