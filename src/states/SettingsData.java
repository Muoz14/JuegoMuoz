package states;

import java.awt.event.KeyEvent;
import java.util.HashMap;

public class SettingsData {

    private static float volume = 0.7f; // volumen general (0.0 a 1.0)
    private static HashMap<String, Integer> keyBindings = new HashMap<>();

    static {
        keyBindings.put("UP", KeyEvent.VK_UP);
        keyBindings.put("DOWN", KeyEvent.VK_DOWN);
        keyBindings.put("LEFT", KeyEvent.VK_LEFT);
        keyBindings.put("RIGHT", KeyEvent.VK_RIGHT);
        keyBindings.put("SHOOT", KeyEvent.VK_SPACE);
    }

    public static float getVolume() {
        return volume;
    }

    public static void setVolume(float newVolume) {
        volume = Math.max(0, Math.min(1, newVolume));
    }

    public static HashMap<String, Integer> getKeyBindings() {
        return new HashMap<>(keyBindings);
    }

    public static void setKeyBinding(String key, int value) {
        keyBindings.put(key, value);
    }
}

