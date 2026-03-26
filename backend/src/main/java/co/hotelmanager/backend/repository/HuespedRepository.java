package co.hotelmanager.backend.repository;

import co.hotelmanager.backend.model.Huesped;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HuespedRepository extends JpaRepository<Huesped, Integer> {

    // Buscar por número de documento — para evitar duplicados
    // y para el check-in rápido desde el panel del empleado
    Optional<Huesped> findByNumeroDocumento(String numeroDocumento);

    // Buscar por reserva — para mostrar el huésped de una reserva
    Optional<Huesped> findByReservaId(Integer idReserva);

    // Búsqueda por nombre o apellido — para el buscador del panel
    List<Huesped> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(
        String nombre,
        String apellido
    );
}