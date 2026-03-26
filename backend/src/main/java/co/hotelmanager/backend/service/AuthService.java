package co.hotelmanager.backend.service;

import co.hotelmanager.backend.dto.request.LoginRequest;
import co.hotelmanager.backend.dto.request.RegisterRequest;
import co.hotelmanager.backend.dto.response.JwtResponse;
import co.hotelmanager.backend.model.Usuario;
import co.hotelmanager.backend.repository.UsuarioRepository;
import co.hotelmanager.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    // Login — valida credenciales y devuelve un token JWT
    public JwtResponse login(LoginRequest request) {

        // AuthenticationManager verifica email + contraseña
        // Si son incorrectos lanza BadCredentialsException
        // que GlobalExceptionHandler convierte en 401
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        Usuario usuario = (Usuario) auth.getPrincipal();
        String token = jwtUtil.generateToken(usuario);

        return JwtResponse.of(
            token,
            usuario.getEmail(),
            usuario.getNombre(),
            usuario.getRol().name()
        );
    }

    // Registro — solo admin puede crear usuarios
    // El endpoint está protegido en SecurityConfig
    public void register(RegisterRequest request) {

        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                "Ya existe un usuario con el email: " + request.getEmail()
            );
        }

        Usuario usuario = Usuario.builder()
            .nombre(request.getNombre())
            .email(request.getEmail())
            // Hasheamos la contraseña con BCrypt antes de guardar
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .rol(request.getRol())
            .activo(true)
            .build();

        usuarioRepository.save(usuario);
    }
}