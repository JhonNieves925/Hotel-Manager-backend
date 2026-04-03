package co.hotelmanager.backend.repository;

import co.hotelmanager.backend.model.HuespedUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface HuespedUsuarioRepository extends JpaRepository<HuespedUsuario, Integer> {
    Optional<HuespedUsuario> findByEmail(String email);
    boolean existsByEmail(String email);
}