package gameObject;

import graphics.Assets;
import java.awt.image.BufferedImage;

/**
 * Enum interno para categorizar los power-ups.
 * Esto le dice a PowerUp.java y Player.java que logica ejecutar.
 */
enum PowerUpCategory {
    SHIELD,
    RAPID_FIRE,
    MULTI_SHOT
    // Aqui puedes anadir mas en el futuro: EXTRA_LIFE, SCORE_MULTIPLIER, etc.
}

/**
 * Define cada tipo de power-up disponible en el juego.
 */
public enum PowerUpType {

    // --- ESCUDOS ---
    BRONZE(
            Assets.shield_bronze,
            8500,
            "ESCUDO LVL 1",
            PowerUpCategory.SHIELD
    ),
    SILVER(
            Assets.shield_silver,
            12000,
            "ESCUDO LVL 2",
            PowerUpCategory.SHIELD
    ),
    GOLD(
            Assets.shield_gold,
            18000,
            "ESCUDO LVL 3",
            PowerUpCategory.SHIELD
    ),

    // --- DISPARO RAPIDO ---
    RAPID_FIRE(
            Assets.speed_shoot,
            12000,
            "¡DISPARO RAPIDO!",
            PowerUpCategory.RAPID_FIRE
    ),

    // --- MULTI-DISPARO ---
    MULTI_SHOT(
            Assets.extra_gun,
            18000,
            "¡MULTI-DISPARO!",
            PowerUpCategory.MULTI_SHOT
    );

    // --- Propiedades de cada Power-Up ---
    public final BufferedImage texture;
    public final long duration;
    public final String message;
    public final PowerUpCategory category;

    /**
     * Constructor para cada tipo de power-up.
     * @param texture La imagen del icono que dropea.
     * @param duration Cuanto dura el efecto en milisegundos.
     * @param message El texto que aparece al recogerlo.
     * @param category La categoria logica a la que pertenece.
     */
    PowerUpType(BufferedImage texture, long duration, String message, PowerUpCategory category) {
        this.texture = texture;
        this.duration = duration;
        this.message = message;
        this.category = category;
    }
}