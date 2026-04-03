package co.hotelmanager.backend.controller;

import co.hotelmanager.backend.dto.response.ReservaResponse;
import co.hotelmanager.backend.model.HuespedUsuario;
import co.hotelmanager.backend.repository.HuespedUsuarioRepository;
import co.hotelmanager.backend.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/huesped-reservas")
@RequiredArgsConstructor
public class HuespedReservasController {

    private final ReservaRepository        reservaRepo;
    private final HuespedUsuarioRepository huespedRepo;

    // Ver todas las reservas del huésped autenticado
    @GetMapping
    public ResponseEntity<?> misReservas(Authentication auth) {
        String email = auth.getName();
        HuespedUsuario huesped = huespedRepo.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Huésped no encontrado"));

        List<ReservaResponse> reservas = reservaRepo
            .findByIdHuespedUsuario(huesped.getId())
            .stream()
            .map(ReservaResponse::from)
            .toList();

        return ResponseEntity.ok(reservas);
    }

    // Cancelar una reserva propia
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelar(
        @PathVariable Integer id,
        Authentication auth
    ) {
        String email = auth.getName();
        HuespedUsuario huesped = huespedRepo.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Huésped no encontrado"));

        return reservaRepo.findById(id).map(reserva -> {
            // Verificar que la reserva pertenece al huésped
            if (!huesped.getId().equals(reserva.getIdHuespedUsuario())) {
                return ResponseEntity.status(403)
                    .body("No tienes permiso para cancelar esta reserva");
            }
            if (!List.of("pendiente", "confirmada")
                    .contains(reserva.getEstadoReserva().name())) {
                return ResponseEntity.badRequest()
                    .body("Solo se pueden cancelar reservas pendientes o confirmadas");
            }
            reserva.setEstadoReserva(
                co.hotelmanager.backend.model.Reserva.EstadoReserva.cancelada
            );
            reservaRepo.save(reserva);
            return ResponseEntity.ok("Reserva cancelada");
        }).orElse(ResponseEntity.notFound().build());
    }
}