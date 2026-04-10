package co.hotelmanager.backend.service;

import co.hotelmanager.backend.model.PushSubscription;
import co.hotelmanager.backend.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.security.Security;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionPushService {

    @Value("${app.vapid.public-key}")
    private String vapidPublicKey;

    @Value("${app.vapid.private-key}")
    private String vapidPrivateKey;

    @Value("${spring.mail.username}")
    private String vapidSubject;

    private final PushSubscriptionRepository pushSubscriptionRepository;

    private PushService webPushService;

    @PostConstruct
    public void init() throws Exception {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        webPushService = new PushService(vapidPublicKey, vapidPrivateKey, "mailto:" + vapidSubject);
    }

    @Async
    public void enviarNotificacion(Integer idHuespedUsuario, String titulo, String cuerpo) {
        List<PushSubscription> subs = pushSubscriptionRepository.findByIdHuespedUsuario(idHuespedUsuario);
        if (subs.isEmpty()) return;

        String payload = String.format(
            "{\"title\":\"%s\",\"body\":\"%s\",\"icon\":\"/huesped/icons/icon-192.png\"}",
            titulo.replace("\"", "'"),
            cuerpo.replace("\"", "'")
        );

        for (PushSubscription sub : subs) {
            try {
                Subscription.Keys keys = new Subscription.Keys(sub.getP256dh(), sub.getAuth());
                Subscription subscription = new Subscription(sub.getEndpoint(), keys);
                Notification notification = new Notification(subscription, payload);
                webPushService.send(notification);
            } catch (Exception e) {
                log.warn("Error enviando push a endpoint {}: {}", sub.getEndpoint(), e.getMessage());
                if (e.getMessage() != null && e.getMessage().contains("410")) {
                    pushSubscriptionRepository.deleteByEndpoint(sub.getEndpoint());
                }
            }
        }
    }

    @Transactional
    public void guardarSuscripcion(Integer idHuespedUsuario, String endpoint, String p256dh, String auth) {
        pushSubscriptionRepository.findByEndpoint(endpoint).ifPresentOrElse(
            existing -> {
                existing.setIdHuespedUsuario(idHuespedUsuario);
                existing.setP256dh(p256dh);
                existing.setAuth(auth);
                pushSubscriptionRepository.save(existing);
            },
            () -> {
                PushSubscription sub = PushSubscription.builder()
                    .idHuespedUsuario(idHuespedUsuario)
                    .endpoint(endpoint)
                    .p256dh(p256dh)
                    .auth(auth)
                    .build();
                pushSubscriptionRepository.save(sub);
            }
        );
    }

    @Transactional
    public void eliminarSuscripcion(String endpoint) {
        pushSubscriptionRepository.deleteByEndpoint(endpoint);
    }

    public String getVapidPublicKey() {
        return vapidPublicKey;
    }
}
