package com.gymapp.ms_tienda.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

@FeignClient(name = "ms-gamificacion", url = "${ms.gamificacion.url}")
public interface GamificacionClient {

    @PostMapping("/api/gamificacion/eventos")
    void enviarEvento(@RequestBody Map<String, Object> evento);
}