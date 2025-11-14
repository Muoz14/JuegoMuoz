package states;

import gameObject.Constants;
import graphics.Assets;
// --- INICIO DE LA MODIFICACIÓN ---
import graphics.MenuBackground;
import graphics.Text; // <-- 1. IMPORTAR TEXT
import math.Vector2D; // <-- 2. IMPORTAR VECTOR2D
// --- FIN DE LA MODIFICACIÓN ---
import graphics.SoundManager;
import input.KeyBoard;
import input.MouseInput;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class SettingsState extends State {

    private Image buttonNormal, buttonHover;
    private Rectangle backButtonBounds;
    private Rectangle volumeBarBounds;
    private Rectangle volumeKnobBounds;

    private Map<String, Integer> keyBindings;
    private Map<String, Rectangle> keyButtons;
    private String waitingForKey = null;

    private State previousState;
    private boolean previousPausedState;
    private GameState gameStateRef;

    private MenuBackground menuBackground;

    public SettingsState(State previousState) {
        this.previousState = previousState;

        this.menuBackground = new MenuBackground();

        buttonNormal = Assets.buttonS1;
        buttonHover = Assets.buttonS2;

        int buttonWidth = 360;
        int buttonHeight = Assets.buttonS1.getHeight();

        int backX = Constants.WIDTH / 2 - buttonWidth / 2;
        int backY = Constants.HEIGHT - buttonHeight - 60;
        backButtonBounds = new Rectangle(backX, backY, buttonWidth, buttonHeight);

        keyBindings = new HashMap<>(SettingsData.getKeyBindings());
        keyBindings.putIfAbsent("UP", KeyEvent.VK_UP);
        keyBindings.putIfAbsent("DOWN", KeyEvent.VK_DOWN);
        keyBindings.putIfAbsent("LEFT", KeyEvent.VK_LEFT);
        keyBindings.putIfAbsent("RIGHT", KeyEvent.VK_RIGHT);
        keyBindings.putIfAbsent("SHOOT", KeyBoard.getShootKey());

        keyButtons = new HashMap<>();
        int startY = 200;
        int spacing = 80;
        int centerX = Constants.WIDTH / 2 - buttonWidth / 2;

        keyButtons.put("UP", new Rectangle(centerX, startY, buttonWidth, 60));
        keyButtons.put("DOWN", new Rectangle(centerX, startY + spacing, buttonWidth, 60));
        keyButtons.put("LEFT", new Rectangle(centerX, startY + spacing * 2, buttonWidth, 60));
        keyButtons.put("RIGHT", new Rectangle(centerX, startY + spacing * 3, buttonWidth, 60));
        keyButtons.put("SHOOT", new Rectangle(centerX, startY + spacing * 4, buttonWidth, 60));

        int volWidth = 300;
        int volHeight = 20;
        int volX = Constants.WIDTH / 2 - volWidth / 2;
        int volY = startY - 80;
        volumeBarBounds = new Rectangle(volX, volY, volWidth, volHeight);
        updateVolumeKnob();

        if (previousState instanceof GameState) {
            gameStateRef = (GameState) previousState;
            previousPausedState = gameStateRef.isPaused();
            gameStateRef.setPaused(true);
            SoundManager.getInstance().pauseAll();
        }
    }

    private void updateVolumeKnob() {
        float vol = SettingsData.getVolume();
        int knobX = (int) (volumeBarBounds.x + vol * volumeBarBounds.width - 10);
        volumeKnobBounds = new Rectangle(knobX, volumeBarBounds.y - 5, 20, 30);
    }

    private void applyVolumeChange() {
        if (gameStateRef != null && Assets.backgroundMusic != null) {
            Assets.backgroundMusic.setVolume(SettingsData.getVolume());
        }
        SoundManager.getInstance().setGlobalVolume(SettingsData.getVolume());
    }

    @Override
    public void update() {
        menuBackground.update();
        MouseInput.update();
        Point mouse = MouseInput.getMousePosition();

        if (MouseInput.isPressed() && volumeBarBounds.contains(mouse)) {
            float newVol = (float) (mouse.x - volumeBarBounds.x) / volumeBarBounds.width;
            newVol = Math.max(0f, Math.min(1f, newVol));
            SettingsData.setVolume(newVol);
            applyVolumeChange();
            updateVolumeKnob();
            MouseInput.releaseClick();
        }

        if (waitingForKey != null) {
            for (int i = 0; i < 256; i++) {
                if (KeyBoard.isKeyPressed(i)) {
                    keyBindings.put(waitingForKey, i);
                    SettingsData.setKeyBinding(waitingForKey, i);
                    if (waitingForKey.equals("SHOOT")) KeyBoard.setShootKey(i);
                    waitingForKey = null;
                    break;
                }
            }
        } else {
            for (String dir : keyButtons.keySet()) {
                if (keyButtons.get(dir).contains(mouse) && MouseInput.isPressed()) {
                    waitingForKey = dir;
                    MouseInput.releaseClick();
                    Assets.buttonSelected.play();
                }
            }
        }

        if (backButtonBounds.contains(mouse) && MouseInput.isPressed()) {
            if (gameStateRef != null) {
                gameStateRef.setPaused(previousPausedState);
                if (!previousPausedState) { // Solo reanudar si no estaba pausado antes
                    SoundManager.getInstance().resumeAll();
                }
            }
            Assets.buttonSelected.play();
            State.changeState(previousState);
            MouseInput.releaseClick();
        }
    }

    @Override
    public void draw(Graphics g) {

        menuBackground.draw(g);
        Graphics2D g2d = (Graphics2D) g; // <-- 3. Usar g2d

        g2d.setColor(Color.WHITE);
        g2d.setFont(Assets.fontBig);
        g2d.drawString("CONFIGURACIONES", Constants.WIDTH / 2 - 200, 100);

        g2d.setFont(Assets.fontMed);
        g2d.drawString("Volumen:", volumeBarBounds.x - 140, volumeBarBounds.y + 18);
        g2d.setColor(Color.GRAY);
        g2d.fillRect(volumeBarBounds.x, volumeBarBounds.y, volumeBarBounds.width, volumeBarBounds.height);
        g2d.setColor(Color.CYAN);
        g2d.fillRect(volumeBarBounds.x, volumeBarBounds.y,
                (int) (volumeBarBounds.width * SettingsData.getVolume()), volumeBarBounds.height);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(volumeKnobBounds.x, volumeKnobBounds.y, volumeKnobBounds.width, volumeKnobBounds.height);

        for (String dir : keyButtons.keySet()) {
            Rectangle rect = keyButtons.get(dir);
            boolean hover = rect.contains(MouseInput.getMousePosition());
            g2d.drawImage(hover ? buttonHover : buttonNormal, rect.x, rect.y, rect.width, rect.height, null);

            String label = getLabel(dir);
            Integer keyCode = keyBindings.get(dir);
            String keyName;

            if (waitingForKey != null && waitingForKey.equals(dir)) keyName = "Presiona una tecla...";
            else if (keyCode != null) keyName = KeyEvent.getKeyText(keyCode);
            else keyName = "Sin asignar";

            // --- INICIO DE LA MODIFICACIÓN (BOTONES DE TECLAS) ---
            String buttonText = label + ": " + keyName;
            Vector2D textPos = new Vector2D(
                    rect.getX() + rect.getWidth() / 2,
                    rect.getY() + rect.getHeight() / 2
            );
            // Usamos g2d, centramos el texto (true), color Negro y fuente mediana
            Text.drawText(g2d, buttonText, textPos, true, Color.BLACK, Assets.fontMed);
            // --- FIN DE LA MODIFICACIÓN ---
        }

        boolean hoverBack = backButtonBounds.contains(MouseInput.getMousePosition());
        g2d.drawImage(hoverBack ? buttonHover : buttonNormal,
                backButtonBounds.x, backButtonBounds.y,
                backButtonBounds.width, backButtonBounds.height, null);

        // --- INICIO DE LA MODIFICACIÓN (BOTÓN VOLVER) ---
        Vector2D backTextPos = new Vector2D(
                backButtonBounds.getX() + backButtonBounds.getWidth() / 2,
                backButtonBounds.getY() + backButtonBounds.getHeight() / 2
        );
        // Usamos g2d, centramos el texto (true), color Negro y fuente mediana
        Text.drawText(g2d, "VOLVER", backTextPos, true, Color.BLACK, Assets.fontMed);
        // --- FIN DE LA MODIFICACIÓN ---
    }

    private String getLabel(String key) {
        switch (key) {
            case "UP": return "Arriba";
            case "DOWN": return "Abajo";
            case "LEFT": return "Izquierda";
            case "RIGHT": return "Derecha";
            case "SHOOT": return "Disparo";
            default: return key;
        }
    }
}