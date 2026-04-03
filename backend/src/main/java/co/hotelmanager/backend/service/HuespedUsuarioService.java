package co.hotelmanager.backend.service;

import co.hotelmanager.backend.dto.request.HuespedRegistroRequest;
import co.hotelmanager.backend.dto.response.HuespedUsuarioResponse;
import co.hotelmanager.backend.model.HuespedUsuario;
import co.hotelmanager.backend.repository.HuespedUsuarioRepository;
import co.hotelmanager.backend.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HuespedUsuarioService {

    private final HuespedUsuarioRepository repo;
    private final ReservaRepository         reservaRepo;
    private final PasswordEncoder           passwordEncoder;

    public HuespedUsuarioResponse registrar(HuespedRegistroRequest req) {
        if (repo.existsByEmail(req.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        HuespedUsuario h = HuespedUsuario.builder()
            .nombre(req.getNombre())
            .apellido(req.getApellido())
            .email(req.getEmail())
            .passwordHash(passwordEncoder.encode(req.getPassword()))
            .telefono(req.getTelefono())
            .tipoDocumento(req.getTipoDocumento())
            .numeroDocumento(req.getNumeroDocumento())
            .fechaNacimiento(req.getFechaNacimiento())
            .nacionalidad(req.getNacionalidad())
            .activo(true)
            .build();
        return HuespedUsuarioResponse.from(repo.save(h));
    }

    public HuespedUsuario buscarPorEmail(String email) {
        return repo.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Huésped no encontrado"));
    }

    public HuespedUsuarioResponse obtenerPerfil(Integer id) {
        return HuespedUsuarioResponse.from(
            repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Huésped no encontrado"))
        );
    }

    public HuespedUsuarioResponse actualizarPerfil(Integer id, HuespedRegistroRequest req) {
        HuespedUsuario h = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Huésped no encontrado"));
        h.setNombre(req.getNombre());
        h.setApellido(req.getApellido());
        h.setTelefono(req.getTelefono());
        h.setNacionalidad(req.getNacionalidad());
        h.setFechaNacimiento(req.getFechaNacimiento());
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            h.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        return HuespedUsuarioResponse.from(repo.save(h));
    }
}