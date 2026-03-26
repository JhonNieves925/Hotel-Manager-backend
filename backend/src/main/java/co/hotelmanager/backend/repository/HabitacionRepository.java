package co.hotelmanager.backend.repository;

import co.hotelmanager.backend.model.Habitacion;
import co.hotelmanager.backend.model.Habitacion.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HabitacionRepository extends JpaRepository<Habitacion, Integer> {

    // Todas las habitaciones por estado — para el dashboard del admin
    List<Habitacion> findByEstado(Estado estado);

    // Verificar si un número de habitación ya existe
    boolean existsByNumero(String numero);

    // Habitaciones disponibles para un rango de fechas —
    // excluye las que tienen reservas activas que se solapan
    // con las fechas pedidas. Usado por el portal público.
    @Query("""
        SELECT h FROM Habitacion h
        WHERE h.estado = 'disponible'
        AND h.id NOT IN (
            SELECT r.habitacion.id FROM Reserva r
            WHERE r.estadoReserva NOT IN ('cancelada', 'completada')
            AND r.fechaEntrada < :fechaSalida
            AND r.fechaSalida  > :fechaEntrada
        )
    """)
    List<Habitacion> findDisponibles(
        @Param("fechaEntrada") LocalDate fechaEntrada,
        @Param("fechaSalida")  LocalDate fechaSalida
    );

    // Contar habitaciones por estado — para las tarjetas del dashboard
    long countByEstado(Estado estado);
}