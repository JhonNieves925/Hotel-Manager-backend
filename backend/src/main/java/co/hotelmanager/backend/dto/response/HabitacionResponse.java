package co.hotelmanager.backend.dto.response;

import co.hotelmanager.backend.model.Habitacion;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

// ============================================================
//  HabitacionResponse — detalle de una habitación
// ============================================================
@Getter
@Builder
public class HabitacionResponse {

    private Integer id;
    private String numero;
    private Integer piso;
    private String tipo;
    private String descripcionTipo;
    private BigDecimal precioNoche;
    private Integer capacidad;
    private String estado;
    private String descripcion;

    public static HabitacionResponse from(Habitacion h) {
        return HabitacionResponse.builder()
            .id(h.getId())
            .numero(h.getNumero())
            .piso(h.getPiso())
            .tipo(h.getTipo().getNombre())
            .descripcionTipo(h.getTipo().getDescripcion())
            .precioNoche(h.getTipo().getPrecioNoche())
            .capacidad(h.getTipo().getCapacidad())
            .estado(h.getEstado().name())
            .descripcion(h.getDescripcion())
            .build();
    }
}