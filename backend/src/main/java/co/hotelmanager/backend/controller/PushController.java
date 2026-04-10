package co.hotelmanager.backend.controller;

import co.hotelmanager.backend.model.HuespedUsuario;
import co.hotelmanager.backend.repository.HuespedUsuarioRepository;
import co.hotelmanager.backend.service.NotificacionPushService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class PushController {

    private final NotificacionPushService pushService;
    private final HuespedUsuarioRepository huespedRepo;

    /** Devuelve la clave pública VAPID para que el frontend pueda suscribirse */
    @GetMapping("/vapid-key")
    public ResponseEntity<Map<String, String>> getVapidKey() {
        return ResponseEntity.ok(Map.of("publicKey", pushService.getVapidPublicKey()));
    }

    /** Registra la suscripción push del huésped autenticado */
    @PostMapping("/suscribir")
    public ResponseEntity<Void> suscribir(
            @RequestBody Map<String, Object> body,
            Authentication auth) {

        String email = auth.getName();
        HuespedUsuario huesped = huespedRepo.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Huésped no encontrado"));

        String endpoint = (String) body.get("endpoint");

        @SuppressWarnings("unchecked")
        Map<String, String> keys = (Map<String, String>) body.get("keys");
        String p256dh = keys.get("p256dh");
        String authKey = keys.get("auth");

        pushService.guardarSuscripcion(huesped.getId(), endpoint, p256dh, authKey);
        return ResponseEntity.ok().build();
    }

    /** Elimina la suscripción push */
    @DeleteMapping("/cancelar")
    public ResponseEntity<Void> cancelar(@RequestBody Map<String, String> body) {
        pushService.eliminarSuscripcion(body.get("endpoint"));
        return ResponseEntity.noContent().build();
    }
}
