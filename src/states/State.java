package states;

import java.awt.*;

public abstract class State {

    // Estado actual del juego
    private static State currentState = null;

    // Obtener el estado actual
    public static State getCurrentState() {
        return currentState;
    }

    // Cambiar de estado (por ejemplo: menú, juego, game over, etc.)
    public static void changeState(State newState) {
        currentState = newState;
    }

    // Métodos que deben implementar las clases hijas
    public abstract void update();
    public abstract void draw(Graphics g);
}
