package graphics;

import math.Vector2D;

import java.awt.*;

public class Text {

    public static void drawText(Graphics g, String text, Vector2D pos, boolean center, Color color, Font font){

        g.setColor(color);
        g.setFont(font);
        Vector2D position = new Vector2D(pos.getX(), pos.getY());

        if (center){ // Esta lógica mueve el punto de inicio para que el centro sea (pos.getX(), pos.getY())

            FontMetrics fm = g.getFontMetrics();
            // Desplaza X a la izquierda por la mitad del ancho del texto
            position.setX(position.getX() - fm.stringWidth(text) / 2);
            // Desplaza Y ligeramente hacia arriba para el centrado visual perfecto (ajuste fino)
            position.setY(position.getY() + fm.getHeight() / 3);
        }

        g.drawString(text, (int)position.getX(), (int)position.getY());

    }

}
