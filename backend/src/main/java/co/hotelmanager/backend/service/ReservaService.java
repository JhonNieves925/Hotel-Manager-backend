package co.hotelmanager.backend.service;

import co.hotelmanager.backend.dto.request.ReservaEditRequest;
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
import co.hotelmanager.backend.service.EmailService;
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
    private final EmailService      emailService;

    // ── Listar todas ──────────────────────────────────────────
    public List<ReservaResponse> listarTodas() {
        return reservaRepository.findAll()
            .stream()
            .map(ReservaResponse::from)
            .toList();
    }

    // ── Detalle de una reserva ────────────────────────────────
    public ReservaResponse obtenerPorId(Integer id) {
        return ReservaResponse.from(obtenerEntidad(id));
    }

    // ── Crear reserva ─────────────────────────────────────────
    @Transactional
    public ReservaResponse crear(ReservaRequest request, Usuario usuario) {

        Habitacion habitacion = habitacionService
            .obtenerEntidadPorId(request.getIdHabitacion());

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

        long noches = ChronoUnit.DAYS.between(
            request.getFechaEntrada(),
            request.getFechaSalida()
        );
        BigDecimal valorTotal = habitacion.getTipo()
            .getPrecioNoche()
            .multiply(BigDecimal.valueOf(noches));

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

        Huesped huesped = Huesped.builder()
            .reserva(reserva)
            .nombre(request.getHuesped().getNombre())
            .apellido(request.getHuesped().getApellido())
            .fechaNacimiento(request.getHuesped().getFechaNacimiento())
            .nacionalidad(request.getHuesped().getNacionalidad())
            .telefono(request.getHuesped().getTelefono())
            .email(request.getHuesped().getEmail())
            .tipoDocumento(request.getHuesped().getTipoDocumento())
            .numeroDocumento(request.getHuesped().getNumeroDocumento())
            .build();

        huespedRepository.save(huesped);

        habitacionService.cambiarEstado(
            habitacion.getId(),
            Habitacion.Estado.ocupada
        );

        ReservaResponse response = ReservaResponse.from(obtenerEntidad(reserva.getId()));

        // Enviar email de confirmación de forma asíncrona (no bloquea la respuesta)
        emailService.enviarConfirmacionReserva(response);

        return response;
    }

    // ── Editar reserva ────────────────────────────────────────
    @Transactional
    public ReservaResponse editar(Integer id, ReservaEditRequest request) {
        Reserva reserva = obtenerEntidad(id);

        // Solo se puede editar si está pendiente o confirmada
        if (reserva.getEstadoReserva() == EstadoReserva.completada ||
            reserva.getEstadoReserva() == EstadoReserva.cancelada) {
            throw new IllegalStateException(
                "No se puede editar una reserva con estado: " + reserva.getEstadoReserva()
            );
        }

        boolean cambioHabitacion = request.getIdHabitacion() != null &&
            !request.getIdHabitacion().equals(reserva.getHabitacion().getId());

        LocalDate nuevaEntrada = request.getFechaEntrada() != null
            ? request.getFechaEntrada() : reserva.getFechaEntrada();
        LocalDate nuevaSalida  = request.getFechaSalida() != null
            ? request.getFechaSalida()  : reserva.getFechaSalida();

        Habitacion nuevaHabitacion = cambioHabitacion
            ? habitacionService.obtenerEntidadPorId(request.getIdHabitacion())
            : reserva.getHabitacion();

        boolean cambioFechas = !nuevaEntrada.equals(reserva.getFechaEntrada()) ||
                               !nuevaSalida.equals(reserva.getFechaSalida());

        // Verificar disponibilidad si cambió algo
        if (cambioHabitacion || cambioFechas) {
            List<Reserva> conflictos = reservaRepository
                .findReservasActivasEnRango(nuevaHabitacion.getId(), nuevaEntrada, nuevaSalida)
                .stream()
                .filter(r -> !r.getId().equals(id))
                .toList();

            if (!conflictos.isEmpty()) {
                throw new HabitacionOcupadaException(
                    nuevaHabitacion.getNumero(),
                    nuevaEntrada.toString(),
                    nuevaSalida.toString()
                );
            }

            // Si cambió habitación: liberar la anterior, ocupar la nueva
            if (cambioHabitacion) {
                habitacionService.cambiarEstado(
                    reserva.getHabitacion().getId(), Habitacion.Estado.disponible
                );
                habitacionService.cambiarEstado(
                    nuevaHabitacion.getId(), Habitacion.Estado.ocupada
                );
            }

            // Recalcular valor total
            long noches = ChronoUnit.DAYS.between(nuevaEntrada, nuevaSalida);
            BigDecimal nuevoTotal = nuevaHabitacion.getTipo()
                .getPrecioNoche()
                .multiply(BigDecimal.valueOf(noches));

            reserva.setHabitacion(nuevaHabitacion);
            reserva.setFechaEntrada(nuevaEntrada);
            reserva.setFechaSalida(nuevaSalida);
            reserva.setValorTotal(nuevoTotal);
        }

        if (request.getFormaPago() != null) {
            reserva.setFormaPago(request.getFormaPago());
        }
        if (request.getObservaciones() != null) {
            reserva.setObservaciones(request.getObservaciones());
        }

        // Actualizar datos del huésped
        if (reserva.getHuesped() != null) {
            if (request.getNombreHuesped() != null && !request.getNombreHuesped().isBlank())
                reserva.getHuesped().setNombre(request.getNombreHuesped());
            if (request.getApellidoHuesped() != null && !request.getApellidoHuesped().isBlank())
                reserva.getHuesped().setApellido(request.getApellidoHuesped());
            if (request.getTelefonoHuesped() != null && !request.getTelefonoHuesped().isBlank())
                reserva.getHuesped().setTelefono(request.getTelefonoHuesped());
            if (request.getEmailHuesped() != null && !request.getEmailHuesped().isBlank())
                reserva.getHuesped().setEmail(request.getEmailHuesped());
            huespedRepository.save(reserva.getHuesped());
        }

        return ReservaResponse.from(reservaRepository.save(reserva));
    }

    // ── Cambiar estado ────────────────────────────────────────
    @Transactional
    public ReservaResponse cambiarEstado(Integer id, EstadoReserva nuevoEstado) {
        Reserva reserva = obtenerEntidad(id);
        reserva.setEstadoReserva(nuevoEstado);

        if (nuevoEstado == EstadoReserva.completada) {
            List<Pago> pagosExistentes = pagoRepository.findByReservaId(reserva.getId());
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
            habitacionService.cambiarEstado(
                reserva.getHabitacion().getId(),
                Habitacion.Estado.disponible
            );
        }

        if (nuevoEstado == EstadoReserva.cancelada) {
            habitacionService.cambiarEstado(
                reserva.getHabitacion().getId(),
                Habitacion.Estado.disponible
            );
        }

        return ReservaResponse.from(reservaRepository.save(reserva));
    }

    // ── Eliminar reserva ──────────────────────────────────────
    @Transactional
    public void eliminar(Integer id) {
        Reserva reserva = obtenerEntidad(id);
        habitacionService.cambiarEstado(
            reserva.getHabitacion().getId(),
            Habitacion.Estado.disponible
        );
        reservaRepository.delete(reserva);
    }

    // ── Reservas activas hoy ──────────────────────────────────
    public List<ReservaResponse> reservasDeHoy() {
        return reservaRepository.findReservasActivas(LocalDate.now())
            .stream()
            .map(ReservaResponse::from)
            .toList();
    }

    // ── Método interno ────────────────────────────────────────
    public Reserva obtenerEntidad(Integer id) {
        return reservaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Reserva no encontrada con id: " + id
            ));
    }
}