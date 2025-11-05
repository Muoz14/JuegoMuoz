package input;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Point;

public class MouseInput extends MouseAdapter {

    // Posición del mouse
    private static int mouseX = 0;
    private static int mouseY = 0;

    // Estado del botón izquierdo
    private static boolean pressed = false;

    // ------------------- Métodos de Java -------------------
    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            pressed = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            pressed = false;
        }
    }

    // ------------------- Métodos públicos -------------------
    // Devuelve la posición actual del mouse
    public static Point getMousePosition() {
        return new Point(mouseX, mouseY);
    }

    // Devuelve si el botón izquierdo está presionado
    public static boolean isPressed() {
        return pressed;
    }

    // Método opcional para "resetear" el click después de usarlo
    public static void releaseClick() {
        pressed = false;
    }

    // Llamar en cada update si quieres procesar algo cada frame
    public static void update() {
        // Por ahora no hace nada, solo para mantener compatibilidad con tu código actual
    }
}
