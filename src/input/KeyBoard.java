package input;

import states.SettingsData;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyBoard implements KeyListener {

    private static boolean[] keys = new boolean[256];
    private static boolean[] justPressed = new boolean[256];
    private static boolean[] cantPress = new boolean[256];

    private static int shootKey = KeyEvent.VK_SPACE; // por default

    public static boolean UP, DOWN, LEFT, RIGTH, SHOOT, A, D, W, S, ESC;

    public static void update() {
        for (int i = 0; i < keys.length; i++) {
            if (cantPress[i] && !keys[i]) {
                cantPress[i] = false;
            } else if (justPressed[i]) {
                cantPress[i] = true;
                justPressed[i] = false;
            }
            if (!cantPress[i] && keys[i]) {
                justPressed[i] = true;
            }
        }

        UP = keys[KeyEvent.VK_UP];
        DOWN = keys[KeyEvent.VK_DOWN];
        LEFT = keys[KeyEvent.VK_LEFT];
        RIGTH = keys[KeyEvent.VK_RIGHT];
        SHOOT = keys[KeyEvent.VK_SPACE];

        A = keys[KeyEvent.VK_A];
        D = keys[KeyEvent.VK_D];
        W = keys[KeyEvent.VK_W];
        S = keys[KeyEvent.VK_S];

        ESC = keys[KeyEvent.VK_ESCAPE];
    }

    public static boolean isKeyPressed(int keyCode) {
        if (keyCode < 0 || keyCode >= keys.length) return false;
        return keys[keyCode];
    }

    public static boolean isKeyJustPressed(int keyCode) {
        if (keyCode < 0 || keyCode >= justPressed.length) return false;
        return justPressed[keyCode];
    }

    // Métodos dinámicos para controles configurables
    public static boolean UP() { return isKeyPressed(SettingsData.getKeyBindings().getOrDefault("UP", KeyEvent.VK_W)); }
    public static boolean DOWN() { return isKeyPressed(SettingsData.getKeyBindings().getOrDefault("DOWN", KeyEvent.VK_S)); }
    public static boolean LEFT() { return isKeyPressed(SettingsData.getKeyBindings().getOrDefault("LEFT", KeyEvent.VK_A)); }
    public static boolean RIGTH() { return isKeyPressed(SettingsData.getKeyBindings().getOrDefault("RIGHT", KeyEvent.VK_D)); }
    public static boolean SHOOT() { return isKeyPressed(SettingsData.getKeyBindings().getOrDefault("SHOOT", shootKey)); }

    public static void setShootKey(int key) { shootKey = key; }
    public static int getShootKey() { return shootKey; }

    // END FRAME manual
    public static void endFrame() {
        update();
    }

    @Override
    public void keyTyped(java.awt.event.KeyEvent e) { }

    @Override
    public void keyPressed(java.awt.event.KeyEvent e) {
        if (e.getKeyCode() < 256) keys[e.getKeyCode()] = true;
    }

    @Override
    public void keyReleased(java.awt.event.KeyEvent e) {
        if (e.getKeyCode() < 256) keys[e.getKeyCode()] = false;
    }
}
