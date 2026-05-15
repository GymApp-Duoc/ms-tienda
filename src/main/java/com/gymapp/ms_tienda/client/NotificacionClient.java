package com.gymapp.ms_tienda.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

@FeignClient(name = "ms-notificaciones", url = "${ms.notificaciones.url}")
public interface NotificacionClient {

    @PostMapping("/api/notificaciones")
    void enviarNotificacion(@RequestBody Map<String, Object> notificacion);
}