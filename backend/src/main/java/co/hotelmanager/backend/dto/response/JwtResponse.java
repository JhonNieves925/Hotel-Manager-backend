package co.hotelmanager.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class JwtResponse {

    private String token;
    private String tipo;
    private String email;
    private String nombre;
    private String rol;

    // Constructor de conveniencia — el tipo siempre es "Bearer"
    public static JwtResponse of(String token, String email,
                                  String nombre, String rol) {
        return JwtResponse.builder()
            .token(token)
            .tipo("Bearer")
            .email(email)
            .nombre(nombre)
            .rol(rol)
            .build();
    }
}