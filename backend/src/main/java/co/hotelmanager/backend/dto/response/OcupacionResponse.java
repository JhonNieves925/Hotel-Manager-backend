package co.hotelmanager.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// ============================================================
//  OcupacionResponse — lo que ve el empleado en reportes
//  Solo datos de ocupación, sin ningún dato financiero
// ============================================================
@Getter
@Builder
@AllArgsConstructor
public class OcupacionResponse {

    private String periodo;
    private long habitacionesOcupadas;
    private long habitacionesDisponibles;
    private long habitacionesMantenimiento;
    private long totalHabitaciones;
    private double porcentajeOcupacion;
}