package co.hotelmanager.backend.controller;

import co.hotelmanager.backend.dto.response.IngresoResponse;
import co.hotelmanager.backend.dto.response.OcupacionResponse;
import co.hotelmanager.backend.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    // ============================================================
    // ENDPOINTS ADMIN — datos financieros completos
    // Ruta: /api/reportes/admin/...
    // ============================================================

    @GetMapping("/admin/hoy")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IngresoResponse> adminHoy() {
        return ResponseEntity.ok(reporteService.ingresosHoy());
    }

    @GetMapping("/admin/semana")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IngresoResponse> adminSemana() {
        return ResponseEntity.ok(reporteService.ingresosSemana());
    }

    @GetMapping("/admin/mes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IngresoResponse> adminMes() {
        return ResponseEntity.ok(reporteService.ingresosMes());
    }

    @GetMapping("/admin/anual")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IngresoResponse> adminAnual(
        @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}")
        int anio
    ) {
        return ResponseEntity.ok(reporteService.ingresosAnual(anio));
    }

    // ============================================================
    // ENDPOINTS EMPLEADO — solo datos de ocupación, sin dinero
    // Ruta: /api/reportes/ocupacion/...
    // ============================================================

    @GetMapping("/ocupacion/hoy")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    public ResponseEntity<OcupacionResponse> ocupacionHoy() {
        return ResponseEntity.ok(reporteService.ocupacionHoy());
    }

    @GetMapping("/ocupacion/semana")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    public ResponseEntity<OcupacionResponse> ocupacionSemana() {
        return ResponseEntity.ok(reporteService.ocupacionSemana());
    }

    @GetMapping("/ocupacion/mes")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    public ResponseEntity<OcupacionResponse> ocupacionMes() {
        return ResponseEntity.ok(reporteService.ocupacionMes());
    }

    @GetMapping("/ocupacion/anual")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    public ResponseEntity<OcupacionResponse> ocupacionAnual(
        @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}")
        int anio
    ) {
        return ResponseEntity.ok(reporteService.ocupacionAnual(anio));
    }
}