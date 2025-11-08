package gameObject;

import graphics.Assets;
import math.Vector2D;
import states.GameState;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Meteor extends MovingObject {

    protected Size size;
    protected int family;
    private AffineTransform at;

    public Meteor(Vector2D position, Vector2D velocity, double maxVel, BufferedImage texture, GameState gameState, Size size, int family) {
        super(position, velocity, maxVel, texture, gameState);
        this.size = size;
        this.family = family;
        this.velocity = velocity.scale(maxVel);
        this.immuneToMeteors = false;
    }

    @Override
    protected void Destroy() {
        gameState.removeObject(this);

        // Reproducir sonido y agregar puntuacion solo si fue destruido por el jugador
        if (lastHitByPlayer) {
            gameState.addScore(Constants.METEOR_SCORE, position);
            Assets.explosion.play(); // <- AHORA USA EL ASSET PRECARGADO
        }

        // Dividir meteoros
        if (size != Size.TINY) {
            int nextSizeIndex = size.ordinal() + 1;
            Size nextSize = Size.values()[nextSizeIndex];

            for (int i = 0; i < size.quantity; i++) {
                Vector2D newVel = new Vector2D(Math.random() * 2 - 1, Math.random() * 2 - 1)
                        .normalize()
                        .scale(Math.random() * 2 + 1);

                Meteor newMeteor = new Meteor(
                        getCenter(),
                        newVel,
                        2,
                        nextSize.getTexture(family),
                        gameState,
                        nextSize,
                        family
                );
                gameState.addObject(newMeteor);
            }
        }
    }

    @Override
    public void update() {
        position = position.add(velocity);

        if (position.getX() > Constants.WIDTH) position.setX(-width);
        if (position.getY() > Constants.HEIGHT) position.setY(-height);
        if (position.getX() < -width) position.setX(Constants.WIDTH);
        if (position.getY() < -height) position.setY(Constants.HEIGHT);

        angle += Constants.DELTAANGLE;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        at = AffineTransform.getTranslateInstance(position.getX(), position.getY());
        at.rotate(angle, width / 2, height / 2);
        g2d.drawImage(texture, at, null);
    }

    // Getters y Setters
    public Vector2D getVelocity() { return velocity; }
    public void setVelocity(Vector2D velocity) { this.velocity = velocity; }
    public BufferedImage getTexture() { return texture; }
    public Size getSize() { return size; }
    public int getFamily() { return family; }
}