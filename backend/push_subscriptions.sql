-- Tabla para suscripciones Web Push
CREATE TABLE IF NOT EXISTS push_subscriptions (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_huesped_usuario  INT           NOT NULL,
    endpoint            VARCHAR(500)  NOT NULL,
    p256dh              VARCHAR(200)  NOT NULL,
    auth                VARCHAR(100)  NOT NULL,
    created_at          DATETIME,
    UNIQUE KEY uq_endpoint (endpoint),
    KEY idx_huesped (id_huesped_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
