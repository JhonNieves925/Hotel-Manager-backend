package co.hotelmanager.backend.service;

import co.hotelmanager.backend.dto.response.ReservaResponse;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // Se ejecuta en un hilo separado para no bloquear la respuesta al cliente
    @Async
    public void enviarConfirmacionReserva(ReservaResponse reserva, String emailOverride, String nombreOverride) {
        // Usar el email del request directamente (más confiable que el lazy-loaded del response)
        String emailDestino = (emailOverride != null && !emailOverride.isBlank())
            ? emailOverride
            : reserva.getEmailHuesped();
        String nombreDestino = (nombreOverride != null && !nombreOverride.isBlank())
            ? nombreOverride
            : reserva.getNombreHuesped();

        if (emailDestino == null || emailDestino.isBlank()) {
            log.info("Reserva #{} sin email — no se envía confirmación", reserva.getId());
            return;
        }

        // Inyectar email y nombre en el response para construir el HTML correctamente
        reserva = ReservaResponse.builder()
            .id(reserva.getId())
            .numeroHabitacion(reserva.getNumeroHabitacion())
            .tipoHabitacion(reserva.getTipoHabitacion())
            .fechaEntrada(reserva.getFechaEntrada())
            .fechaSalida(reserva.getFechaSalida())
            .noches(reserva.getNoches())
            .valorTotal(reserva.getValorTotal())
            .formaPago(reserva.getFormaPago())
            .estadoReserva(reserva.getEstadoReserva())
            .createdAt(reserva.getCreatedAt())
            .nombreHuesped(nombreDestino)
            .apellidoHuesped(reserva.getApellidoHuesped())
            .telefonoHuesped(reserva.getTelefonoHuesped())
            .emailHuesped(emailDestino)
            .build();

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setTo(emailDestino);
            helper.setSubject("✅ Reserva confirmada — Hotel Manager #" + reserva.getId());
            helper.setText(construirHtml(reserva), true);

            mailSender.send(mensaje);
            log.info("Email de confirmación enviado a {} para reserva #{}", reserva.getEmailHuesped(), reserva.getId());

        } catch (Exception e) {
            // Si el email falla NO se interrumpe el flujo — solo se loguea
            log.error("Error enviando email para reserva #{}: {}", reserva.getId(), e.getMessage());
        }
    }

    private String construirHtml(ReservaResponse r) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "CO"));

        String fechaEntrada = r.getFechaEntrada() != null ? r.getFechaEntrada().format(fmt) : "—";
        String fechaSalida  = r.getFechaSalida()  != null ? r.getFechaSalida().format(fmt)  : "—";

        String numeroFactura = "FAC-" + java.time.Year.now().getValue()
            + "-" + String.format("%06d", r.getId());

        String totalFormateado = String.format("%,.0f", r.getValorTotal()).replace(",", ".");

        String formaPagoTexto = switch (r.getFormaPago()) {
            case "efectivo"        -> "Efectivo";
            case "tarjeta_credito" -> "Tarjeta de crédito";
            case "tarjeta_debito"  -> "Tarjeta de débito";
            case "transferencia"   -> "Transferencia bancaria";
            case "pse"             -> "PSE";
            case "nequi"           -> "Nequi";
            case "daviplata"       -> "Daviplata";
            default                -> r.getFormaPago();
        };

        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Confirmación de Reserva</title>
            </head>
            <body style="margin:0;padding:0;background:#f1f5f9;font-family:Helvetica,Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f1f5f9;padding:32px 16px;">
                <tr><td align="center">
                  <table width="600" cellpadding="0" cellspacing="0" style="max-width:600px;width:100%%;">

                    <!-- CABECERA -->
                    <tr>
                      <td style="background:#2563eb;border-radius:12px 12px 0 0;padding:28px 32px;">
                        <table width="100%%" cellpadding="0" cellspacing="0">
                          <tr>
                            <td>
                              <div style="font-size:22px;font-weight:900;color:#fff;margin-bottom:4px;">Hotel Manager</div>
                              <div style="font-size:12px;color:rgba(255,255,255,0.75);">Cali, Valle del Cauca — Colombia</div>
                            </td>
                            <td align="right">
                              <div style="background:rgba(255,255,255,0.15);border-radius:8px;padding:8px 14px;display:inline-block;">
                                <div style="font-size:10px;color:rgba(255,255,255,0.7);margin-bottom:2px;">RESERVA</div>
                                <div style="font-size:18px;font-weight:900;color:#fff;">#%d</div>
                              </div>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>

                    <!-- MENSAJE BIENVENIDA -->
                    <tr>
                      <td style="background:#fff;padding:28px 32px 20px;">
                        <div style="font-size:20px;font-weight:700;color:#1e293b;margin-bottom:8px;">
                          ¡Tu reserva está confirmada, %s!
                        </div>
                        <div style="font-size:14px;color:#64748b;line-height:1.7;">
                          Hemos registrado exitosamente tu reserva en Hotel Manager.
                          A continuación encontrarás todos los detalles de tu estadía.
                        </div>
                      </td>
                    </tr>

                    <!-- DATOS ESTADÍA -->
                    <tr>
                      <td style="background:#fff;padding:0 32px 24px;">
                        <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f8fafc;border-radius:10px;overflow:hidden;">
                          <tr style="background:#e2e8f0;">
                            <td colspan="2" style="padding:12px 16px;font-size:11px;font-weight:700;letter-spacing:1px;color:#64748b;text-transform:uppercase;">
                              Detalle de la estadía
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:10px 16px;font-size:13px;color:#64748b;border-bottom:1px solid #e2e8f0;">Habitación</td>
                            <td style="padding:10px 16px;font-size:13px;font-weight:600;color:#1e293b;text-align:right;border-bottom:1px solid #e2e8f0;">%s — Nro. %s</td>
                          </tr>
                          <tr>
                            <td style="padding:10px 16px;font-size:13px;color:#64748b;border-bottom:1px solid #e2e8f0;">Check-in</td>
                            <td style="padding:10px 16px;font-size:13px;font-weight:600;color:#1e293b;text-align:right;border-bottom:1px solid #e2e8f0;">%s</td>
                          </tr>
                          <tr>
                            <td style="padding:10px 16px;font-size:13px;color:#64748b;border-bottom:1px solid #e2e8f0;">Check-out</td>
                            <td style="padding:10px 16px;font-size:13px;font-weight:600;color:#1e293b;text-align:right;border-bottom:1px solid #e2e8f0;">%s</td>
                          </tr>
                          <tr>
                            <td style="padding:10px 16px;font-size:13px;color:#64748b;border-bottom:1px solid #e2e8f0;">Noches</td>
                            <td style="padding:10px 16px;font-size:13px;font-weight:600;color:#1e293b;text-align:right;border-bottom:1px solid #e2e8f0;">%d</td>
                          </tr>
                          <tr>
                            <td style="padding:10px 16px;font-size:13px;color:#64748b;border-bottom:1px solid #e2e8f0;">Forma de pago</td>
                            <td style="padding:10px 16px;font-size:13px;font-weight:600;color:#1e293b;text-align:right;border-bottom:1px solid #e2e8f0;">%s</td>
                          </tr>
                          <tr>
                            <td style="padding:12px 16px;font-size:14px;font-weight:700;color:#1e293b;">Total</td>
                            <td style="padding:12px 16px;font-size:18px;font-weight:900;color:#2563eb;text-align:right;">$%s</td>
                          </tr>
                        </table>
                      </td>
                    </tr>

                    <!-- N° FACTURA -->
                    <tr>
                      <td style="background:#fff;padding:0 32px 28px;">
                        <div style="background:#eff6ff;border:1px solid #bfdbfe;border-radius:8px;padding:12px 16px;">
                          <table width="100%%" cellpadding="0" cellspacing="0">
                            <tr>
                              <td style="font-size:12px;color:#3b82f6;">
                                Número de factura: <strong>%s</strong>
                              </td>
                              <td align="right" style="font-size:12px;color:#64748b;">
                                Guarda este número como soporte
                              </td>
                            </tr>
                          </table>
                        </div>
                      </td>
                    </tr>

                    <!-- INFORMACIÓN HOTEL -->
                    <tr>
                      <td style="background:#fff;padding:0 32px 28px;">
                        <table width="100%%" cellpadding="0" cellspacing="0" style="border-top:1px solid #e2e8f0;padding-top:20px;">
                          <tr>
                            <td style="font-size:13px;color:#64748b;line-height:1.8;">
                              <strong style="color:#1e293b;">📍 Ubicación:</strong> Cali, Valle del Cauca, Colombia<br>
                              <strong style="color:#1e293b;">📞 Teléfono:</strong> (2) 123-4567<br>
                              <strong style="color:#1e293b;">🕐 Check-in:</strong> Desde las 3:00 PM<br>
                              <strong style="color:#1e293b;">🕐 Check-out:</strong> Hasta las 12:00 PM
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>

                    <!-- PIE -->
                    <tr>
                      <td style="background:#1e293b;border-radius:0 0 12px 12px;padding:20px 32px;text-align:center;">
                        <div style="font-size:12px;color:rgba(255,255,255,0.6);line-height:1.8;">
                          Gracias por elegir Hotel Manager<br>
                          Este correo es una confirmación automática, por favor no responder
                        </div>
                      </td>
                    </tr>

                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(
                r.getId(),
                r.getNombreHuesped(),
                r.getTipoHabitacion(), r.getNumeroHabitacion(),
                fechaEntrada,
                fechaSalida,
                r.getNoches(),
                formaPagoTexto,
                totalFormateado,
                numeroFactura
        );
    }
}