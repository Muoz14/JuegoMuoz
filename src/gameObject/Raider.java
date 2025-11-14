package gameObject;

import graphics.Assets;
import math.Vector2D;
import states.GameState;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/**
 * Tarea que debe realizar un Raider.
 */
enum RaiderTask {
    FLY_RL, // Derecha a Izquierda
    FLY_LR, // Izquierda a Derecha
    FLY_TB, // Arriba a Abajo
    FLY_BT  // Abajo a Arriba
}

/**
 * Clase Raider "tonta". Solo sigue una tarea (Task) y se autodestruye.
 * Es controlado por el RaiderSquadManager.
 */
public class Raider extends MovingObject {

    private Chronometer fireRateTimer;
    private RaiderTask task;

    public Raider(Vector2D position, RaiderTask task, GameState gameState) {
        super(position, new Vector2D(), Constants.RAIDER_MAX_VEL, Assets.raider, gameState);
        this.task = task;

        // Configurar velocidad y ángulo basado en la tarea
        switch (task) {
            case FLY_RL:
                this.angle = -Math.PI / 2; // Mirando a la izquierda
                this.velocity = new Vector2D(-maxVel, 0);
                break;
            case FLY_LR:
                this.angle = Math.PI / 2; // Mirar a la derecha
                this.velocity = new Vector2D(maxVel, 0);
                break;
            case FLY_TB:
                this.angle = Math.PI; // Mirar hacia abajo
                this.velocity = new Vector2D(0, maxVel);
                break;
            case FLY_BT:
                this.angle = 0; // Mirar hacia arriba (por defecto)
                this.velocity = new Vector2D(0, -maxVel);
                break;
        }

        this.fireRateTimer = new Chronometer();
        this.immuneToMeteors = true;
    }

    @Override
    public void update() {
        // Actualizar temporizadores
        fireRateTimer.update();

        // Lógica de disparo
        shoot();

        // Aplicar movimiento
        position = position.add(velocity);

        // Comprobar si salió de la pantalla para autodestruirse
        checkOutOfBounds();
    }

    private void checkOutOfBounds() {
        // Comprueba si está completamente fuera de los límites
        if (position.getX() < -width || position.getX() > Constants.WIDTH ||
                position.getY() < -height || position.getY() > Constants.HEIGHT)
        {
            // Se destruye silenciosamente (sin puntos, sin explosión)
            gameState.removeObject(this);
        }
    }

    private void shoot() {
        if (!fireRateTimer.isRunning()) {

            // Vector de dirección (siempre hacia adelante)
            Vector2D heading = new Vector2D(1, 0).setDirection(angle - Math.PI / 2);

            // Origen del láser (punta de la nave)
            Vector2D laserOrigin = new Vector2D(width / 2.0, 0)
                    .subtract(new Vector2D(width / 2.0, height / 2.0))
                    .rotate(angle)
                    .add(getCenter());

            Laser laser = new Laser(
                    laserOrigin,
                    heading,
                    Constants.LASER_VEL,
                    angle,
                    Assets.laserPersonalizado1,
                    gameState,
                    false
            );

            gameState.addObject(laser);
            fireRateTimer.run(Constants.RAIDER_FIRE_RATE);
            Assets.ufoShoot.play();
        }
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform at = AffineTransform.getTranslateInstance(position.getX(), position.getY());
        at.rotate(angle, width / 2.0, height / 2.0);
        g2d.drawImage(texture, at, null);
    }

    @Override
    protected void Destroy() {
        // Este Destroy() SÓLO es llamado por colisión con láser del jugador
        if (lastHitByPlayer) {
            gameState.addScore(Constants.RAIDER_SCORE, getCenter());
        }

        // Llama al Destroy() de MovingObject para reproducir sonido y eliminarse
        super.Destroy();
    }
}