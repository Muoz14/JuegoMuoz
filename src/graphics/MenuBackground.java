package graphics;

import gameObject.Constants;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * Gestiona el fondo parallax del menú.
 * Dibuja 1 capa estática de 4096x4096 (centrada)
 * y 4 capas de estrellas de 4096x4096 que se desplazan.
 */
public class MenuBackground {

    /**
     * Clase interna para una sola capa de fondo scrollable.
     */
    private class Layer {
        private BufferedImage image;
        private double x; // Posición horizontal actual
        private double speedX; // Velocidad de scroll
        private int imgWidth;

        /**
         * Constructor para una capa de fondo scrollable.
         * @param image La textura (ej. 4096x4096)
         * @param speedX Velocidad de scroll horizontal
         */
        Layer(BufferedImage image, double speedX) {
            this.image = image;
            this.speedX = speedX;
            this.x = 0; // Empezar en 0

            if (image != null) {
                this.imgWidth = image.getWidth(); // 4096
            } else {
                this.imgWidth = Constants.WIDTH; // Fallback
            }
        }

        public void update() {
            // No mover si la velocidad es 0 (capa estática)
            if (speedX == 0) return;

            x += speedX;

            // Lógica de "wrap-around" (vuelta) para el scroll infinito
            // Si la imagen se salió completamente por la izquierda...
            if (x <= -imgWidth) {
                // ...la reseteamos a la derecha.
                x += imgWidth;
            }
        }

        public void draw(Graphics g) {
            if (image == null) return; // No dibujar si la imagen no cargó

            // Dibujar la imagen principal en su posición actual
            g.drawImage(image, (int)x, 0, null);

            // Dibujar la "copia" pegada a la derecha para el efecto de scroll
            // Esto es necesario para que no se vea un vacío
            g.drawImage(image, (int)x + imgWidth, 0, null);
        }
    }

    // --- Fin de la clase interna Layer ---

    private Layer[] layers;
    private BufferedImage staticBackground; // Capa 0 (estática)

    public MenuBackground() {
        layers = new Layer[4]; // 4 capas móviles

        // Capa 0 - Estática
        staticBackground = Assets.menuBg_Static;

        // Capas 1-4 - Móviles
        // (Ajusta las velocidades a tu gusto)

        // Estrellas pequeñas (lejanas, lentas)
        layers[0] = new Layer(Assets.menuStarsSmall1, -0.2);
        layers[1] = new Layer(Assets.menuStarsSmall2, -0.4);

        // Estrellas grandes (cercanas, rápidas)
        layers[2] = new Layer(Assets.menuStarsBig1, -0.7);
        layers[3] = new Layer(Assets.menuStarsBig2, -1.0);
    }

    /**
     * Actualiza la posición de todas las capas móviles.
     */
    public void update() {
        for (Layer layer : layers) {
            if (layer != null) {
                layer.update();
            }
        }
    }

    /**
     * Dibuja todas las capas en orden.
     */
    public void draw(Graphics g) {

        // 1. Dibujar el fondo estático primero (centrado)
        if (staticBackground != null) {
            // Centramos la imagen de 4096x4096
            int x = (Constants.WIDTH - staticBackground.getWidth()) / 2;
            int y = (Constants.HEIGHT - staticBackground.getHeight()) / 2;
            g.drawImage(staticBackground, x, y, null);
        }

        // 2. Dibujar todas las capas móviles encima
        for (Layer layer : layers) {
            if (layer != null) {
                layer.draw(g);
            }
        }
    }
}