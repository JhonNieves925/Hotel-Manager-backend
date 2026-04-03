package co.hotelmanager.backend.controller;

import co.hotelmanager.backend.dto.request.HuespedRegistroRequest;
import co.hotelmanager.backend.dto.response.HuespedUsuarioResponse;
import co.hotelmanager.backend.service.HuespedUsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/huesped-perfil")
@RequiredArgsConstructor
public class HuespedPerfilController {

    private final HuespedUsuarioService service;

    @GetMapping("/{id}")
    public ResponseEntity<HuespedUsuarioResponse> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtenerPerfil(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HuespedUsuarioResponse> actualizar(
        @PathVariable Integer id,
        @RequestBody HuespedRegistroRequest req
    ) {
        return ResponseEntity.ok(service.actualizarPerfil(id, req));
    }
}