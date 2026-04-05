package co.hotelmanager.backend.controller;

import co.hotelmanager.backend.exception.ResourceNotFoundException;
import co.hotelmanager.backend.model.Huesped;
import co.hotelmanager.backend.repository.HuespedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/huespedes")
@RequiredArgsConstructor
public class HuespedController {

    private final HuespedRepository huespedRepository;

    // Convierte la entidad a un Map simple para evitar
    // el ciclo infinito Huesped -> Reserva -> Huesped
    private Map<String, Object> toDto(Huesped h) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id",              h.getId());
        dto.put("nombre",          h.getNombre());
        dto.put("apellido",        h.getApellido());
        dto.put("fechaNacimiento", h.getFechaNacimiento() != null ? h.getFechaNacimiento().toString() : "");
        dto.put("nacionalidad",    h.getNacionalidad());
        dto.put("telefono",        h.getTelefono());
        dto.put("email",           h.getEmail() != null ? h.getEmail() : "");
        dto.put("tipoDocumento",   h.getTipoDocumento().name());
        dto.put("numeroDocumento", h.getNumeroDocumento());
        dto.put("idReserva",       h.getReserva() != null ? h.getReserva().getId() : null);
        return dto;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    public ResponseEntity<List<Map<String, Object>>> listarTodos() {
        return ResponseEntity.ok(
            huespedRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList())
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    public ResponseEntity<Map<String, Object>> obtener(@PathVariable Integer id) {
        Huesped h = huespedRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Huésped no encontrado con id: " + id
            ));
        return ResponseEntity.ok(toDto(h));
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    public ResponseEntity<List<Map<String, Object>>> buscar(@RequestParam String q) {
        return ResponseEntity.ok(
            huespedRepository
                .findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(q, q)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList())
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        Huesped huesped = huespedRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Huésped no encontrado con id: " + id
            ));
        huespedRepository.delete(huesped);
        return ResponseEntity.noContent().build();
    }
}