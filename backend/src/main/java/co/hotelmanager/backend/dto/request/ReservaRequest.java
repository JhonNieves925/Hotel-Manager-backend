package co.hotelmanager.backend.dto.request;

import co.hotelmanager.backend.model.Reserva.FormaPago;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ReservaRequest {

    @NotNull(message = "La habitación es obligatoria")
    private Integer idHabitacion;

    @NotNull(message = "La fecha de entrada es obligatoria")
    private LocalDate fechaEntrada;

    @NotNull(message = "La fecha de salida es obligatoria")
    @Future(message = "La fecha de salida debe ser futura")
    private LocalDate fechaSalida;

    @NotNull(message = "La forma de pago es obligatoria")
    private FormaPago formaPago;

    private String observaciones;

    // Datos del huésped — vienen junto con la reserva
    @NotNull(message = "Los datos del huésped son obligatorios")
    private HuespedRequest huesped;
    
    private Integer idHuespedUsuario;
}