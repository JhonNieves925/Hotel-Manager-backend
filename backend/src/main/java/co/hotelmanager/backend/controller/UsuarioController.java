package co.hotelmanager.backend.controller;

import co.hotelmanager.backend.dto.request.RegisterRequest;
import co.hotelmanager.backend.exception.ResourceNotFoundException;
import co.hotelmanager.backend.model.Usuario;
import co.hotelmanager.backend.repository.UsuarioRepository;
import co.hotelmanager.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    // Convierte entidad a DTO sin exponer el hash de contraseña
    private Map<String, Object> toDto(Usuario u) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id",        u.getId());
        dto.put("nombre",    u.getNombre());
        dto.put("email",     u.getEmail());
        dto.put("rol",       u.getRol().name());
        dto.put("activo",    u.getActivo());
        dto.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : "");
        return dto;
    }

    // GET /api/usuarios — listar todos los usuarios
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listarTodos() {
        return ResponseEntity.ok(
            usuarioRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList())
        );
    }

    // GET /api/usuarios/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtener(@PathVariable Integer id) {
        Usuario u = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Usuario no encontrado con id: " + id
            ));
        return ResponseEntity.ok(toDto(u));
    }

    // POST /api/usuarios — crear usuario (mismo que /auth/register)
    @PostMapping
    public ResponseEntity<Void> crear(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(201).build();
    }

    // PATCH /api/usuarios/{id} — editar nombre, email o contraseña
    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, Object>> editar(
        @PathVariable Integer id,
        @RequestBody Map<String, String> cambios
    ) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Usuario no encontrado con id: " + id
            ));

        if (cambios.containsKey("nombre") && !cambios.get("nombre").isBlank()) {
            usuario.setNombre(cambios.get("nombre"));
        }

        if (cambios.containsKey("email") && !cambios.get("email").isBlank()) {
            // Verificar que el nuevo email no esté en uso
            String nuevoEmail = cambios.get("email");
            if (!nuevoEmail.equals(usuario.getEmail()) &&
                usuarioRepository.existsByEmail(nuevoEmail)) {
                throw new IllegalArgumentException(
                    "Ya existe un usuario con el email: " + nuevoEmail
                );
            }
            usuario.setEmail(nuevoEmail);
        }

        if (cambios.containsKey("password") && !cambios.get("password").isBlank()) {
            if (cambios.get("password").length() < 6) {
                throw new IllegalArgumentException(
                    "La contraseña debe tener mínimo 6 caracteres"
                );
            }
            usuario.setPasswordHash(
                passwordEncoder.encode(cambios.get("password"))
            );
        }

        return ResponseEntity.ok(toDto(usuarioRepository.save(usuario)));
    }

    // PATCH /api/usuarios/{id}/estado — activar o bloquear
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Map<String, Object>> cambiarEstado(
        @PathVariable Integer id,
        @RequestParam Boolean activo
    ) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Usuario no encontrado con id: " + id
            ));
        usuario.setActivo(activo);
        return ResponseEntity.ok(toDto(usuarioRepository.save(usuario)));
    }

    // DELETE /api/usuarios/{id} — eliminar usuario
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Usuario no encontrado con id: " + id
            ));
        usuarioRepository.delete(usuario);
        return ResponseEntity.noContent().build();
    }
}