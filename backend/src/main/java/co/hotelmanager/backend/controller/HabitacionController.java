package co.hotelmanager.backend.controller;

import co.hotelmanager.backend.dto.response.HabitacionResponse;
import co.hotelmanager.backend.model.Habitacion.Estado;
import co.hotelmanager.backend.service.HabitacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/habitaciones")
@RequiredArgsConstructor
public class HabitacionController {

    private final HabitacionService habitacionService;

    // GET /api/habitaciones/disponibles?entrada=2026-04-01&salida=2026-04-05
    // Público — lo usa el portal del huésped para ver qué hay libre
    @GetMapping("/disponibles")
    public ResponseEntity<List<HabitacionResponse>> disponibles(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate entrada,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate salida
    ) {
        return ResponseEntity.ok(
            habitacionService.listarDisponibles(entrada, salida)
        );
    }

    // GET /api/habitaciones — empleado y admin
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    public ResponseEntity<List<HabitacionResponse>> listarTodas() {
        return ResponseEntity.ok(habitacionService.listarTodas());
    }

    // GET /api/habitaciones/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    public ResponseEntity<HabitacionResponse> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(habitacionService.obtenerPorId(id));
    }

    // PATCH /api/habitaciones/{id}/estado?nuevoEstado=mantenimiento
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    public ResponseEntity<HabitacionResponse> cambiarEstado(
        @PathVariable Integer id,
        @RequestParam Estado nuevoEstado
    ) {
        return ResponseEntity.ok(
            habitacionService.cambiarEstado(id, nuevoEstado)
        );
    }
}