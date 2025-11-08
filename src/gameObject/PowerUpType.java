package gameObject;

import graphics.Assets;
import java.awt.image.BufferedImage;

// Un Enum para definir los 3 niveles de escudo
public enum PowerUpType {

    // Nivel 1: Bronce
    BRONZE(
            Assets.shield_bronze,  // Icono
            5000,                  // Duracion (5 segundos)
            "ESCUDO LVL 1"         // Mensaje
    ),

    // Nivel 2: Plata
    SILVER(
            Assets.shield_silver,  // Icono
            10000,                 // Duracion (10 segundos)
            "ESCUDO LVL 2"         // Mensaje
    ),

    // Nivel 3: Oro
    GOLD(
            Assets.shield_gold,    // Icono
            15000,                 // Duracion (15 segundos)
            "ESCUDO LVL 3"         // Mensaje
    );

    // Variables de cada tipo
    public final BufferedImage texture;
    public final long duration;
    public final String message;

    PowerUpType(BufferedImage texture, long duration, String message) {
        this.texture = texture;
        this.duration = duration;
        this.message = message;
    }
}