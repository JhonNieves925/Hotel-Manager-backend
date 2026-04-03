package co.hotelmanager.backend.dto.response;

import co.hotelmanager.backend.model.HuespedUsuario;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class HuespedUsuarioResponse {
    private Integer   id;
    private String    nombre;
    private String    apellido;
    private String    email;
    private String    telefono;
    private String    tipoDocumento;
    private String    numeroDocumento;
    private LocalDate fechaNacimiento;
    private String    nacionalidad;

    public static HuespedUsuarioResponse from(HuespedUsuario h) {
        return HuespedUsuarioResponse.builder()
            .id(h.getId())
            .nombre(h.getNombre())
            .apellido(h.getApellido())
            .email(h.getEmail())
            .telefono(h.getTelefono())
            .tipoDocumento(h.getTipoDocumento())
            .numeroDocumento(h.getNumeroDocumento())
            .fechaNacimiento(h.getFechaNacimiento())
            .nacionalidad(h.getNacionalidad())
            .build();
    }
}