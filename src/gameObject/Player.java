package gameObject;

import graphics.Animation;
import graphics.Assets;
import graphics.Sound;
import graphics.SoundManager;
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

    private boolean isShielded = false;
    private Chronometer shieldTimer;
    private Animation shieldAnimation;

    public Player(Vector2D position, Vector2D velocity, GameState gameState, ShipData data, BufferedImage laserTexture) {
        super(position, velocity, data.getMaxVelocity(), data.getTexture(), gameState);
        this.data = data;
        this.laserTexture = laserTexture;

        heading = new Vector2D(0, 1);
        acceleration = new Vector2D();
        fireRate = new Chronometer();
        spawnTime = new Chronometer();
        flickerTime = new Chronometer();

        shieldTimer = new Chronometer();

        float initialSFXVolume = SettingsData.getVolume() * 1.2f;
        if (initialSFXVolume > 1f) initialSFXVolume = 1f;
        Assets.playerShoot.setVolume(initialSFXVolume);
        Assets.playerLoose.setVolume(SettingsData.getVolume());
    }

    @Override
    public void update() {
        if (spawning) updateSpawnTimer();

        if (isShielded) {
            shieldTimer.update();
            shieldAnimation.update();

            if (shieldTimer.isFinished()) {
                deactivateShield();
            }
        }

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

        if (KeyBoard.RIGTH()) angle += Constants.DELTAANGLE;
        if (KeyBoard.LEFT()) angle -= Constants.DELTAANGLE;

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

        if (position.getX() > Constants.WIDTH) position.setX(0);
        if (position.getY() > Constants.HEIGHT) position.setY(0);
        if (position.getX() < 0) position.setX(Constants.WIDTH);
        if (position.getY() < 0) position.setY(Constants.HEIGHT);

        fireRate.update();
        collidesWith();
    }

    @Override
    public void Destroy() {
        if (isSpawning() || isShielded()) {
            return;
        }

        if (!dead) {
            dead = true;
            visible = false;
            Assets.playerLoose.play();
            Assets.playerLoose.changeVolume(-2.0f);

            gameState.subtractScore(80, getCenter());
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

        at = AffineTransform.getTranslateInstance(position.getX(), position.getY());
        at.rotate(angle, width / 2, height / 2);
        g2d.drawImage(texture, at, null);

        // --- INICIO DE LA SOLUCION: DIBUJAR ESCUDO CON ROTACION ---
        if (isShielded && shieldAnimation != null) {
            BufferedImage shieldFrame = shieldAnimation.getCurrentFrame();

            int frameWidth = shieldFrame.getWidth();
            int frameHeight = shieldFrame.getHeight();

            // Centrar el escudo en la nave
            double x = getCenter().getX() - (frameWidth / 2.0);
            double y = getCenter().getY() - (frameHeight / 2.0);

            // Crear una nueva transformacion para el escudo
            AffineTransform shieldAt = AffineTransform.getTranslateInstance(x, y);

            // Rotar el escudo alrededor del centro del jugador
            // (centroDelJugadorX - posicionDelEscudoX, centroDelJugadorY - posicionDelEscudoY)
            shieldAt.rotate(angle, getCenter().getX() - x, getCenter().getY() - y);

            g2d.drawImage(shieldFrame, shieldAt, null);
        }
        // --- FIN DE LA SOLUCION ---
    }

    public boolean isSpawning() { return spawning; }
    public boolean isDead() { return dead; }

    public void startRespawn() {
        if (dead && !spawning) {
            dead = false;
            triggerPostHitImmunity(false);
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
    protected boolean isSpawnImmune() {
        return spawning;
    }

    public void activateShield(PowerUpType type) {
        isShielded = true;
        shieldTimer.run(type.duration);

        shieldAnimation = new Animation(
                Assets.shield_effect,
                150,
                new Vector2D(),
                true
        );
    }

    public void deactivateShield() {
        isShielded = false;
        shieldTimer.reset();
        shieldAnimation = null;
    }

    public void triggerPostHitImmunity(boolean applyKnockback) {
        if (spawning) return;

        spawning = true;
        visible = true;
        spawnTime.run(1500);
        flickerTime.run(200);

        if (applyKnockback) {
            velocity = heading.scale(-8.0);
        }
    }

    public boolean isShielded() {
        return isShielded;
    }

    public double getShieldTimeRemaining() {
        if (!isShielded || !shieldTimer.isRunning() || shieldTimer.getDuration() == 0) {
            return 0;
        }
        return (double)shieldTimer.getTimeRemaining() / shieldTimer.getDuration();
    }

    @Override
    public Vector2D getCenter() {
        return new Vector2D(position.getX() + width / 2.0, position.getY() + height / 2.0);
    }
}