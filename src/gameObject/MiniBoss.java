package gameObject;

import graphics.Assets;
import graphics.Sound;
import math.Vector2D;
import states.GameState;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class MiniBoss extends MovingObject {

    private int hitsTaken;               // Cantidad de impactos recibidos del jugador
    private int maxHits;
    private long lastShotTime;           // Control de disparo
    private static final long FIRE_RATE = 1200; // milisegundos entre disparos
    private Sound shootSound;            // Sonido de disparo

    // Variables para el nuevo movimiento
    private Vector2D targetVelocity;     // Velocidad deseada
    private long lastMoveChangeTime;     // Cuando cambio de direccion por ultima vez
    private long moveChangeInterval = 2000; // Cambia de objetivo cada 2 segundos (2000 ms)

    // --- SOLUCION: El constructor ahora recibe la vida maxima ---
    public MiniBoss(Vector2D position, GameState gameState, int maxHits) {
        super(position, new Vector2D(0, 0), 2.5, Assets.miniBoss, gameState);
        this.hitsTaken = 0;
        this.shootSound = new Sound("/sounds/ufoShoot.wav");
        this.immuneToMeteors = true;
        this.maxHits = maxHits; // Se asigna la vida (12 o 30)
        this.lastMoveChangeTime = System.currentTimeMillis();
        this.targetVelocity = new Vector2D();
    }

    @Override
    public void update() {
        // Actualizar movimiento
        updateMovement();

        // Aplicar movimiento y limites de pantalla
        velocity = velocity.add(targetVelocity.subtract(velocity).scale(0.1)); // Suavizado
        velocity = velocity.limit(maxVel);
        position = position.add(velocity);

        // Limites de pantalla (rebote)
        handleScreenLimits();

        // Disparo y colisiones
        shootAtPlayer();     // Disparo hacia jugador
        collidesWithLasers(); // Detecta solo colisiones con laseres del jugador
    }

    // Nuevo metodo de movimiento
    private void updateMovement() {
        long now = System.currentTimeMillis();
        Player player = gameState.getPlayer();

        // Si el jugador no existe, se mueve erraticamente
        if (player == null || player.isDead()) {
            if (now - lastMoveChangeTime > moveChangeInterval) {
                // Direccion aleatoria
                double randomAngle = Math.random() * 2 * Math.PI;
                targetVelocity = new Vector2D(Math.cos(randomAngle), Math.sin(randomAngle)).scale(maxVel);
                lastMoveChangeTime = now;
            }
            return;
        }

        // Si el jugador existe, decide si perseguirlo o moverse aleatoriamente
        if (now - lastMoveChangeTime > moveChangeInterval) {
            lastMoveChangeTime = now;

            // 50% de probabilidad de ir hacia el jugador, 50% de moverse aleatorio
            if (Math.random() < 0.5) {
                // Perseguir al jugador
                Vector2D toPlayer = player.getCenter().subtract(getCenter()).normalize();
                targetVelocity = toPlayer.scale(maxVel);
            } else {
                // Direccion aleatoria
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

        // Si rebota, forzamos un cambio de direccion
        if (bounced) {
            lastMoveChangeTime = System.currentTimeMillis();
            // Direccion aleatoria al rebotar
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
                false // laser enemigo
        );

        gameState.addObject(laser);
        shootSound.play();
        lastShotTime = now;
    }

    // Colisiones unicamente con laseres del jugador
    private void collidesWithLasers() {
        for (MovingObject obj : gameState.getMovingObjects()) {
            if (obj instanceof Laser) {
                Laser laser = (Laser) obj;

                // Solo afecta si el laser es del jugador
                if (laser.isPlayerLaser() && this.collides(laser)) {
                    laser.Destroy();   // Destruye el laser
                    hitsTaken++;

                    // Explosion pequena en cada impacto
                    gameState.playExplosion(getCenter());

                    // --- SOLUCION: Usar la variable 'maxHits' ---
                    if (hitsTaken >= this.maxHits) {
                        lastHitByPlayer = true;
                        Destroy(); // <- AQUI SE LLAMAN LOS MENSAJES
                        gameState.playExplosion(getCenter()); // Explosion final
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
            Sound explosionSound = new Sound("/sounds/explosion.wav");
            explosionSound.play();

            // 1. Mensaje principal
            Message bossDeadMsg = new Message(
                    new Vector2D(Constants.WIDTH / 2, Constants.HEIGHT / 2),
                    false, // fade
                    "MINI JEFE ANIQUILADO",
                    Color.CYAN, // Un color victorioso
                    true, // centered
                    Assets.fontBig,
                    gameState
            );
            bossDeadMsg.setLifespan(3000); // 3 segundos
            gameState.addMessage(bossDeadMsg);

            // 2. Mensaje secundario (usando un Thread para que aparezca despues)
            new Thread(() -> {
                try {
                    // Esperar a que el primer mensaje casi desaparezca
                    Thread.sleep(2800);
                } catch (InterruptedException ignored) {}

                Message cleanupMsg = new Message(
                        new Vector2D(Constants.WIDTH / 2, Constants.HEIGHT / 2 + 60), // Un poco mas abajo
                        false,
                        "ACABA CON LOS METEORITOS RESTANTES",
                        Color.WHITE,
                        true,
                        Assets.fontMed, // Fuente mediana
                        gameState
                );
                cleanupMsg.setLifespan(3500); // 3.5 segundos
                gameState.addMessage(cleanupMsg);
            }).start();
        }
        gameState.removeObject(this); // Quitar al jefe
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Dibujar la nave sin rotacion
        AffineTransform at = AffineTransform.getTranslateInstance(position.getX(), position.getY());
        g2d.drawImage(texture, at, null);

        // Barra de vida
        // --- SOLUCION: Usar la variable 'maxHits' ---
        double lifePercent = (double) (this.maxHits - hitsTaken) / this.maxHits;
        int barWidth = width;
        int barHeight = 6;
        int barYOffset = 10; // Distancia de la barra sobre el boss

        g.setColor(Color.RED);
        g.fillRect((int) position.getX(), (int) position.getY() - barYOffset, barWidth, barHeight);
        g.setColor(Color.GREEN);
        g.fillRect((int) position.getX(), (int) position.getY() - barYOffset, (int) (barWidth * lifePercent), barHeight);
        g.setColor(Color.WHITE);
        g.drawRect((int) position.getX(), (int) position.getY() - barYOffset, barWidth, barHeight);
    }
}