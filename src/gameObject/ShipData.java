package gameObject;

import graphics.Assets;
import math.Vector2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Clase que actua como un contenedor de datos para las estadisticas
 * y componentes de una nave especifica.
 */
public class ShipData {

    public String name;
    public BufferedImage texture;
    public double maxVelocity;
    public double fireRate;

    // Lista de canones base
    public List<Vector2D> gunOffsets;

    // Lista de canones cuando el power-up MULTI_SHOT esta activo
    public List<Vector2D> upgradedGunOffsets;

    // Lista de propulsores
    public List<Vector2D> thrusterOffsets;

    public BufferedImage thrusterTexture;
    public BufferedImage laserTexture;

    /**
     * Constructor completo para una nave.
     */
    public ShipData(String name, BufferedImage texture, double maxVelocity, double fireRate,
                    List<Vector2D> gunOffsets, List<Vector2D> upgradedGunOffsets,
                    List<Vector2D> thrusterOffsets, BufferedImage thrusterTexture, BufferedImage laserTexture) {

        this.name = name;
        this.texture = texture;
        this.maxVelocity = maxVelocity;
        this.fireRate = fireRate;
        this.gunOffsets = gunOffsets;
        this.upgradedGunOffsets = upgradedGunOffsets; // Lista de disparos mejorados
        this.thrusterOffsets = thrusterOffsets;
        this.thrusterTexture = thrusterTexture;
        this.laserTexture = laserTexture;
    }

    // --- Getters ---

    public BufferedImage getLaserTexture() {
        return laserTexture;
    }

    public String getName() {
        return name;
    }

    public BufferedImage getTexture() {
        return texture;
    }

    public double getMaxVelocity() {
        return maxVelocity;
    }

    public double getFireRate() {
        return fireRate;
    }

    public List<Vector2D> getGunOffsets() {
        return gunOffsets;
    }

    public List<Vector2D> getThrusterOffsets() {
        return thrusterOffsets;
    }
}