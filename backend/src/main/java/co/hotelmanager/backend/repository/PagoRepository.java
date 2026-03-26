package co.hotelmanager.backend.repository;

import co.hotelmanager.backend.model.Pago;
import co.hotelmanager.backend.model.Pago.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Integer> {

    // Pagos de una reserva específica
    List<Pago> findByReservaId(Integer idReserva);

    // Pagos aprobados en un rango de fechas —
    // base de todos los reportes financieros del admin
    List<Pago> findByEstadoPagoAndFechaPagoBetween(
        EstadoPago estado,
        LocalDateTime inicio,
        LocalDateTime fin
    );

    // Suma total de pagos aprobados en un rango —
    // usado directamente por ReporteService
    @Query("""
        SELECT COALESCE(SUM(p.monto), 0)
        FROM Pago p
        WHERE p.estadoPago = co.hotelmanager.backend.model.Pago.EstadoPago.aprobado
        AND p.fechaPago BETWEEN :inicio AND :fin
    """)
    BigDecimal sumMontoAprobadoEnRango(
        @Param("inicio") LocalDateTime inicio,
        @Param("fin")    LocalDateTime fin
    );

    // Contar transacciones aprobadas en un rango —
    // para mostrar número de pagos en el dashboard
    @Query("""
        SELECT COUNT(p)
        FROM Pago p
        WHERE p.estadoPago = co.hotelmanager.backend.model.Pago.EstadoPago.aprobado
        AND p.fechaPago BETWEEN :inicio AND :fin
    """)
    long countAprobadosEnRango(
        @Param("inicio") LocalDateTime inicio,
        @Param("fin")    LocalDateTime fin
    );
}