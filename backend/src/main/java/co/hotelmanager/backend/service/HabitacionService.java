package co.hotelmanager.backend.service;

import co.hotelmanager.backend.dto.response.HabitacionResponse;
import co.hotelmanager.backend.exception.ResourceNotFoundException;
import co.hotelmanager.backend.model.Habitacion;
import co.hotelmanager.backend.model.Habitacion.Estado;
import co.hotelmanager.backend.repository.HabitacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HabitacionService {

    private final HabitacionRepository habitacionRepository;

    // Todas las habitaciones — panel del empleado y admin
    public List<HabitacionResponse> listarTodas() {
        return habitacionRepository.findAll()
            .stream()
            .map(HabitacionResponse::from)
            .toList();
    }

    // Habitaciones disponibles para un rango de fechas
    // Endpoint público — lo usa el portal del huésped
    public List<HabitacionResponse> listarDisponibles(
        LocalDate fechaEntrada,
        LocalDate fechaSalida
    ) {
        if (!fechaSalida.isAfter(fechaEntrada)) {
            throw new IllegalArgumentException(
                "La fecha de salida debe ser posterior a la fecha de entrada"
            );
        }
        return habitacionRepository.findDisponibles(fechaEntrada, fechaSalida)
            .stream()
            .map(HabitacionResponse::from)
            .toList();
    }

    // Detalle de una habitación por id
    public HabitacionResponse obtenerPorId(Integer id) {
        Habitacion habitacion = habitacionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Habitación no encontrada con id: " + id
            ));
        return HabitacionResponse.from(habitacion);
    }

    // Cambiar estado de una habitación
    // Empleado puede cambiar a ocupada/disponible
    // Admin puede cambiar a mantenimiento/fuera_de_servicio
    public HabitacionResponse cambiarEstado(Integer id, Estado nuevoEstado) {
        Habitacion habitacion = habitacionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Habitación no encontrada con id: " + id
            ));
        habitacion.setEstado(nuevoEstado);
        return HabitacionResponse.from(habitacionRepository.save(habitacion));
    }

    // Conteo por estado — para las tarjetas del dashboard admin
    public long contarPorEstado(Estado estado) {
        return habitacionRepository.countByEstado(estado);
    }

    // Método interno usado por ReservaService
    public Habitacion obtenerEntidadPorId(Integer id) {
        return habitacionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Habitación no encontrada con id: " + id
            ));
    }
}