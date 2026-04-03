package co.hotelmanager.backend.service;

import co.hotelmanager.backend.dto.request.ReservaRequest;
import co.hotelmanager.backend.dto.response.ReservaResponse;
import co.hotelmanager.backend.exception.HabitacionOcupadaException;
import co.hotelmanager.backend.exception.ResourceNotFoundException;
import co.hotelmanager.backend.model.*;
import co.hotelmanager.backend.model.Reserva.EstadoReserva;
import co.hotelmanager.backend.model.Pago.EstadoPago;
import co.hotelmanager.backend.repository.HuespedRepository;
import co.hotelmanager.backend.repository.PagoRepository;
import co.hotelmanager.backend.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final HuespedRepository huespedRepository;
    private final PagoRepository    pagoRepository;
    private final HabitacionService habitacionService;

    // Listar todas las reservas
    public List<ReservaResponse> listarTodas() {
        return reservaRepository.findAll()
            .stream()
            .map(ReservaResponse::from)
            .toList();
    }

    // Detalle de una reserva
    public ReservaResponse obtenerPorId(Integer id) {
        return ReservaResponse.from(obtenerEntidad(id));
    }

    // Crear reserva
    @Transactional
    public ReservaResponse crear(ReservaRequest request, Usuario usuario) {

        Habitacion habitacion = habitacionService
            .obtenerEntidadPorId(request.getIdHabitacion());

        // Verificar disponibilidad
        List<Reserva> conflictos = reservaRepository.findReservasActivasEnRango(
            habitacion.getId(),
            request.getFechaEntrada(),
            request.getFechaSalida()
        );

        if (!conflictos.isEmpty()) {
            throw new HabitacionOcupadaException(
                habitacion.getNumero(),
                request.getFechaEntrada().toString(),
                request.getFechaSalida().toString()
            );
        }

        // Calcular valor total
        long noches = ChronoUnit.DAYS.between(
            request.getFechaEntrada(),
            request.getFechaSalida()
        );
        BigDecimal valorTotal = habitacion.getTipo()
            .getPrecioNoche()
            .multiply(BigDecimal.valueOf(noches));

        // Crear reserva
        Reserva reserva = Reserva.builder()
            .habitacion(habitacion)
            .usuario(usuario)
            .fechaEntrada(request.getFechaEntrada())
            .fechaSalida(request.getFechaSalida())
            .valorTotal(valorTotal)
            .formaPago(request.getFormaPago())
            .estadoReserva(EstadoReserva.pendiente)
            .observaciones(request.getObservaciones())
            .idHuespedUsuario(request.getIdHuespedUsuario())
            .build();

        reserva = reservaRepository.save(reserva);

        // Crear huésped vinculado
        Huesped huesped = Huesped.builder()
        	    .reserva(reserva)
        	    .nombre(request.getHuesped().getNombre())
        	    .apellido(request.getHuesped().getApellido())
        	    .fechaNacimiento(request.getHuesped().getFechaNacimiento()) // puede ser null
        	    .nacionalidad(request.getHuesped().getNacionalidad())
        	    .telefono(request.getHuesped().getTelefono())
        	    .email(request.getHuesped().getEmail())
        	    .tipoDocumento(request.getHuesped().getTipoDocumento())
        	    .numeroDocumento(request.getHuesped().getNumeroDocumento())
        	    .build();

        huespedRepository.save(huesped);

        // Marcar habitación como ocupada
        habitacionService.cambiarEstado(
            habitacion.getId(),
            Habitacion.Estado.ocupada
        );

        return ReservaResponse.from(obtenerEntidad(reserva.getId()));
    }

    // Cambiar estado de la reserva
    @Transactional
    public ReservaResponse cambiarEstado(Integer id, EstadoReserva nuevoEstado) {
        Reserva reserva = obtenerEntidad(id);
        reserva.setEstadoReserva(nuevoEstado);

        // --------------------------------------------------------
        // CHECK-OUT — registrar pago automático
        // Cuando la reserva se completa, se crea un pago con el
        // valor total de la reserva marcado como 'aprobado'.
        // Esto permite que los reportes reflejen el ingreso
        // inmediatamente sin necesidad de una pasarela real.
        // Nota en el pago: 'PAGO_DEMO' para identificarlo
        // fácilmente y poder eliminarlo cuando se integre
        // una pasarela de pagos real en producción.
        // --------------------------------------------------------
        if (nuevoEstado == EstadoReserva.completada) {

            // Verificar que no tenga ya un pago registrado
            // para evitar duplicados si se llama dos veces
            List<Pago> pagosExistentes = pagoRepository
                .findByReservaId(reserva.getId());

            if (pagosExistentes.isEmpty()) {
                Pago pago = Pago.builder()
                    .reserva(reserva)
                    .monto(reserva.getValorTotal())
                    .estadoPago(EstadoPago.aprobado)
                    .metodo(reserva.getFormaPago())
                    .fechaPago(LocalDateTime.now())
                    .notas("PAGO_DEMO")
                    .build();

                pagoRepository.save(pago);
            }

            // Liberar la habitación
            habitacionService.cambiarEstado(
                reserva.getHabitacion().getId(),
                Habitacion.Estado.disponible
            );
        }

        // Cancelada — solo liberar habitación sin registrar pago
        if (nuevoEstado == EstadoReserva.cancelada) {
            habitacionService.cambiarEstado(
                reserva.getHabitacion().getId(),
                Habitacion.Estado.disponible
            );
        }

        return ReservaResponse.from(reservaRepository.save(reserva));
    }

    // Eliminar reserva — solo admin
    @Transactional
    public void eliminar(Integer id) {
        Reserva reserva = obtenerEntidad(id);
        habitacionService.cambiarEstado(
            reserva.getHabitacion().getId(),
            Habitacion.Estado.disponible
        );
        reservaRepository.delete(reserva);
    }

    // Reservas activas hoy
    public List<ReservaResponse> reservasDeHoy() {
        return reservaRepository.findReservasActivas(LocalDate.now())
            .stream()
            .map(ReservaResponse::from)
            .toList();
    }

    // Método interno
    public Reserva obtenerEntidad(Integer id) {
        return reservaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Reserva no encontrada con id: " + id
            ));
    }
}