package co.hotelmanager.backend.controller;

import co.hotelmanager.backend.dto.request.ReservaRequest;
import co.hotelmanager.backend.dto.response.ReservaResponse;
import co.hotelmanager.backend.model.Reserva.EstadoReserva;
import co.hotelmanager.backend.model.Usuario;
import co.hotelmanager.backend.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    // GET /api/reservas — empleado y admin
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    public ResponseEntity<List<ReservaResponse>> listarTodas() {
        return ResponseEntity.ok(reservaService.listarTodas());
    }

    // GET /api/reservas/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    public ResponseEntity<ReservaResponse> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(reservaService.obtenerPorId(id));
    }

    // GET /api/reservas/hoy — reservas activas del día
    @GetMapping("/hoy")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    public ResponseEntity<List<ReservaResponse>> reservasDeHoy() {
        return ResponseEntity.ok(reservaService.reservasDeHoy());
    }

    // POST /api/reservas
    // @AuthenticationPrincipal inyecta el usuario logueado automáticamente
    // Si viene del portal público el usuario será null
    @PostMapping
    public ResponseEntity<ReservaResponse> crear(
        @Valid @RequestBody ReservaRequest request,
        @AuthenticationPrincipal Usuario usuario
    ) {
        return ResponseEntity.status(201)
            .body(reservaService.crear(request, usuario));
    }

    // PATCH /api/reservas/{id}/estado?nuevoEstado=confirmada
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    public ResponseEntity<ReservaResponse> cambiarEstado(
        @PathVariable Integer id,
        @RequestParam EstadoReserva nuevoEstado
    ) {
        return ResponseEntity.ok(
            reservaService.cambiarEstado(id, nuevoEstado)
        );
    }

    // DELETE /api/reservas/{id} — solo admin
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        reservaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}