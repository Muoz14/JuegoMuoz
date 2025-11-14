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
            if ((m instanceof Raider) && this instanceof Meteor) {
                this.Destroy(); // Destruye el meteoro (this)
                continue;    // El Raider (m) sobrevive y continúa
            }
            // --- FIN DE LAS NUEVAS REGLAS (RAIDER) ---

            // --- INICIO DE NUEVAS REGLAS (JEFE FINAL Y ESBIRRO) ---

            // Jefe/Esbirro vs Meteoros: Destruyen meteoro, ellos sobreviven
            if ((this instanceof FinalBoss || this instanceof Minion) && m instanceof Meteor) {
                m.Destroy();
                continue;
            }
            if ((m instanceof FinalBoss || m instanceof Minion) && this instanceof Meteor) {
                this.Destroy();
                continue;
            }

            // Jefe/Esbirro vs UFO: Se ignoran
            if ((this instanceof FinalBoss || this instanceof Minion) && m instanceof Ufo) continue;
            if ((m instanceof FinalBoss || m instanceof Minion) && this instanceof Ufo) continue;

            // Jefe/Esbirro vs Raider: Se ignoran
            if ((this instanceof FinalBoss || this instanceof Minion) && m instanceof Raider) continue;
            if ((m instanceof FinalBoss || m instanceof Minion) && this instanceof Raider) continue;

            // Láseres enemigos no dañan al Jefe, Esbirros, Raiders, o UFOs
            if (this instanceof Laser && !((Laser)this).isPlayerLaser() &&
                    (m instanceof FinalBoss || m instanceof Minion || m instanceof Raider || m instanceof Ufo)) {
                continue;
            }
            if (m instanceof Laser && !((Laser)m).isPlayerLaser() &&
                    (this instanceof FinalBoss || this instanceof Minion || this instanceof Raider || this instanceof Ufo)) {
                continue;
            }
            // --- FIN DE NUEVAS REGLAS ---


            // --- INICIO DE LÓGICA DE COLISIÓN REESTRUCTURADA ---

            // 1. LÓGICA DE ESCUDO (Prioridad alta, usa colisión circular)
            Player shieldedPlayer = null;
            MovingObject other = null;

            if (this instanceof Player && ((Player)this).isShielded()) {
                shieldedPlayer = (Player) this;
                other = m;
            } else if (m instanceof Player && ((Player)m).isShielded()) {
                shieldedPlayer = (Player) m;
                other = this;
            }

            if (shieldedPlayer != null) {
                double shieldRadius = 70.0;
                double otherRadius = other.width / 2.0;
                double distance = m.getCenter().subtract(getCenter()).getMagnitude();

                if (distance < shieldRadius + otherRadius) {
                    // Está colisionando CON EL ESCUDO
                    // (Lógica de escudo que ya tenías)
                    if (other instanceof Laser && !((Laser)other).isPlayerLaser()) {
                        gameState.playExplosion(other.getCenter());
                        other.Destroy();
                        return; // Sale de collidesWith() para este objeto 'm'
                    }
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
                    if (other instanceof Meteor || other instanceof Ufo) {
                        other.velocity = other.velocity.scale(-1);
                        Vector2D separation = other.getCenter().subtract(shieldedPlayer.getCenter()).normalize().scale(5);
                        other.position = other.position.add(separation);
                        return;
                    }
                }
            }

            // 2. LÓGICA DE COLISIÓN NORMAL (Sin escudo)
            boolean collided = false;

            // Usar Bounding Box (Rectangular) si el jugador choca con un enemigo peligroso
            if (this instanceof Player && (m instanceof FinalBoss || m instanceof MiniBoss || m instanceof Minion || m instanceof Raider)) {
                collided = this.getBounds().intersects(m.getBounds());
            } else if (m instanceof Player && (this instanceof FinalBoss || this instanceof MiniBoss || this instanceof Minion || this instanceof Raider)) {
                collided = m.getBounds().intersects(this.getBounds());
            } else {
                // Usar Colisión Circular para todo lo demás (Meteoro vs Meteoro, Player vs Meteor, etc.)
                double myRadius = this.width / 2.0;
                double theirRadius = m.width / 2.0;
                double distance = m.getCenter().subtract(getCenter()).getMagnitude();
                collided = (distance < theirRadius + myRadius);
            }

            if (collided) {
                objectCollision(this, m);
            }
            // --- FIN DE LÓGICA DE COLISIÓN REESTRUCTURADA ---
        }
    }

    private void objectCollision(MovingObject a, MovingObject b) {
        if ((a instanceof Player && ((Player) a).isSpawning()) || (b instanceof Player && ((Player) b).isSpawning())) return;

        if (a instanceof Laser && ((Laser) a).isPlayerLaser()) b.lastHitByPlayer = true;
        if (b instanceof Laser && ((Laser) b).isPlayerLaser()) a.lastHitByPlayer = true;

        // El láser enemigo (Raider/UFO) destruye meteoritos pero el láser sobrevive
        if (a instanceof Laser && !((Laser)a).isPlayerLaser() && b instanceof Meteor) {
            gameState.playExplosion(b.getCenter());
            b.Destroy();
            return;
        }
        if (b instanceof Laser && !((Laser)b).isPlayerLaser() && a instanceof Meteor) {
            gameState.playExplosion(a.getCenter());
            a.Destroy();
            return;
        }

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

        // --- INICIO DE LA CORRECCIÓN ---
        // Regla: Jugador vs Esbirro
        if (a instanceof Player && b instanceof Minion) {
            gameState.playExplosion(a.getCenter());
            a.Destroy(); // Destruye al jugador
            return;      // Esbirro sobrevive
        }
        if (a instanceof Minion && b instanceof Player) {
            gameState.playExplosion(b.getCenter());
            b.Destroy(); // Destruye al jugador
            return;      // Esbirro sobrevive
        }

        // Regla: Jugador vs Jefe Final
        if (a instanceof Player && b instanceof FinalBoss) {
            gameState.playExplosion(a.getCenter());
            a.Destroy(); // Destruye al jugador
            return;      // Jefe Final sobrevive
        }
        if (a instanceof FinalBoss && b instanceof Player) {
            gameState.playExplosion(b.getCenter());
            b.Destroy(); // Destruye al jugador
            return;      // Jefe Final sobrevive
        }

        // Regla: Láser enemigo vs Enemigos
        // (Añadido FinalBoss y Minion a la regla)
        if (a instanceof Laser && !((Laser)a).isPlayerLaser() &&
                (b instanceof MiniBoss || b instanceof Ufo || b instanceof FinalBoss || b instanceof Minion)) {
            return;
        }
        if (b instanceof Laser && !((Laser)b).isPlayerLaser() &&
                (a instanceof MiniBoss || a instanceof Ufo || a instanceof FinalBoss || a instanceof Minion)) {
            return;
        }
        // --- FIN DE LA CORRECCIÓN ---

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