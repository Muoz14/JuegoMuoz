package states;

import gameObject.Constants;
import gameObject.PlayerData;
import graphics.Assets;
import graphics.MenuBackground;
import graphics.Text;
import input.KeyBoard;
import math.Vector2D;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

public class NameInputState extends State {

    private MenuBackground menuBackground;
    private String playerName = "";
    private String promptMessage = "INGRESA UN APODO";
    private String confirmMessage = "(Presiona ENTER para continuar)";

    // Para el cursor parpadeante
    private long lastBlinkTime = System.currentTimeMillis();
    private boolean drawCursor = true;
    private final int MAX_NAME_LENGTH = 10; // Límite de caracteres

    public NameInputState() {
        menuBackground = new MenuBackground();
    }

    @Override
    public void update() {
        menuBackground.update();

        // Logica de parpadeo del cursor
        long now = System.currentTimeMillis();
        if (now - lastBlinkTime > 500) { // Parpadea cada 500ms
            drawCursor = !drawCursor;
            lastBlinkTime = now;
        }

        handleKeyInput();
    }

    private void handleKeyInput() {
        // 1. Confirmar Nombre
        if (KeyBoard.isKeyJustPressed(KeyEvent.VK_ENTER)) {
            if (!playerName.trim().isEmpty()) {
                PlayerData.setCurrentPlayerName(playerName);
                State.changeState(new GameState());
            }
        }

        // 2. Borrar (Backspace)
        if (KeyBoard.isKeyJustPressed(KeyEvent.VK_BACK_SPACE)) {
            if (playerName.length() > 0) {
                playerName = playerName.substring(0, playerName.length() - 1);
            }
        }

        // 3. Escribir caracteres
        // Iteramos por las teclas presionadas 'justo ahora'
        for (int i = 0; i < 256; i++) {
            if (KeyBoard.isKeyJustPressed(i)) {
                // Solo nos interesan A-Z, 0-9 y Espacio
                char keyChar = (char) i;
                if ((keyChar >= 'A' && keyChar <= 'Z') ||
                        (keyChar >= '0' && keyChar <= '9') ||
                        i == KeyEvent.VK_SPACE)
                {
                    if (playerName.length() < MAX_NAME_LENGTH) {
                        playerName += keyChar;
                    }
                }
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // 1. Fondo
        menuBackground.draw(g);

        // 2. Mensaje de Título
        Text.drawText(g2d, promptMessage,
                new Vector2D(Constants.WIDTH / 2, Constants.HEIGHT / 2 - 100),
                true, Color.WHITE, Assets.fontBig);

        // 3. El nombre que se está escribiendo
        String nameToShow = playerName;
        if (drawCursor) {
            nameToShow += "_"; // Simular cursor
        }

        Text.drawText(g2d, nameToShow,
                new Vector2D(Constants.WIDTH / 2, Constants.HEIGHT / 2),
                true, Color.CYAN, Assets.fontBig);

        // 4. Mensaje de confirmación
        Text.drawText(g2d, confirmMessage,
                new Vector2D(Constants.WIDTH / 2, Constants.HEIGHT / 2 + 80),
                true, Color.GRAY, Assets.fontMed);
    }
}