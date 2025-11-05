package gameObject;

import graphics.Assets;
import graphics.Sound;
import math.Vector2D;
import states.GameState;
import java.awt.*;
import java.util.ArrayList;

public abstract class MovingObject extends GameObject {

    protected Vector2D velocity;
    protected double angle;
    protected double maxVel;
    protected int width, height;
    protected GameState gameState;

    protected boolean immuneToMeteors = false;  // Inmunidad a meteoritos
    protected boolean lastHitByPlayer = false;  // Saber si fue destruido por el jugador

    private Sound explosion;

    public MovingObject(Vector2D position, Vector2D velocity, double maxVel, java.awt.image.BufferedImage texture, GameState gameState) {
        super(position, texture);
        this.velocity = velocity;
        this.maxVel = maxVel;
        this.gameState = gameState;
        this.angle = 0;
        this.width = texture.getWidth();
        this.height = texture.getHeight();
        explosion = new Sound("/sounds/explosion.wav");
    }

    public Vector2D getCenter() {
        return new Vector2D(position.getX() + width / 2, position.getY() + height / 2);
    }

    public Rectangle getBounds() {
        return new Rectangle((int) position.getX(), (int) position.getY(), width, height);
    }

    // Detección de colisiones
    protected void collidesWith() {
        if (isSpawnImmune()) return;

        ArrayList<MovingObject> movingObjects = gameState.getMovingObjects();

        for (MovingObject m : movingObjects) {
            if (m.equals(this)) continue;
            if (m.isSpawnImmune()) continue;

            // Ignorar colisiones con propios lasers
            if (this instanceof Ufo && m instanceof Laser && !((Laser) m).isPlayerLaser()) continue;
            if (this instanceof Player && m instanceof Laser && ((Laser) m).isPlayerLaser()) continue;

            // Bloquear colisiones entre meteoros y UFO
            if ((this instanceof Ufo && m instanceof Meteor) || (m instanceof Ufo && this instanceof Meteor)) continue;

            double distance = m.getCenter().subtract(getCenter()).getMagnitude();
            if (distance < m.width / 2 + width / 2) objectCollision(this, m);
        }
    }

    private void objectCollision(MovingObject a, MovingObject b) {
        // Evitar colisiones si el jugador está reapareciendo
        if ((a instanceof Player && ((Player) a).isSpawning()) || (b instanceof Player && ((Player) b).isSpawning())) return;

        // Marcar que fue golpeado por el jugador
        if (a instanceof Laser && ((Laser) a).isPlayerLaser()) b.lastHitByPlayer = true;
        if (b instanceof Laser && ((Laser) b).isPlayerLaser()) a.lastHitByPlayer = true;

        // Evitar que meteoros se destruyan entre sí
        if (!(a instanceof Meteor && b instanceof Meteor)) {
            gameState.playExplosion(getCenter());
            a.Destroy();
            b.Destroy();
        }
    }

    protected boolean isSpawnImmune() {
        return false;
    }

    protected void Destroy() {

        gameState.removeObject(this);

        // Reproducir sonido solo si fue destruido por el jugador
        if (lastHitByPlayer) {
            explosion.play();
        }

    }

    @Override
    public void update() {}

    @Override
    public void draw(Graphics g) {}

    public void setLastHitByPlayer(boolean value) {
        lastHitByPlayer = value;
    }
}
