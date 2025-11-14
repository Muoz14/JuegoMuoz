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

    protected boolean immuneToMeteors = false;
    protected boolean lastHitByPlayer = false;

    public MovingObject(Vector2D position, Vector2D velocity, double maxVel, java.awt.image.BufferedImage texture, GameState gameState) {
        super(position, texture);
        this.velocity = velocity;
        this.maxVel = maxVel;
        this.gameState = gameState;
        this.angle = 0;
        this.width = texture.getWidth();
        this.height = texture.getHeight();
    }

    public Vector2D getCenter() {
        return new Vector2D(position.getX() + width / 2.0, position.getY() + height / 2.0);
    }

    public Rectangle getBounds() {
        return new Rectangle((int) position.getX(), (int) position.getY(), width, height);
    }

    protected void collidesWith() {
        if (isSpawnImmune()) return;

        ArrayList<MovingObject> movingObjects = gameState.getMovingObjects();

        for (MovingObject m : movingObjects) {
            if (m.equals(this)) continue;
            if (m.isSpawnImmune()) continue;

            // --- REGLAS DE IGNORAR (MODIFICADAS) ---
            if (this instanceof Ufo && m instanceof Laser && !((Laser) m).isPlayerLaser()) continue;
            if (this instanceof Player && m instanceof Laser && ((Laser) m).isPlayerLaser()) continue;
            if ((this instanceof Ufo && m instanceof Meteor) || (m instanceof Ufo && this instanceof Meteor)) continue;
            if (this instanceof Laser && m instanceof MiniBoss) continue;
            if ((this instanceof MiniBoss && m instanceof Meteor) || (m instanceof MiniBoss && this instanceof Meteor)) continue;
            if ((this instanceof Ufo && m instanceof MiniBoss) || (this instanceof MiniBoss && m instanceof Ufo)) continue;
            if (this instanceof PowerUp || m instanceof PowerUp) continue;

            // --- INICIO DE LAS NUEVAS REGLAS (RAIDER) ---
            // El Raider ignora al UFO y al MiniBoss (y viceversa)
            if (this instanceof Raider && (m instanceof Ufo || m instanceof MiniBoss)) continue;
            if (m instanceof Raider && (this instanceof Ufo || this instanceof MiniBoss)) continue;

            // El láser enemigo ignora al Raider (ya que es disparado por él)
            if (this instanceof Laser && !((Laser)this).isPlayerLaser() && m instanceof Raider) continue;
            if (m instanceof Laser && !((Laser)m).isPlayerLaser() && this instanceof Raider) continue;

            // El Raider destruye meteoros al contacto, pero el Raider no se destruye
            if (this instanceof Raider && m instanceof Meteor) {
                m.Destroy(); // Destruye el meteoro
                continue;    // El Raider (this) sobrevive y continúa
            }
            if (m instanceof Raider && this instanceof Meteor) {
                this.Destroy(); // Destruye el meteoro (this)
                continue;    // El Raider (m) sobrevive y continúa
            }
            // --- FIN DE LAS NUEVAS REGLAS (RAIDER) ---


            // --- INICIO DE LA SOLUCION: HITBOX DE BURBUJA ---

            // 1. Definir los radios de colision base
            double myRadius = this.width / 2.0;
            double theirRadius = m.width / 2.0;

            // 2. Comprobar si alguno es el jugador con escudo
            if (this instanceof Player && ((Player)this).isShielded()) {
                // Usar un radio de escudo mas grande (ej. 70 pixeles)
                // Tus animaciones de escudo miden ~140px, asi que 70 es un buen radio.
                myRadius = 70.0;
            } else if (m instanceof Player && ((Player)m).isShielded()) {
                theirRadius = 70.0; // Usar el mismo radio
            }

            // 3. Calcular la distancia y usar los radios (posiblemente) modificados
            double distance = m.getCenter().subtract(getCenter()).getMagnitude();

            if (distance < theirRadius + myRadius) {
                // --- FIN DE LA SOLUCION ---

                // --- LOGICA DE COLISION (LA QUE YA TENIAMOS) ---

                Player shieldedPlayer = null;
                MovingObject other = null;

                if (this instanceof Player && ((Player)this).isShielded()) {
                    shieldedPlayer = (Player) this;
                    other = m;
                } else if (m instanceof Player && ((Player)m).isShielded()) {
                    shieldedPlayer = (Player) m;
                    other = this;
                }

                // ESCENARIO 1: Jugador con escudo esta involucrado
                if (shieldedPlayer != null) {

                    // Caso 1.1: Escudo vs Laser Enemigo
                    if (other instanceof Laser && !((Laser)other).isPlayerLaser()) {
                        gameState.playExplosion(other.getCenter());
                        other.Destroy();
                        return;
                    }

                    // Caso 1.2: Escudo vs MiniBoss
                    if (other instanceof MiniBoss) {
                        shieldedPlayer.deactivateShield();
                        gameState.addMessage(new Message(
                                shieldedPlayer.getCenter(), false, "ESCUDO ROTO!", Color.RED, true, Assets.fontMed, gameState
                        ));
                        gameState.playExplosion(shieldedPlayer.getCenter());
                        shieldedPlayer.triggerPostHitImmunity(true);

                        Vector2D knockback = other.getCenter().subtract(shieldedPlayer.getCenter()).normalize().scale(5);
                        other.velocity = other.velocity.add(knockback).limit(other.maxVel);

                        return;
                    }

                    // Caso 1.3: Escudo vs Meteor o UFO
                    if (other instanceof Meteor || other instanceof Ufo) {
                        other.velocity = other.velocity.scale(-1);
                        Vector2D separation = other.getCenter().subtract(shieldedPlayer.getCenter()).normalize().scale(5);
                        other.position = other.position.add(separation);

                        return;
                    }
                }

                // ESCENARIO 2: Colision normal (sin escudo)
                objectCollision(this, m);
            }
        }
    }

    private void objectCollision(MovingObject a, MovingObject b) {
        if ((a instanceof Player && ((Player) a).isSpawning()) || (b instanceof Player && ((Player) b).isSpawning())) return;

        if (a instanceof Laser && ((Laser) a).isPlayerLaser()) b.lastHitByPlayer = true;
        if (b instanceof Laser && ((Laser) b).isPlayerLaser()) a.lastHitByPlayer = true;

        // --- INICIO DE LA MODIFICACIÓN ---
        // El láser enemigo (Raider/UFO) destruye meteoritos pero el láser sobrevive
        if (a instanceof Laser && !((Laser)a).isPlayerLaser() && b instanceof Meteor) {
            gameState.playExplosion(b.getCenter()); // <-- AÑADIDO
            b.Destroy(); // Meteoro destruido
            return;      // Láser (a) sobrevive
        }
        if (b instanceof Laser && !((Laser)b).isPlayerLaser() && a instanceof Meteor) {
            gameState.playExplosion(a.getCenter()); // <-- AÑADIDO
            a.Destroy(); // Meteoro destruido
            return;      // Láser (b) sobrevive
        }
        // --- FIN DE LA MODIFICACIÓN ---

        if (a instanceof Meteor && b instanceof Meteor) {
            return;
        }

        if (a instanceof Player && b instanceof MiniBoss) {
            gameState.playExplosion(a.getCenter());
            a.Destroy();
            return;
        }
        if (a instanceof MiniBoss && b instanceof Player) {
            gameState.playExplosion(b.getCenter());
            b.Destroy();
            return;
        }

        // --- NUEVA REGLA: El láser enemigo (Raider/UFO) no daña al MiniBoss ni al UFO ---
        // (Esto también previene que los UFOs se disparen entre ellos)
        if (a instanceof Laser && !((Laser)a).isPlayerLaser() && (b instanceof MiniBoss || b instanceof Ufo)) {
            return; // No hacer nada
        }
        if (b instanceof Laser && !((Laser)b).isPlayerLaser() && (a instanceof MiniBoss || a instanceof Ufo)) {
            return; // No hacer nada
        }

        // Colisión por defecto: destruir ambos
        gameState.playExplosion(a.getCenter());
        a.Destroy();
        b.Destroy();
    }

    protected boolean isSpawnImmune() {
        return false;
    }

    protected void Destroy() {
        gameState.removeObject(this);

        if (lastHitByPlayer) {
            Assets.explosion.play();
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
        return distance < other.width / 2.0 + width / 2.0;
    }
}