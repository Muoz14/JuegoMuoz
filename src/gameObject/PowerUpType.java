package gameObject;

import graphics.Assets;
import java.awt.image.BufferedImage;

/**
 * Enum interno para categorizar los power-ups.
 */
enum PowerUpCategory {
    SHIELD,
    RAPID_FIRE,
    MULTI_SHOT,
    EXTRA_LIFE,
    SCORE_MULTIPLIER,
    RANDOM // --- NUEVO ---
}

/**
 * Define cada tipo de power-up disponible en el juego.
 */
public enum PowerUpType {

    // --- ESCUDOS ---
    BRONZE(
            Assets.shield_bronze, 5000, "ESCUDO LVL 1", PowerUpCategory.SHIELD
    ),
    SILVER(
            Assets.shield_silver, 10000, "ESCUDO LVL 2", PowerUpCategory.SHIELD
    ),
    GOLD(
            Assets.shield_gold, 15000, "ESCUDO LVL 3", PowerUpCategory.SHIELD
    ),

    // --- OFENSIVOS ---
    RAPID_FIRE(
            Assets.speed_shoot, 8500, "¡DISPARO RAPIDO!", PowerUpCategory.RAPID_FIRE
    ),
    MULTI_SHOT(
            Assets.extra_gun, 10000, "¡MULTI-DISPARO!", PowerUpCategory.MULTI_SHOT
    ),

    // --- UTILIDAD Y PUNTOS ---
    EXTRA_LIFE(
            Assets.extraLife, 0, "¡VIDA EXTRA!", PowerUpCategory.EXTRA_LIFE
    ),
    DOUBLE_POINTS(
            Assets.double_points, 15000, "¡PUNTOS X2!", PowerUpCategory.SCORE_MULTIPLIER
    ),

    // --- NUEVO: ALEATORIO ---
    RANDOM_POWER(
            Assets.random_powerUp,
            0,                     // Duracion 0 (es instantaneo)
            "¿...?",               // Mensaje al recogerlo
            PowerUpCategory.RANDOM
    );
    // --- FIN NUEVO ---


    // --- Propiedades de cada Power-Up ---
    public final BufferedImage texture;
    public final long duration;
    public final String message;
    public final PowerUpCategory category;

    /**
     * Constructor para cada tipo de power-up.
     */
    PowerUpType(BufferedImage texture, long duration, String message, PowerUpCategory category) {
        this.texture = texture;
        this.duration = duration;
        this.message = message;
        this.category = category;
    }
}