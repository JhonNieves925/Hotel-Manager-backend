package co.hotelmanager.backend.repository;

import co.hotelmanager.backend.model.Reserva;
import co.hotelmanager.backend.model.Reserva.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Integer> {
	
	List<Reserva> findByIdHuespedUsuario(Integer idHuespedUsuario);

    // Reservas por estado — para filtrar en el panel del empleado
    List<Reserva> findByEstadoReserva(EstadoReserva estado);

    // Reservas gestionadas por un empleado específico
    List<Reserva> findByUsuarioId(Integer idUsuario);

    // Reservas activas de una habitación —
    // para verificar disponibilidad antes de confirmar
    @Query("""
        SELECT r FROM Reserva r
        WHERE r.habitacion.id = :idHabitacion
        AND r.estadoReserva NOT IN ('cancelada', 'completada')
        AND r.fechaEntrada < :fechaSalida
        AND r.fechaSalida  > :fechaEntrada
    """)
    List<Reserva> findReservasActivasEnRango(
        @Param("idHabitacion") Integer idHabitacion,
        @Param("fechaEntrada") LocalDate fechaEntrada,
        @Param("fechaSalida")  LocalDate fechaSalida
    );

    // --------------------------------------------------------
    // Consultas para reportes del admin
    // --------------------------------------------------------

    // Ingresos totales de un día específico
    @Query("""
        SELECT COALESCE(SUM(p.monto), 0)
        FROM Pago p
        WHERE p.estadoPago = 'aprobado'
        AND CAST(p.fechaPago AS LocalDate) = :fecha
    """)
    BigDecimal sumIngresosByFecha(@Param("fecha") LocalDate fecha);

    // Ingresos entre dos fechas — para semana, mes o año
    @Query("""
        SELECT COALESCE(SUM(p.monto), 0)
        FROM Pago p
        WHERE p.estadoPago = 'aprobado'
        AND p.fechaPago BETWEEN :inicio AND :fin
    """)
    BigDecimal sumIngresosByRango(
        @Param("inicio") LocalDateTime inicio,
        @Param("fin")    LocalDateTime fin
    );

    // Reservas de hoy — las que tienen check-in o están en curso
    @Query("""
        SELECT r FROM Reserva r
        WHERE r.estadoReserva IN ('confirmada', 'en_curso')
        AND :hoy BETWEEN r.fechaEntrada AND r.fechaSalida
    """)
    List<Reserva> findReservasActivas(@Param("hoy") LocalDate hoy);
}