package gameObject;

import graphics.Assets;
import math.Vector2D;
import java.awt.image.BufferedImage;
import java.util.List;

public class ShipData {

    public String name;
    public BufferedImage texture;
    public double maxVelocity;
    public double fireRate;
    public List<Vector2D> gunOffsets;       // posiciones relativas de las armas
    public List<Vector2D> thrusterOffsets;  // posiciones relativas de los propulsores
    public BufferedImage thrusterTexture; // cada nave tiene su propio propulsor
    public BufferedImage laserTexture;

    public ShipData(String name, BufferedImage texture, double maxVelocity, double fireRate,
                    List<Vector2D> gunOffsets, List<Vector2D> thrusterOffsets, BufferedImage thrusterTexture, BufferedImage laserTexture) {

        this.name = name;
        this.texture = texture;
        this.maxVelocity = maxVelocity;
        this.fireRate = fireRate;
        this.gunOffsets = gunOffsets;
        this.thrusterOffsets = thrusterOffsets;
        this.thrusterTexture = thrusterTexture;
        this.laserTexture = laserTexture;

    }

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
