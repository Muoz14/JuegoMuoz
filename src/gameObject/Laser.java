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

    // Un 'Shape' para la hitbox rectangular que puede ser rotada
    private Shape hitbox;

    public Laser(Vector2D position, Vector2D velocity, double maxVel, double angle,
                 BufferedImage texture, GameState gameState, boolean playerLaser) {
        super(position, velocity.scale(maxVel), maxVel, texture, gameState);
        this.angle = angle;
        this.playerLaser = playerLaser;

        // Inicializar la hitbox (se actualizara en update)
        updateHitbox();
    }

    public boolean isPlayerLaser() {
        return playerLaser;
    }

    /**
     * Actualiza la posicion y rotacion de la hitbox rectangular.
     */
    private void updateHitbox() {
        // 1. Crear la transformacion igual que en el metodo draw()
        AffineTransform at = AffineTransform.getTranslateInstance(position.getX() - width / 2.0, position.getY());
        at.rotate(angle, width / 2.0, 0);

        // 2. Crear un rectangulo base (sin rotar) que sea mas delgado
        double hitboxWidth = width * 0.7;
        double xOffset = (width - hitboxWidth) / 2.0;
        Rectangle2D.Double baseRect = new Rectangle2D.Double(xOffset, 0, hitboxWidth, height);

        // 3. Aplicar la transformacion al rectangulo para crear el 'Shape' final
        this.hitbox = at.createTransformedShape(baseRect);
    }

    @Override
    public void update() {
        position = position.add(velocity);

        if (position.getX() < 0 || position.getX() > Constants.WIDTH ||
                position.getY() < 0 || position.getY() > Constants.HEIGHT) {
            Destroy();
        }

        // Actualizar la hitbox en cada frame
        updateHitbox();

        collidesWith();
    }

    /**
     * Logica de colision sobrescrita.
     * Los lasers del jugador usan una hitbox rectangular precisa.
     * Los lasers enemigos usan la logica circular simple (heredada).
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

            // El laser del jugador NO colisiona con:
            // 1. Otros lasers (sean enemigos o no)
            // 2. El propio jugador
            if (m instanceof Laser || m instanceof Player) {
                continue;
            }

            // El MiniBoss tiene su propia logica de colision (collidesWithLasers)
            if (m instanceof MiniBoss) {
                continue;
            }

            // --- INICIO DE LA SOLUCION ---
            // Ignorar los power-ups para que no sean destruidos
            if (m instanceof PowerUp) {
                continue;
            }
            // --- FIN DE LA SOLUCION ---

            // Comprobar colision rectangular contra los 'bounds' del otro objeto
            if (this.hitbox.intersects(m.getBounds())) {

                // Anadir la explosion en el centro del objeto golpeado
                gameState.playExplosion(m.getCenter());

                m.setLastHitByPlayer(true);

                // Destruir el meteoro/ufo y el laser
                m.Destroy();
                this.Destroy();

                // Salir del bucle, ya que este laser fue destruido
                break;
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform at = AffineTransform.getTranslateInstance(position.getX() - width / 2.0, position.getY());
        at.rotate(angle, width / 2.0, 0);
        g2d.drawImage(texture, at, null);
    }

    @Override
    public Vector2D getCenter() {
        // Corregido: El centro debe basarse en la altura (height) y la rotacion.

        // 1. Encontrar el centro visual sin rotar
        Point2D.Double center = new Point2D.Double(position.getX(), position.getY() + height / 2.0);

        // 2. Crear una transformacion SOLO para la rotacion alrededor del pivote
        AffineTransform rotation = AffineTransform.getRotateInstance(angle, position.getX(), position.getY());

        // 3. Aplicar la rotacion al punto central
        Point2D.Double rotatedCenter = new Point2D.Double();
        rotation.transform(center, rotatedCenter);

        // 4. Devolver el nuevo centro rotado
        return new Vector2D(rotatedCenter.getX(), rotatedCenter.getY());
    }
}