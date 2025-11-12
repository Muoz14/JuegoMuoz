package gameObject;

import graphics.Assets;
import math.Vector2D;
import states.GameState;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList; // --- Asegurate de importar ArrayList ---

public class PowerUp extends MovingObject {

    private PowerUpType type;
    private long spawnTime;
    private static final long DURATION = 8000;
    private static final long FLICKER_TIME = 200;
    private boolean isFlickering = false;
    private boolean visible = true;
    private Chronometer flickerTimer;

    private BufferedImage orbTexture;
    private double iconRotation;

    public PowerUp(Vector2D position, PowerUpType type, GameState gameState) {
        super(position, new Vector2D(), 0, type.texture, gameState);
        this.type = type;
        this.spawnTime = System.currentTimeMillis();
        this.flickerTimer = new Chronometer();
        this.immuneToMeteors = true;

        this.orbTexture = Assets.orb;
        this.iconRotation = 0.0;
    }

    @Override
    public void update() {
        long elapsed = System.currentTimeMillis() - spawnTime;

        if (elapsed > DURATION) {
            Destroy();
            return;
        }

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

        iconRotation += 0.02;

        collidesWithPlayer();
    }

    private void collidesWithPlayer() {
        Player player = gameState.getPlayer();
        if (player == null || player.isDead() || player.isSpawning()) {
            return;
        }

        double distance = getCenter().subtract(player.getCenter()).getMagnitude();
        if (distance < (width / 2.0) + (player.width / 2.0)) {
            // ¡Colision!

            // --- LOGICA DE MENSAJE REFACTORIZADA ---
            // Ahora la creacion del mensaje esta dentro de cada case

            switch (type.category) {
                case SHIELD:
                    player.activateShield(type);
                    createPickupMessage(type.message);
                    break;
                case RAPID_FIRE:
                    player.activateRapidFire(type);
                    createPickupMessage(type.message);
                    break;
                case MULTI_SHOT:
                    player.activateMultiShot(type);
                    createPickupMessage(type.message);
                    break;
                case EXTRA_LIFE:
                    gameState.addLife();
                    createPickupMessage(type.message);
                    break;
                case SCORE_MULTIPLIER:
                    player.activateScoreMultiplier(type);
                    createPickupMessage(type.message);
                    break;

                // --- NUEVO ---
                case RANDOM:
                    handleRandomPowerUp(); // Este metodo creara su propio mensaje
                    break;
                // --- FIN NUEVO ---
            }

            Destroy();
        }
    }

    /**
     * Metodo helper para crear el mensaje flotante estandar
     */
    private void createPickupMessage(String message) {
        Message pickupMsg = new Message(
                gameState.getPlayer().getCenter(),
                false,
                message,
                Color.CYAN,
                true,
                Assets.fontMed,
                gameState
        );
        pickupMsg.setLifespan(2500);
        gameState.addMessage(pickupMsg);
    }

    // --- NUEVO METODO ---
    /**
     * Logica para el power-up ALEATORIO.
     * Escoge un power-up (que no sea Vida Extra o Aleatorio),
     * duplica su duracion, y lo activa.
     */
    private void handleRandomPowerUp() {
        Player player = gameState.getPlayer();

        // 1. Crear lista de power-ups "elegibles" (con duracion)
        ArrayList<PowerUpType> eligibleTypes = new ArrayList<>();
        for (PowerUpType t : PowerUpType.values()) {
            // Solo queremos los que tienen duracion (por eso excluimos EXTRA_LIFE)
            // Y no queremos que vuelva a salir RANDOM
            if (t.category != PowerUpCategory.RANDOM && t.category != PowerUpCategory.EXTRA_LIFE) {
                eligibleTypes.add(t);
            }
        }

        // Si por alguna razon la lista esta vacia, no hacer nada
        if (eligibleTypes.isEmpty()) {
            return;
        }

        // 2. Escoger uno al azar
        PowerUpType chosenType = eligibleTypes.get( (int)(Math.random() * eligibleTypes.size()) );

        // 3. Calcular la duracion x2
        long doubledDuration = chosenType.duration * 2;

        // 4. Activar el power-up escogido usando el metodo sobrecargado
        switch (chosenType.category) {
            case SHIELD:
                player.activateShield(chosenType, doubledDuration);
                break;
            case RAPID_FIRE:
                player.activateRapidFire(chosenType, doubledDuration);
                break;
            case MULTI_SHOT:
                player.activateMultiShot(chosenType, doubledDuration);
                break;
            case SCORE_MULTIPLIER:
                player.activateScoreMultiplier(chosenType, doubledDuration);
                break;
            // No incluimos EXTRA_LIFE o RANDOM
        }

        // 5. Crear el mensaje especial "stacked"
        String randomMessage = "¡ALEATORIO!\n" + chosenType.message + " X2";
        Message pickupMsg = new Message(
                player.getCenter(),
                false,
                randomMessage, // Mensaje especial
                Color.MAGENTA, // Color especial
                true,
                Assets.fontMed,
                gameState
        );
        pickupMsg.setLifespan(3500); // Un poco mas de duracion
        gameState.addMessage(pickupMsg);
    }
    // --- FIN NUEVO METODO ---

    @Override
    public void draw(Graphics g) {
        if (!visible) return;

        Graphics2D g2d = (Graphics2D) g;
        BufferedImage iconTexture = this.texture;
        double sway = Math.sin(System.currentTimeMillis() * 0.002) * 5;
        Vector2D center = getCenter();

        // 1. Dibujar Orbe
        double orbX = center.getX() - (orbTexture.getWidth() / 2.0);
        double orbY = center.getY() - (orbTexture.getHeight() / 2.0) + sway;
        AffineTransform atOrb = AffineTransform.getTranslateInstance(orbX, orbY);
        g2d.drawImage(orbTexture, atOrb, null);

        // 2. Dibujar Icono
        double iconX = position.getX();
        double iconY = position.getY() + sway;
        AffineTransform atIcon = AffineTransform.getTranslateInstance(iconX, iconY);
        atIcon.rotate(iconRotation, width / 2.0, height / 2.0);
        g2d.drawImage(iconTexture, atIcon, null);
    }

    @Override
    protected void Destroy() {
        gameState.removeObject(this);
    }
}