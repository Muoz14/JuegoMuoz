package graphics; // O 'gameObject', como prefieras

import gameObject.Constants;
import java.awt.Graphics;

public class Background {

    private java.awt.image.BufferedImage bg1, bg2, bg3;

    // Posiciones 'x' para cada capa
    private double x1, x2, x3;

    // Velocidades. La mas lejana (1) es mas lenta. La mas cercana (3) es mas rapida.
    private double speed1 = 0.5;
    private double speed2 = 1.0;
    private double speed3 = 2.0;

    private int scaledWidth;

    public Background() {
        // Obtener las imagenes ya cargadas y escaladas
        bg1 = Assets.layer01;
        bg2 = Assets.layer02;
        bg3 = Assets.layer03;

        // El ancho escalado (11520)
        scaledWidth = bg1.getWidth();

        // Empezar todas las posiciones en 0
        x1 = 0;
        x2 = 0;
        x3 = 0;
    }

    public void update() {
        // Mover cada capa hacia la izquierda
        x1 -= speed1;
        x2 -= speed2;
        x3 -= speed3;

        // Logica de bucle:
        // Si la posicion 'x' es mas negativa que el ancho de la imagen,
        // significa que la primera copia ya salio completamente de la pantalla.
        // La reiniciamos a 0 para que el bucle sea infinito.
        if (x1 < -scaledWidth) {
            x1 = 0;
        }
        if (x2 < -scaledWidth) {
            x2 = 0;
        }
        if (x3 < -scaledWidth) {
            x3 = 0;
        }
    }

    public void draw(Graphics g) {
        // Dibujar la capa 1 (lejana)
        g.drawImage(bg1, (int) x1, 0, null);
        g.drawImage(bg1, (int) x1 + scaledWidth, 0, null); // Dibuja la copia

        // Dibujar la capa 2 (media)
        g.drawImage(bg2, (int) x2, 0, null);
        g.drawImage(bg2, (int) x2 + scaledWidth, 0, null); // Dibuja la copia

        // Dibujar la capa 3 (cercana)
        g.drawImage(bg3, (int) x3, 0, null);
        g.drawImage(bg3, (int) x3 + scaledWidth, 0, null); // Dibuja la copia
    }
}