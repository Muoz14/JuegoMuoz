package states;

import gameObject.Constants;
import graphics.Assets;
import graphics.MenuBackground;
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
        menuBackground.update(); // ACTUALIZAR EL FONDO
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
                SoundManager.getInstance().resumeAll();
            }
            Assets.buttonSelected.play();
            State.changeState(previousState);
            MouseInput.releaseClick();
        }
    }

    @Override
    public void draw(Graphics g) {

        menuBackground.draw(g); // DIBUJAR EL FONDO

        g.setColor(Color.WHITE);
        g.setFont(Assets.fontBig);
        g.drawString("CONFIGURACIONES", Constants.WIDTH / 2 - 200, 100);

        g.setFont(Assets.fontMed);
        g.drawString("Volumen:", volumeBarBounds.x - 140, volumeBarBounds.y + 18);
        g.setColor(Color.GRAY);
        g.fillRect(volumeBarBounds.x, volumeBarBounds.y, volumeBarBounds.width, volumeBarBounds.height);
        g.setColor(Color.CYAN);
        g.fillRect(volumeBarBounds.x, volumeBarBounds.y,
                (int) (volumeBarBounds.width * SettingsData.getVolume()), volumeBarBounds.height);
        g.setColor(Color.WHITE);
        g.fillRect(volumeKnobBounds.x, volumeKnobBounds.y, volumeKnobBounds.width, volumeKnobBounds.height);

        for (String dir : keyButtons.keySet()) {
            Rectangle rect = keyButtons.get(dir);
            boolean hover = rect.contains(MouseInput.getMousePosition());
            g.drawImage(hover ? buttonHover : buttonNormal, rect.x, rect.y, rect.width, rect.height, null);

            String label = getLabel(dir);
            Integer keyCode = keyBindings.get(dir);
            String keyName;

            if (waitingForKey != null && waitingForKey.equals(dir)) keyName = "Presiona una tecla...";
            else if (keyCode != null) keyName = KeyEvent.getKeyText(keyCode);
            else keyName = "Sin asignar";

            g.setColor(Color.BLACK);
            g.drawString(label + ": " + keyName, rect.x + 25, rect.y + 38);
        }

        boolean hoverBack = backButtonBounds.contains(MouseInput.getMousePosition());
        g.drawImage(hoverBack ? buttonHover : buttonNormal,
                backButtonBounds.x, backButtonBounds.y,
                backButtonBounds.width, backButtonBounds.height, null);
        g.setColor(Color.BLACK);
        g.drawString("VOLVER", backButtonBounds.x + 110, backButtonBounds.y + 45);
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