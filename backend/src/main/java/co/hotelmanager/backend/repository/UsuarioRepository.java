package co.hotelmanager.backend.repository;

import co.hotelmanager.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Buscar por email — usado por Spring Security al hacer login
    Optional<Usuario> findByEmail(String email);

    // Verificar si ya existe un email antes de registrar
    boolean existsByEmail(String email);
}