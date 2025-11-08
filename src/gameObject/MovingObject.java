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

    // Deteccion de colisiones
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

            // El MiniBoss maneja sus PROPIAS colisiones con Lasers
            if (this instanceof Laser && m instanceof MiniBoss) continue;

            // El MiniBoss tambien es inmune a meteoros
            if ((this instanceof MiniBoss && m instanceof Meteor) || (m instanceof MiniBoss && this instanceof Meteor)) continue;


            // ---------- INICIO DE LA SOLUCION ----------
            // Evitar que Ufo y MiniBoss colisionen entre si
            if ((this instanceof Ufo && m instanceof MiniBoss) || (this instanceof MiniBoss && m instanceof Ufo)) continue;
            // ---------- FIN DE LA SOLUCION ----------


            double distance = m.getCenter().subtract(getCenter()).getMagnitude();
            if (distance < m.width / 2 + width / 2) objectCollision(this, m);
        }
    }

    private void objectCollision(MovingObject a, MovingObject b) {
        // Evitar colisiones si el jugador esta reapareciendo
        if ((a instanceof Player && ((Player) a).isSpawning()) || (b instanceof Player && ((Player) b).isSpawning())) return;

        // Marcar que fue golpeado por el jugador
        if (a instanceof Laser && ((Laser) a).isPlayerLaser()) b.lastHitByPlayer = true;
        if (b instanceof Laser && ((Laser) b).isPlayerLaser()) a.lastHitByPlayer = true;

        // Evitar que meteoros se destruyan entre si
        if (a instanceof Meteor && b instanceof Meteor) {
            return;
        }

        // REGLA ESPECIAL: Si la colision es Player vs MiniBoss, solo destruir al Player.
        if (a instanceof Player && b instanceof MiniBoss) {
            gameState.playExplosion(a.getCenter()); // Explosion del jugador
            a.Destroy(); // Destruye al jugador (a)
            return; // El MiniBoss (b) sobrevive
        }
        if (a instanceof MiniBoss && b instanceof Player) {
            gameState.playExplosion(b.getCenter()); // Explosion del jugador
            b.Destroy(); // Destruye al jugador (b)
            return; // El MiniBoss (a) sobrevive
        }

        // (Ya no es necesario el chequeo de Ufo vs MiniBoss aqui, porque lo filtramos en collidesWith)

        // Logica original (para Player vs Meteor, Player vs UFO, etc.)
        // Si no es una de las excepciones de arriba, destruir ambos objetos.
        gameState.playExplosion(getCenter());
        a.Destroy();
        b.Destroy();
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

    public boolean collides(MovingObject other) {
        double distance = other.getCenter().subtract(getCenter()).getMagnitude();
        return distance < other.width / 2 + width / 2;
    }

}