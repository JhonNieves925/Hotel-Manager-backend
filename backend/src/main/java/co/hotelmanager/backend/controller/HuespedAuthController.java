package co.hotelmanager.backend.controller;

import co.hotelmanager.backend.dto.request.HuespedRegistroRequest;
import co.hotelmanager.backend.dto.request.LoginRequest;
import co.hotelmanager.backend.dto.response.HuespedUsuarioResponse;
import co.hotelmanager.backend.model.HuespedUsuario;
import co.hotelmanager.backend.security.JwtUtil;
import co.hotelmanager.backend.service.HuespedUsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/huesped-auth")
@RequiredArgsConstructor
public class HuespedAuthController {

    private final HuespedUsuarioService service;
    private final PasswordEncoder       passwordEncoder;
    private final JwtUtil               jwtUtil;

    // Registro
    @PostMapping("/register")
    public ResponseEntity<?> registrar(@RequestBody HuespedRegistroRequest req) {
        try {
            HuespedUsuarioResponse resp = service.registrar(req);
            return ResponseEntity.status(201).body(resp);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            HuespedUsuario huesped = service.buscarPorEmail(req.getEmail());
            if (!huesped.getActivo()) {
                return ResponseEntity.status(403)
                    .body(Map.of("error", "Cuenta desactivada"));
            }
            if (!passwordEncoder.matches(req.getPassword(), huesped.getPasswordHash())) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Credenciales incorrectas"));
            }
            String token = jwtUtil.generateTokenHuesped(huesped);
            return ResponseEntity.ok(Map.of(
                "token",    token,
                "id",       huesped.getId(),
                "nombre",   huesped.getNombre(),
                "apellido", huesped.getApellido(),
                "email",    huesped.getEmail(),
                "rol",      "huesped"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401)
                .body(Map.of("error", "Credenciales incorrectas"));
        }
    }
}