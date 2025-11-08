package gameObject;

import graphics.Assets;
import input.KeyBoard;
import math.Vector2D;
import states.GameState;
import states.SettingsData;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Player extends MovingObject {

    private Vector2D heading;
    private Vector2D acceleration;

    public final int CORRECCION_X = 6;
    public final int CORRECCION_Y = 10;

    private boolean accelerating = false;
    private Chronometer fireRate;
    private ShipData data;
    private BufferedImage laserTexture;

    private AffineTransform at;

    // Reaparicion
    private boolean spawning = false;
    private boolean dead = false;
    private boolean visible = true;

    private Chronometer spawnTime, flickerTime;

    public Player(Vector2D position, Vector2D velocity, GameState gameState, ShipData data, BufferedImage laserTexture) {
        super(position, velocity, data.getMaxVelocity(), data.getTexture(), gameState);
        this.data = data;
        this.laserTexture = laserTexture;

        heading = new Vector2D(0, 1);
        acceleration = new Vector2D();
        fireRate = new Chronometer();
        spawnTime = new Chronometer();
        flickerTime = new Chronometer();

        // APLICAMOS LA LOGICA DE VOLUMEN A LOS ASSETS ESTATICOS
        float initialSFXVolume = SettingsData.getVolume() * 1.2f;
        if (initialSFXVolume > 1f) initialSFXVolume = 1f;
        Assets.playerShoot.setVolume(initialSFXVolume);
        Assets.playerLoose.setVolume(SettingsData.getVolume());
    }

    @Override
    public void update() {
        // Actualizar spawn
        if (spawning) updateSpawnTimer();

        // Disparo
        if (KeyBoard.SHOOT() && !fireRate.isRunning() && !spawning) {
            Vector2D basePosition = getCenter().add(heading.scale(width));
            for (Vector2D offset : data.gunOffsets) {
                Vector2D rotatedOffset = offset.rotate(angle);
                gameState.addObject(new Laser(
                        basePosition.add(rotatedOffset),
                        heading,
                        Constants.LASER_VEL,
                        angle,
                        laserTexture,
                        gameState,
                        true
                ));
            }

            fireRate.run(Constants.FIRERATE);

            float sfxVolume = SettingsData.getVolume() * 1.2f;
            if (sfxVolume > 1f) sfxVolume = 1f;
            Assets.playerShoot.setVolume(sfxVolume);
            Assets.playerShoot.play();
        }

        if (Assets.playerShoot.getFramePosition() > 15500) Assets.playerShoot.stop();

        // Rotacion
        if (KeyBoard.RIGTH()) angle += Constants.DELTAANGLE;
        if (KeyBoard.LEFT()) angle -= Constants.DELTAANGLE;

        // Movimiento
        if (KeyBoard.UP()) {
            acceleration = heading.scale(Constants.ACC);
            accelerating = true;
        } else if (KeyBoard.DOWN()) {
            acceleration = heading.scale(-Constants.ACC / 2);
        } else {
            if (velocity.getMagnitude() != 0)
                acceleration = velocity.scale(-1).normalize().scale(Constants.ACC / 2);
            accelerating = false;
        }

        velocity = velocity.add(acceleration).limit(maxVel);
        heading = heading.setDirection(angle - Math.PI / 2);
        position = position.add(velocity);

        // Limites de pantalla
        if (position.getX() > Constants.WIDTH) position.setX(0);
        if (position.getY() > Constants.HEIGHT) position.setY(0);
        if (position.getX() < 0) position.setX(Constants.WIDTH);
        if (position.getY() < 0) position.setY(Constants.HEIGHT);

        fireRate.update();
        collidesWith();
    }

    @Override
    public void Destroy() {
        if (!dead) {
            dead = true;
            visible = false;
            Assets.playerLoose.play();
            Assets.playerLoose.changeVolume(-2.0f);

            gameState.subtractScore(80, getCenter()); // Penalizacion
            resetValues();
        }
    }

    private void resetValues() {
        angle = 0;
        velocity = new Vector2D();
        position = new Vector2D(560, 320);
        gameState.subtractLife();
    }

    @Override
    public void draw(Graphics g) {
        if (!visible) return;
        Graphics2D g2d = (Graphics2D) g;

        // Propulsores
        if (accelerating) {
            for (Vector2D offset : data.thrusterOffsets) {
                Vector2D rotatedOffset = offset.rotate(angle);
                AffineTransform atPropulsor = AffineTransform.getTranslateInstance(
                        position.getX() + rotatedOffset.getX(),
                        position.getY() + rotatedOffset.getY()
                );
                atPropulsor.rotate(angle, width / 2, height / 2);
                g2d.drawImage(data.thrusterTexture, atPropulsor, null);
            }
        }

        // Nave
        at = AffineTransform.getTranslateInstance(position.getX(), position.getY());
        at.rotate(angle, width / 2, height / 2);
        g2d.drawImage(texture, at, null);
    }

    // ------------------ Metodos auxiliares ------------------
    public boolean isSpawning() { return spawning; }
    public boolean isDead() { return dead; }

    public void startRespawn() {
        if (dead && !spawning) {
            spawning = true;
            dead = false;
            visible = true;
            spawnTime.run(3000); // 3s de inmunidad
            flickerTime.run(200); // parpadeo
            velocity = new Vector2D();
        }
    }

    public void updateSpawnTimer() {
        spawnTime.update();
        flickerTime.update();

        if (flickerTime.isFinished()) {
            visible = !visible;
            flickerTime.run(200);
        }

        if (spawnTime.isFinished()) {
            spawning = false;
            visible = true;
        }
    }

    @Override
    protected boolean isSpawnImmune() { return spawning; }

    public Vector2D getCenter() {
        return new Vector2D(position.getX() + width / 2, position.getY() + height / 2);
    }
}