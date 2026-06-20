package com.gymapp.ms_tienda.client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-miembros")
public interface MiembroClient {
    @GetMapping("/api/miembros/validar/{id}")
    Boolean validarMiembro(@PathVariable("id") Long id);
}