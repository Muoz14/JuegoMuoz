package gameObject;

import graphics.Assets;
import math.Vector2D;
import states.GameState;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class PowerUp extends MovingObject {

    private PowerUpType type;
    private long spawnTime;
    private static final long DURATION = 8000; // Cuanto tiempo esta en pantalla (8s)
    private static final long FLICKER_TIME = 200; // Parpadeo
    private boolean isFlickering = false;
    private boolean visible = true;
    private Chronometer flickerTimer;

    public PowerUp(Vector2D position, PowerUpType type, GameState gameState) {
        super(position, new Vector2D(), 0, type.texture, gameState); // Velocidad 0
        this.type = type;
        this.spawnTime = System.currentTimeMillis();
        this.flickerTimer = new Chronometer();
        this.immuneToMeteors = true; // Que no lo destruyan meteoros
    }

    @Override
    public void update() {
        long elapsed = System.currentTimeMillis() - spawnTime;

        // Desaparece despues de DURATION
        if (elapsed > DURATION) {
            Destroy();
            return;
        }

        // Empieza a parpadear cuando le quedan 3 segundos
        if (DURATION - elapsed < 3000 && !isFlickering) {
            isFlickering = true;
            flickerTimer.run(FLICKER_TIME);
        }

        if (isFlickering) {
            flickerTimer.update();
            if (flickerTimer.isFinished()) {
                visible = !visible;
                flickerTimer.run(FLICKER_TIME);
            }
        }

        // Chequear colision con el jugador
        collidesWithPlayer();
    }

    private void collidesWithPlayer() {
        Player player = gameState.getPlayer();
        if (player == null || player.isDead() || player.isSpawning()) {
            return;
        }

        // Usamos colision circular simple
        double distance = getCenter().subtract(player.getCenter()).getMagnitude();
        if (distance < (width / 2.0) + (player.width / 2.0)) {
            // ¡Colision!
            player.activateShield(type);

            // --- INICIO DE LA SOLUCION: CAMBIAR FUENTE ---
            Message pickupMsg = new Message(
                    player.getCenter(),
                    false, // No se desvanece
                    type.message,
                    Color.CYAN,
                    true,
                    Assets.fontMed, // <-- CAMBIADO A FUENTE MEDIANA
                    gameState
            );
            // --- FIN DE LA SOLUCION ---

            pickupMsg.setLifespan(2500); // 2.5s en pantalla
            gameState.addMessage(pickupMsg);

            // Destruir el power-up
            Destroy();
        }
    }

    @Override
    public void draw(Graphics g) {
        if (!visible) return;

        Graphics2D g2d = (Graphics2D) g;
        AffineTransform at = AffineTransform.getTranslateInstance(position.getX(), position.getY());

        // Efecto visual de "flotar"
        double sway = Math.sin(System.currentTimeMillis() * 0.002) * 5; // 5 pixeles de sube/baja
        at.translate(0, sway);

        g2d.drawImage(texture, at, null);
    }

    @Override
    protected void Destroy() {
        // Solo lo quitamos de la lista
        gameState.removeObject(this);
    }
}