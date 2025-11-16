package gameObject;

import math.Vector2D;
import states.GameState;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Laser extends MovingObject {

    private boolean playerLaser;
    protected Shape hitbox;

    // --- INICIO DE LA MODIFICACION ---
    private BufferedImage[] frames; // Para animacion
    private int currentFrame;
    private Chronometer animTimer;
    private static final int ANIM_SPEED_MS = 80; // Milisegundos por frame (ajusta si es muy rapido/lento)
    // --- FIN DE LA MODIFICACION ---


    // Constructor ESTATICO (original, para enemigos y lasers simples)
    public Laser(Vector2D position, Vector2D velocity, double maxVel, double angle,
                 BufferedImage texture, GameState gameState, boolean playerLaser) {
        super(position, velocity.scale(maxVel), maxVel, texture, gameState);
        this.angle = angle;
        this.playerLaser = playerLaser;
        this.frames = null; // No es animado
        updateHitbox();
    }

    // Constructor ANIMADO (nuevo, para el jugador)
    public Laser(Vector2D position, Vector2D velocity, double maxVel, double angle,
                 BufferedImage[] frames, GameState gameState, boolean playerLaser) {
        super(position, velocity.scale(maxVel), maxVel, frames[0], gameState); // Usa el frame 0 como base
        this.angle = angle;
        this.playerLaser = playerLaser;
        this.frames = frames; // Guarda los frames
        this.currentFrame = 0;
        this.animTimer = new Chronometer();
        this.animTimer.run(ANIM_SPEED_MS);
        updateHitbox();
    }


    public boolean isPlayerLaser() {
        return playerLaser;
    }

    /**
     * Actualiza la posicion y rotacion de la hitbox rectangular.
     */
    private void updateHitbox() {
        AffineTransform at = AffineTransform.getTranslateInstance(position.getX() - width / 2.0, position.getY());
        at.rotate(angle, width / 2.0, 0);

        double hitboxWidth = width * 0.7;
        double xOffset = (width - hitboxWidth) / 2.0;
        Rectangle2D.Double baseRect = new Rectangle2D.Double(xOffset, 0, hitboxWidth, height);

        this.hitbox = at.createTransformedShape(baseRect);
    }

    @Override
    public void update() {
        position = position.add(velocity);

        if (position.getX() < 0 || position.getX() > Constants.WIDTH ||
                position.getY() < 0 || position.getY() > Constants.HEIGHT) {
            Destroy();
        }

        // --- INICIO DE LA MODIFICACION (Logica de Animacion) ---
        if (frames != null) {
            animTimer.update();
            if (animTimer.isFinished()) {
                currentFrame++;
                if (currentFrame >= frames.length) {
                    currentFrame = 0; // Bucle
                }
                this.texture = frames[currentFrame]; // Actualiza la textura actual

                // Actualizar ancho/alto por si los frames son distintos
                this.width = this.texture.getWidth();
                this.height = this.texture.getHeight();

                animTimer.run(ANIM_SPEED_MS); // Reinicia el timer
            }
        }
        // --- FIN DE LA MODIFICACION ---

        // Actualizar la hitbox en cada frame (importante para frames de distinto tamano)
        updateHitbox();

        collidesWith();
    }

    /**
     * Logica de colision sobrescrita.
     */
    @Override
    protected void collidesWith() {
        if (isSpawnImmune()) return;

        // Si NO es un laser del jugador, usa la logica de colision circular
        if (!playerLaser) {
            super.collidesWith();
            return;
        }

        // Si ES un laser del jugador, usa la logica rectangular (Shape.intersects)
        ArrayList<MovingObject> movingObjects = gameState.getMovingObjects();

        for (MovingObject m : movingObjects) {
            if (m.equals(this)) continue;
            if (m.isSpawnImmune()) continue;

            if (m instanceof Laser || m instanceof Player) {
                continue;
            }

            if (m instanceof MiniBoss || m instanceof FinalBoss) {
                continue;
            }

            if (m instanceof PowerUp) {
                continue;
            }

            if (this.hitbox.intersects(m.getBounds())) {

                gameState.playExplosion(m.getCenter());
                m.setLastHitByPlayer(true);
                m.Destroy();
                this.Destroy();
                break;
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform at = AffineTransform.getTranslateInstance(position.getX() - width / 2.0, position.getY());
        at.rotate(angle, width / 2.0, 0);
        g2d.drawImage(texture, at, null); // Dibuja la 'texture' actual (que es actualizada por el update)
    }

    @Override
    public Vector2D getCenter() {
        // Corregido: El centro debe basarse en la altura (height) y la rotacion.
        Point2D.Double center = new Point2D.Double(position.getX(), position.getY() + height / 2.0);
        AffineTransform rotation = AffineTransform.getRotateInstance(angle, position.getX(), position.getY());
        Point2D.Double rotatedCenter = new Point2D.Double();
        rotation.transform(center, rotatedCenter);
        return new Vector2D(rotatedCenter.getX(), rotatedCenter.getY());
    }
}