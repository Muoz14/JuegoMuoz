package gameObject;

import graphics.Assets;
import math.Vector2D;
import states.GameState;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class MiniBoss extends MovingObject {

    private int hitsTaken;
    private int maxHits;
    private long lastShotTime;
    private static final long FIRE_RATE = 1200;

    // Variables para el nuevo movimiento
    private Vector2D targetVelocity;
    private long lastMoveChangeTime;
    private long moveChangeInterval = 2000;

    public MiniBoss(Vector2D position, GameState gameState, int maxHits) {
        super(position, new Vector2D(0, 0), 2.5, Assets.miniBoss, gameState);
        this.hitsTaken = 0;
        this.immuneToMeteors = true;
        this.maxHits = maxHits;
        this.lastMoveChangeTime = System.currentTimeMillis();
        this.targetVelocity = new Vector2D();
    }

    @Override
    public void update() {
        // Actualizar movimiento
        updateMovement();

        // Aplicar movimiento y limites de pantalla
        velocity = velocity.add(targetVelocity.subtract(velocity).scale(0.1));
        velocity = velocity.limit(maxVel);
        position = position.add(velocity);

        // Limites de pantalla (rebote)
        handleScreenLimits();

        // Disparo y colisiones
        shootAtPlayer();
        collidesWithLasers();
    }

    // Nuevo metodo de movimiento
    private void updateMovement() {
        long now = System.currentTimeMillis();
        Player player = gameState.getPlayer();

        if (player == null || player.isDead()) {
            if (now - lastMoveChangeTime > moveChangeInterval) {
                double randomAngle = Math.random() * 2 * Math.PI;
                targetVelocity = new Vector2D(Math.cos(randomAngle), Math.sin(randomAngle)).scale(maxVel);
                lastMoveChangeTime = now;
            }
            return;
        }

        if (now - lastMoveChangeTime > moveChangeInterval) {
            lastMoveChangeTime = now;

            if (Math.random() < 0.5) {
                Vector2D toPlayer = player.getCenter().subtract(getCenter()).normalize();
                targetVelocity = toPlayer.scale(maxVel);
            } else {
                double randomAngle = Math.random() * 2 * Math.PI;
                targetVelocity = new Vector2D(Math.cos(randomAngle), Math.sin(randomAngle)).scale(maxVel);
            }
        }
    }

    // Metodo para que rebote en los bordes en lugar de desaparecer
    private void handleScreenLimits() {
        boolean bounced = false;
        if (position.getX() < 0) {
            position.setX(0);
            velocity.setX(-velocity.getX());
            bounced = true;
        }
        if (position.getX() > Constants.WIDTH - width) {
            position.setX(Constants.WIDTH - width);
            velocity.setX(-velocity.getX());
            bounced = true;
        }
        if (position.getY() < 0) {
            position.setY(0);
            velocity.setY(-velocity.getY());
            bounced = true;
        }
        if (position.getY() > Constants.HEIGHT - height) {
            position.setY(Constants.HEIGHT - height);
            velocity.setY(-velocity.getY());
            bounced = true;
        }

        if (bounced) {
            lastMoveChangeTime = System.currentTimeMillis();
            double randomAngle = Math.random() * 2 * Math.PI;
            targetVelocity = new Vector2D(Math.cos(randomAngle), Math.sin(randomAngle)).scale(maxVel);
        }
    }


    // Dispara hacia el jugador
    private void shootAtPlayer() {
        long now = System.currentTimeMillis();
        if (now - lastShotTime < FIRE_RATE) return;

        Player player = gameState.getPlayer();
        if (player == null || player.isDead()) return;

        Vector2D toPlayer = player.getCenter().subtract(getCenter()).normalize();

        Laser laser = new Laser(
                getCenter(),
                toPlayer,
                5.5,
                Math.atan2(toPlayer.getY(), toPlayer.getX()) + Math.PI / 2,
                Assets.laserPersonalizado1,
                gameState,
                false
        );

        gameState.addObject(laser);
        Assets.ufoShoot.play(); // <- AHORA USA EL ASSET PRECARGADO
        lastShotTime = now;
    }

    // Colisiones unicamente con laseres del jugador
    private void collidesWithLasers() {
        for (MovingObject obj : gameState.getMovingObjects()) {
            if (obj instanceof Laser) {
                Laser laser = (Laser) obj;

                if (laser.isPlayerLaser() && this.collides(laser)) {
                    laser.Destroy();
                    hitsTaken++;

                    gameState.playExplosion(getCenter());

                    if (hitsTaken >= this.maxHits) {
                        lastHitByPlayer = true;
                        Destroy();
                        gameState.playExplosion(getCenter());
                        gameState.addScore(500, getCenter());
                        return;
                    }
                }
            }
        }
    }

    @Override
    protected void Destroy() {
        // Solo se destruye si fue eliminado por el jugador
        if (lastHitByPlayer) {
            Assets.explosion.play(); // <- AHORA USA EL ASSET PRECARGADO

            // 1. Mensaje principal
            Message bossDeadMsg = new Message(
                    new Vector2D(Constants.WIDTH / 2, Constants.HEIGHT / 2),
                    false,
                    "MINI JEFE ANIQUILADO",
                    Color.CYAN,
                    true,
                    Assets.fontBig,
                    gameState
            );
            bossDeadMsg.setLifespan(3000);
            gameState.addMessage(bossDeadMsg);

            // 2. Mensaje secundario (usando un Thread para que aparezca despues)
            new Thread(() -> {
                try {
                    Thread.sleep(2800);
                } catch (InterruptedException ignored) {}

                Message cleanupMsg = new Message(
                        new Vector2D(Constants.WIDTH / 2, Constants.HEIGHT / 2 + 60),
                        false,
                        "ACABA CON LOS METEORITOS RESTANTES",
                        Color.WHITE,
                        true,
                        Assets.fontMed,
                        gameState
                );
                cleanupMsg.setLifespan(3500);
                gameState.addMessage(cleanupMsg);
            }).start();
        }
        gameState.removeObject(this); // Quitar al jefe
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        AffineTransform at = AffineTransform.getTranslateInstance(position.getX(), position.getY());
        g2d.drawImage(texture, at, null);

        double lifePercent = (double) (this.maxHits - hitsTaken) / this.maxHits;
        int barWidth = width;
        int barHeight = 6;
        int barYOffset = 10;

        g.setColor(Color.RED);
        g.fillRect((int) position.getX(), (int) position.getY() - barYOffset, barWidth, barHeight);
        g.setColor(Color.GREEN);
        g.fillRect((int) position.getX(), (int) position.getY() - barYOffset, (int) (barWidth * lifePercent), barHeight);
        g.setColor(Color.WHITE);
        g.drawRect((int) position.getX(), (int) position.getY() - barYOffset, barWidth, barHeight);
    }
}