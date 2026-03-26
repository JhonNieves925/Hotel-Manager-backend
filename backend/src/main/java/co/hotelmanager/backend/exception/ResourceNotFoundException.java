package co.hotelmanager.backend.exception;

// ============================================================
//  ResourceNotFoundException
//  Se lanza cuando no se encuentra un registro en la BD
//  Ejemplo: buscar reserva con id que no existe
// ============================================================
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }
}