package co.hotelmanager.backend.exception;

// ============================================================
//  HabitacionOcupadaException
//  Se lanza cuando se intenta reservar una habitación que
//  ya tiene una reserva activa para las fechas solicitadas
// ============================================================
public class HabitacionOcupadaException extends RuntimeException {
    public HabitacionOcupadaException(String numeroHabitacion,
                                       String fechaEntrada,
                                       String fechaSalida) {
        super("La habitación " + numeroHabitacion +
              " no está disponible del " + fechaEntrada +
              " al " + fechaSalida);
    }
}