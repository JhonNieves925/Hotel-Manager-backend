package co.hotelmanager.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

// ============================================================
//  IngresoResponse — respuesta de los reportes financieros
//  Usado por /api/reportes/ingresos/hoy|semana|mes|anual
// ============================================================
@Getter
@Builder
@AllArgsConstructor
public class IngresoResponse {

    private String periodo;        // "hoy", "semana", "mes", "2026"
    private BigDecimal total;      // suma de pagos aprobados
    private long transacciones;    // número de pagos en el periodo
    private long habitacionesOcupadas;
    private long habitacionesDisponibles;
    private long habitacionesMantenimiento;
}