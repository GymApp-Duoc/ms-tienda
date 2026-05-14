package com.gymapp.ms_tienda.repository;

import com.gymapp.ms_tienda.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {


    List<Venta> findByProductoId(Long productoId);


    List<Venta> findByMiembroIdOrderByFechaVentaDesc(Long miembroId);
}
