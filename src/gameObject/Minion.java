package gameObject;

import graphics.Assets;
import math.Vector2D;
import states.GameState;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class Minion extends MovingObject {

    private Chronometer fireRateTimer;
    private int hitsTaken;
    private final int MAX_HITS = 2; // Tiene 2 vidas

    public Minion(Vector2D position, GameState gameState) {
        super(position, new Vector2D(), Constants.MINION_SPEED, Assets.minion, gameState);
        this.fireRateTimer = new Chronometer();
        this.hitsTaken = 0;
        this.immuneToMeteors = true;
    }

    @Override
    public void update() {
        Player player = gameState.getPlayer();
        if (player == null || player.isDead()) {
            // Si el jugador no está, deambular
            velocity = velocity.add(new Vector2D(Math.random() * 2 - 1, Math.random() * 2 - 1).scale(0.1));
        } else {
            // Ir hacia el jugador (IA de seguimiento suave)
            Vector2D toPlayer = player.getCenter().subtract(getCenter()).normalize();

            // Se reduce de 0.05 a 0.02 para que sea menos preciso
            velocity = velocity.add(toPlayer.subtract(velocity).scale(0.02)).limit(maxVel);
        }

        position = position.add(velocity);

        // Disparar
        shoot(player);
        fireRateTimer.update();

        // Girar para mirar la dirección del movimiento
        if (velocity.getMagnitude() > 0.1) {
            angle = Math.atan2(velocity.getY(), velocity.getX()) + Math.PI / 2;
        }
    }

    private void shoot(Player player) {
        if (!fireRateTimer.isRunning() && player != null && !player.isDead()) {

            Vector2D toPlayer = player.getCenter().subtract(getCenter()).normalize();
            double fireAngle = Math.atan2(toPlayer.getY(), toPlayer.getX()) + Math.PI / 2;

            Laser laser = new Laser(
                    getCenter().add(toPlayer.scale(width / 2.0)), // Disparar desde el centro
                    toPlayer,
                    Constants.LASER_VEL,
                    fireAngle,
                    Assets.laserPersonalizado1, // Placeholder
                    gameState,
                    false
            );

            gameState.addObject(laser);
            fireRateTimer.run(Constants.MINION_FIRE_RATE);
            Assets.ufoShoot.play();
        }
    }

    public void pauseTimers() {
        fireRateTimer.pause();
    }

    public void resumeTimers() {
        fireRateTimer.resume();
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
        // Este Destroy() es llamado por un láser del jugador
        hitsTaken++;

        if (hitsTaken >= MAX_HITS) {
            if (lastHitByPlayer) {
                gameState.addScore(Constants.MINION_SCORE, getCenter());
            }
            // Llama al Destroy() de MovingObject para sonido y eliminación
            super.Destroy();
        } else {
            // Solo fue un golpe, no se destruye aún
            gameState.playExplosion(getCenter());
            lastHitByPlayer = false; // Resetear para que el siguiente golpe cuente
        }
    }
}