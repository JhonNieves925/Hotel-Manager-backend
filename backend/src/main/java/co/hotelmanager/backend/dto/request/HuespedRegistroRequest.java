package co.hotelmanager.backend.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class HuespedRegistroRequest {
    private String    nombre;
    private String    apellido;
    private String    email;
    private String    password;
    private String    telefono;
    private String    tipoDocumento;
    private String    numeroDocumento;
    private LocalDate fechaNacimiento;
    private String    nacionalidad;
}