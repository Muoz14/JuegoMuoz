package gameObject;

import graphics.Assets;
import graphics.Sound;
import math.Vector2D;
import states.GameState;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Ufo extends MovingObject {

    private ArrayList<Vector2D> path;
    private Vector2D currentNode;
    private int index;
    private double swayTime;

    private Chronometer fireRate;
    private Sound shoot;

    private long spawnTime = 0;
    private final long LIFE_TIME = 10000;

    public Ufo(Vector2D position, Vector2D velocity, double maxVel, BufferedImage texture,
               ArrayList<Vector2D> path, GameState gameState) {
        super(position, velocity, maxVel, texture, gameState);

        this.path = path;
        this.index = 0;
        this.swayTime = 0;

        this.fireRate = new Chronometer();
        fireRate.run(Constants.UFO_FIRE_RATE);

        this.shoot = new Sound("/sounds/ufoShoot.wav");
        this.immuneToMeteors = true;
    }

    private Vector2D pathFollowing() {
        currentNode = path.get(index);
        double distanceToNode = currentNode.subtract(getCenter()).getMagnitude();
        if (distanceToNode < Constants.NODE_RADIUS) {
            index++;
            if (index >= path.size()) index = 0;
        }
        return seekForce(currentNode);
    }

    private Vector2D seekForce(Vector2D target) {
        Vector2D desiredVelocity = target.subtract(getCenter()).normalize().scale(maxVel);
        return desiredVelocity.subtract(velocity);
    }

    @Override
    public void update() {
        if (spawnTime == 0) spawnTime = System.currentTimeMillis();

        long elapsed = System.currentTimeMillis() - spawnTime;
        if (elapsed >= LIFE_TIME) {
            Destroy(); // No sumar puntos al destruirse por tiempo
            gameState.playExplosion(getCenter());
            return;
        }

        Vector2D pathForce = pathFollowing().scale(1 / Constants.UFO_MASS);
        velocity = velocity.add(pathForce).limit(maxVel);
        position = position.add(velocity);

        // Teletransportación
        boolean teleported = false;
        if (position.getX() > Constants.WIDTH) { position.setX(0); teleported = true; }
        if (position.getX() < 0) { position.setX(Constants.WIDTH); teleported = true; }
        if (position.getY() > Constants.HEIGHT) { position.setY(0); teleported = true; }
        if (position.getY() < 0) { position.setY(Constants.HEIGHT); teleported = true; }
        if (teleported) spawnTime = System.currentTimeMillis();

        // Disparo
        if (!fireRate.isRunning()) {
            Vector2D toPlayer = gameState.getPlayer().getCenter().subtract(getCenter()).normalize();
            double currentAngle = toPlayer.getAngle();
            final double DISPERSION_RANGE = Math.PI / 8;
            double newAngle = Math.random() * (2 * DISPERSION_RANGE) - DISPERSION_RANGE + currentAngle;
            toPlayer = toPlayer.setDirection(newAngle);

            Laser laser = new Laser(
                    getCenter().add(toPlayer.scale(width)),
                    toPlayer,
                    Constants.LASER_VEL,
                    newAngle + Math.PI / 2,
                    Assets.ufoLaser,
                    gameState,
                    false
            );

            gameState.getMovingObjects().add(0, laser);
            fireRate.run(Constants.UFO_FIRE_RATE);
            shoot.play();
            shoot.changeVolume(-15.0f);
        }

        swayTime += 0.05;
        collidesWith();
        fireRate.update();
    }

    @Override
    protected void Destroy() {

        if (lastHitByPlayer) {
            gameState.addScore(Constants.UFO_SCORE, position);
        }

        super.Destroy();

    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        double swayOffset = Math.sin(swayTime * Constants.SWAY_SPEED_VISUAL) * Constants.SWAY_AMPLITUDE_VISUAL;
        AffineTransform at = AffineTransform.getTranslateInstance(position.getX() + swayOffset, position.getY());
        g2d.drawImage(texture, at, null);
    }
}
