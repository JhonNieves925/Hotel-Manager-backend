package co.hotelmanager.backend.dto.response;

import co.hotelmanager.backend.model.Reserva;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ReservaResponse {

    private Integer id;
    private String numeroHabitacion;
    private String tipoHabitacion;
    private LocalDate fechaEntrada;
    private LocalDate fechaSalida;
    private Integer noches;
    private BigDecimal valorTotal;
    private String formaPago;
    private String estadoReserva;
    private LocalDateTime createdAt;

    // Datos básicos del huésped incluidos en la respuesta
    private String nombreHuesped;
    private String apellidoHuesped;
    private String telefonoHuesped;
    private String emailHuesped;

    // Convierte la entidad Reserva a este DTO
    public static ReservaResponse from(Reserva reserva) {
        String nombreHuesped = "";
        String apellidoHuesped = "";
        String telefonoHuesped = "";
        String emailHuesped = "";

        if (reserva.getHuesped() != null) {
            nombreHuesped   = reserva.getHuesped().getNombre();
            apellidoHuesped = reserva.getHuesped().getApellido();
            telefonoHuesped = reserva.getHuesped().getTelefono();
            emailHuesped    = reserva.getHuesped().getEmail();
        }

        long noches = 0;
        if (reserva.getFechaEntrada() != null && reserva.getFechaSalida() != null) {
            noches = java.time.temporal.ChronoUnit.DAYS.between(
                reserva.getFechaEntrada(),
                reserva.getFechaSalida()
            );
        }

        return ReservaResponse.builder()
            .id(reserva.getId())
            .numeroHabitacion(reserva.getHabitacion().getNumero())
            .tipoHabitacion(reserva.getHabitacion().getTipo().getNombre())
            .fechaEntrada(reserva.getFechaEntrada())
            .fechaSalida(reserva.getFechaSalida())
            .noches((int) noches)
            .valorTotal(reserva.getValorTotal())
            .formaPago(reserva.getFormaPago().name())
            .estadoReserva(reserva.getEstadoReserva().name())
            .createdAt(reserva.getCreatedAt())
            .nombreHuesped(nombreHuesped)
            .apellidoHuesped(apellidoHuesped)
            .telefonoHuesped(telefonoHuesped)
            .emailHuesped(emailHuesped)
            .build();
    }
}