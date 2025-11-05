package gameObject;

import math.Vector2D;
import java.awt.*;
import java.awt.image.BufferedImage;

// Clase abstracta que representa a todos los objetos del juego
public abstract class GameObject {

    // A los objetos de tipo protected solo pueden acceder los que heredan de esta clase
    protected BufferedImage texture;
    protected Vector2D position; // Posición del objeto

    // Constructor
    public GameObject(Vector2D position, BufferedImage texture) {
        this.position = position;
        this.texture = texture;
    }

    // Métodos abstractos que serán definidos por las clases hijas
    public abstract void update();
    public abstract void draw(Graphics g);

    // Getter y Setter para la posición
    public Vector2D getPosition() {
        return position;
    }

    public void setPosition(Vector2D position) {
        this.position = position;
    }
}
