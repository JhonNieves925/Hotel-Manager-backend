package co.hotelmanager.backend.service;

import co.hotelmanager.backend.dto.response.IngresoResponse;
import co.hotelmanager.backend.dto.response.OcupacionResponse;
import co.hotelmanager.backend.model.Habitacion.Estado;
import co.hotelmanager.backend.repository.HabitacionRepository;
import co.hotelmanager.backend.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final PagoRepository pagoRepository;
    private final HabitacionRepository habitacionRepository;

    // ============================================================
    // MÉTODOS PARA ADMIN — incluyen datos financieros
    // ============================================================

    public IngresoResponse ingresosHoy() {
        LocalDate hoy  = LocalDate.now();
        return construirReporteAdmin("hoy",
            hoy.atStartOfDay(),
            hoy.atTime(23, 59, 59)
        );
    }

    public IngresoResponse ingresosSemana() {
        LocalDate hoy    = LocalDate.now();
        LocalDate lunes  = hoy.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
        LocalDate domingo = lunes.plusDays(6);
        return construirReporteAdmin("semana",
            lunes.atStartOfDay(),
            domingo.atTime(23, 59, 59)
        );
    }

    public IngresoResponse ingresosMes() {
        LocalDate hoy = LocalDate.now();
        return construirReporteAdmin(
            hoy.getMonth().name().toLowerCase(),
            hoy.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay(),
            hoy.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59)
        );
    }

    public IngresoResponse ingresosAnual(int anio) {
        return construirReporteAdmin(
            String.valueOf(anio),
            LocalDate.of(anio, 1, 1).atStartOfDay(),
            LocalDate.of(anio, 12, 31).atTime(23, 59, 59)
        );
    }

    // ============================================================
    // MÉTODOS PARA EMPLEADO — solo ocupación, sin dinero
    // ============================================================

    public OcupacionResponse ocupacionHoy() {
        return construirOcupacion("hoy");
    }

    public OcupacionResponse ocupacionSemana() {
        return construirOcupacion("semana");
    }

    public OcupacionResponse ocupacionMes() {
        LocalDate hoy = LocalDate.now();
        return construirOcupacion(
            hoy.getMonth().name().toLowerCase()
        );
    }

    public OcupacionResponse ocupacionAnual(int anio) {
        return construirOcupacion(String.valueOf(anio));
    }

    // ============================================================
    // MÉTODOS PRIVADOS — construcción de respuestas
    // ============================================================

    // Reporte completo con datos financieros — solo para admin
    private IngresoResponse construirReporteAdmin(
        String periodo,
        LocalDateTime inicio,
        LocalDateTime fin
    ) {
        BigDecimal total       = pagoRepository.sumMontoAprobadoEnRango(inicio, fin);
        long transacciones     = pagoRepository.countAprobadosEnRango(inicio, fin);
        long ocupadas          = habitacionRepository.countByEstado(Estado.ocupada);
        long disponibles       = habitacionRepository.countByEstado(Estado.disponible);
        long mantenimiento     = habitacionRepository.countByEstado(Estado.mantenimiento);

        return IngresoResponse.builder()
            .periodo(periodo)
            .total(total != null ? total : BigDecimal.ZERO)
            .transacciones(transacciones)
            .habitacionesOcupadas(ocupadas)
            .habitacionesDisponibles(disponibles)
            .habitacionesMantenimiento(mantenimiento)
            .build();
    }

    // Reporte solo de ocupación — para empleado
    // No toca la tabla de pagos en ningún momento
    private OcupacionResponse construirOcupacion(String periodo) {
        long ocupadas      = habitacionRepository.countByEstado(Estado.ocupada);
        long disponibles   = habitacionRepository.countByEstado(Estado.disponible);
        long mantenimiento = habitacionRepository.countByEstado(Estado.mantenimiento);
        long total         = ocupadas + disponibles + mantenimiento;

        double porcentaje = total > 0
            ? Math.round((ocupadas * 100.0 / total) * 10.0) / 10.0
            : 0.0;

        return OcupacionResponse.builder()
            .periodo(periodo)
            .habitacionesOcupadas(ocupadas)
            .habitacionesDisponibles(disponibles)
            .habitacionesMantenimiento(mantenimiento)
            .totalHabitaciones(total)
            .porcentajeOcupacion(porcentaje)
            .build();
    }
}