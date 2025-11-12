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
import java.util.List;

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

    // --- Timers de Power-Ups ---
    private boolean isShielded = false;
    private Chronometer shieldTimer;
    private Animation shieldAnimation;

    private boolean isRapidFire = false;
    private Chronometer rapidFireTimer;

    private boolean isMultiShot = false;
    private Chronometer multiShotTimer;

    private boolean isScoreMultiplier = false;
    private Chronometer scoreMultiplierTimer;


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
        rapidFireTimer = new Chronometer();
        multiShotTimer = new Chronometer();
        scoreMultiplierTimer = new Chronometer();

        float initialSFXVolume = SettingsData.getVolume() * 1.2f;
        if (initialSFXVolume > 1f) initialSFXVolume = 1f;
        Assets.playerShoot.setVolume(initialSFXVolume);
        Assets.playerLoose.setVolume(SettingsData.getVolume());
    }

    @Override
    public void update() {
        if (spawning) updateSpawnTimer();

        // --- Actualizacion de Power-ups ---
        if (isShielded) {
            shieldTimer.update();
            shieldAnimation.update();
            if (shieldTimer.isFinished()) {
                deactivateShield();
            }
        }
        if (isRapidFire) {
            rapidFireTimer.update();
            if (rapidFireTimer.isFinished()) {
                deactivateRapidFire();
            }
        }
        if (isMultiShot) {
            multiShotTimer.update();
            if (multiShotTimer.isFinished()) {
                deactivateMultiShot();
            }
        }
        if (isScoreMultiplier) {
            scoreMultiplierTimer.update();
            if (scoreMultiplierTimer.isFinished()) {
                deactivateScoreMultiplier();
            }
        }

        // --- Logica de Disparo ---
        if (KeyBoard.SHOOT() && !fireRate.isRunning() && !spawning) {
            Vector2D basePosition = getCenter().add(heading.scale(width));
            List<Vector2D> currentGunOffsets = isMultiShot ? data.upgradedGunOffsets : data.gunOffsets;

            for (Vector2D offset : currentGunOffsets) {
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

            long currentFireRate = isRapidFire ? (long)(Constants.FIRERATE * 0.5) : Constants.FIRERATE;
            fireRate.run(currentFireRate);

            float sfxVolume = SettingsData.getVolume() * 1.2f;
            if (sfxVolume > 1f) sfxVolume = 1f;
            Assets.playerShoot.setVolume(sfxVolume);
            Assets.playerShoot.play();
        }

        if (Assets.playerShoot.getFramePosition() > 15500) Assets.playerShoot.stop();

        // --- Logica de Movimiento ---
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

        if (isShielded && shieldAnimation != null) {
            BufferedImage shieldFrame = shieldAnimation.getCurrentFrame();
            int frameWidth = shieldFrame.getWidth();
            int frameHeight = shieldFrame.getHeight();
            double x = getCenter().getX() - (frameWidth / 2.0);
            double y = getCenter().getY() - (frameHeight / 2.0);
            AffineTransform shieldAt = AffineTransform.getTranslateInstance(x, y);
            shieldAt.rotate(angle, getCenter().getX() - x, getCenter().getY() - y);
            g2d.drawImage(shieldFrame, shieldAt, null);
        }
    }

    // --- Metodos de Respawn ---
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

    // --- Metodos de Activacion de Power-ups ---

    // --- ESCUDO ---
    public void activateShield(PowerUpType type) {
        activateShield(type, type.duration);
    }
    public void activateShield(PowerUpType type, long duration) {
        // --- INICIO DE LA MODIFICACION (Stacking) ---
        long newTotalDuration = duration;
        if (isShielded && shieldTimer.isRunning()) {
            newTotalDuration += shieldTimer.getTimeRemaining();
        }
        // --- FIN DE LA MODIFICACION ---

        isShielded = true;
        shieldTimer.run(newTotalDuration); // Usa la duracion (posiblemente sumada)

        if (shieldAnimation == null) { // Solo crear animacion si no existe
            shieldAnimation = new Animation(
                    Assets.shield_effect, 150, new Vector2D(), true
            );
        }
    }
    public void deactivateShield() {
        isShielded = false;
        shieldTimer.reset();
        shieldAnimation = null; // Destruir animacion al desactivar
    }

    // --- DISPARO RAPIDO ---
    public void activateRapidFire(PowerUpType type) {
        activateRapidFire(type, type.duration);
    }
    public void activateRapidFire(PowerUpType type, long duration) {
        // --- INICIO DE LA MODIFICACION (Stacking) ---
        long newTotalDuration = duration;
        if (isRapidFire && rapidFireTimer.isRunning()) {
            newTotalDuration += rapidFireTimer.getTimeRemaining();
        }
        // --- FIN DE LA MODIFICACION ---

        isRapidFire = true;
        rapidFireTimer.run(newTotalDuration); // Usa la duracion sumada
    }
    public void deactivateRapidFire() {
        isRapidFire = false;
        rapidFireTimer.reset();
    }

    // --- MULTI-DISPARO ---
    public void activateMultiShot(PowerUpType type) {
        activateMultiShot(type, type.duration);
    }
    public void activateMultiShot(PowerUpType type, long duration) {
        // --- INICIO DE LA MODIFICACION (Stacking) ---
        long newTotalDuration = duration;
        if (isMultiShot && multiShotTimer.isRunning()) {
            newTotalDuration += multiShotTimer.getTimeRemaining();
        }
        // --- FIN DE LA MODIFICACION ---

        isMultiShot = true;
        multiShotTimer.run(newTotalDuration); // Usa la duracion sumada
    }
    public void deactivateMultiShot() {
        isMultiShot = false;
        multiShotTimer.reset();
    }

    // --- PUNTOS DOBLES ---
    public void activateScoreMultiplier(PowerUpType type) {
        activateScoreMultiplier(type, type.duration);
    }
    public void activateScoreMultiplier(PowerUpType type, long duration) {
        // --- INICIO DE LA MODIFICACION (Stacking) ---
        long newTotalDuration = duration;
        if (isScoreMultiplier && scoreMultiplierTimer.isRunning()) {
            newTotalDuration += scoreMultiplierTimer.getTimeRemaining();
        }
        // --- FIN DE LA MODIFICACION ---

        isScoreMultiplier = true;
        scoreMultiplierTimer.run(newTotalDuration); // Usa la duracion sumada
    }
    public void deactivateScoreMultiplier() {
        isScoreMultiplier = false;
        scoreMultiplierTimer.reset();
    }

    // --- Getters para el HUD ---
    public boolean isShielded() {
        return isShielded;
    }
    public double getShieldTimeRemaining() {
        if (!isShielded || !shieldTimer.isRunning() || shieldTimer.getDuration() == 0) return 0;
        return (double)shieldTimer.getTimeRemaining() / shieldTimer.getDuration();
    }

    public boolean isRapidFire() {
        return isRapidFire;
    }
    public double getRapidFireTimeRemaining() {
        if (!isRapidFire || !rapidFireTimer.isRunning() || rapidFireTimer.getDuration() == 0) return 0;
        return (double)rapidFireTimer.getTimeRemaining() / rapidFireTimer.getDuration();
    }

    public boolean isMultiShot() {
        return isMultiShot;
    }
    public double getMultiShotTimeRemaining() {
        if (!isMultiShot || !multiShotTimer.isRunning() || multiShotTimer.getDuration() == 0) return 0;
        return (double)multiShotTimer.getTimeRemaining() / multiShotTimer.getDuration();
    }

    public boolean isScoreMultiplier() {
        return isScoreMultiplier;
    }
    public double getScoreMultiplierTimeRemaining() {
        if (!isScoreMultiplier || !scoreMultiplierTimer.isRunning() || scoreMultiplierTimer.getDuration() == 0) return 0;
        return (double)scoreMultiplierTimer.getTimeRemaining() / scoreMultiplierTimer.getDuration();
    }


    @Override
    public Vector2D getCenter() {
        return new Vector2D(position.getX() + width / 2.0, position.getY() + height / 2.0);
    }
}