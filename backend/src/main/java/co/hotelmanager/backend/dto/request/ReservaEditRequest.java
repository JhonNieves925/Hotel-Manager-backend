package co.hotelmanager.backend.dto.request;

import co.hotelmanager.backend.model.Reserva.FormaPago;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

// DTO para editar una reserva existente.
// Solo permite cambiar fechas, habitación, forma de pago y observaciones.
// Los datos del huésped se editan por separado si se necesitan.
@Getter
@Setter
public class ReservaEditRequest {

    private Integer    idHabitacion;
    private LocalDate  fechaEntrada;
    private LocalDate  fechaSalida;
    private FormaPago  formaPago;
    private String     observaciones;

    // Datos del huésped — opcionales, solo se actualizan si vienen
    private String nombreHuesped;
    private String apellidoHuesped;
    private String telefonoHuesped;
    private String emailHuesped;
}