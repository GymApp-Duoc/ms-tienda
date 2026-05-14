package com.gymapp.ms_tienda.repository;

import com.gymapp.ms_tienda.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByStockLessThan(int limite);
}