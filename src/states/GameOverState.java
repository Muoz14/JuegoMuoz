package states;

import gameObject.Constants;
import graphics.Assets;
import input.MouseInput;

import java.awt.*;
import java.awt.image.BufferedImage;

public class GameOverState extends State {

    private int score;
    private int wave;

    // Efecto de vibracion para el texto "GAME OVER"
    private long startTime;
    private double shakeAmplitude = 4; // intensidad del temblor
    private double shakeSpeed = 0.1;

    // Botones
    private BufferedImage btnNormal, btnHover;
    private Rectangle playAgainButton;
    private Rectangle selectShipButton;
    private Rectangle menuButton;

    public GameOverState(int score, int wave) {
        this.score = score;
        this.wave = wave;
        this.startTime = System.currentTimeMillis();

        btnNormal = Assets.buttonS1;
        btnHover = Assets.buttonS2;

        int buttonWidth = 300;
        int buttonHeight = 60;
        int spacing = 60;
        int totalWidth = buttonWidth * 3 + spacing * 2;
        int startX = (1280 - totalWidth) / 2;
        int y = 500;

        playAgainButton = new Rectangle(startX, y, buttonWidth, buttonHeight);
        selectShipButton = new Rectangle(startX + buttonWidth + spacing, y, buttonWidth, buttonHeight);
        menuButton = new Rectangle(startX + (buttonWidth + spacing) * 2, y, buttonWidth, buttonHeight);
    }

    @Override
    public void update() {
        MouseInput.update();
        Point mouse = MouseInput.getMousePosition();

        // JUGAR DE NUEVO
        if (playAgainButton.contains(mouse) && MouseInput.isPressed()) {
            Assets.buttonSelected.play();
            State.changeState(new GameState());
            MouseInput.releaseClick();
        }

        // ELEGIR NAVE
        if (selectShipButton.contains(mouse) && MouseInput.isPressed()) {
            Assets.buttonSelected.play();
            State.changeState(new ShipSelectionState());
            MouseInput.releaseClick();
        }

        // MENU PRINCIPAL
        if (menuButton.contains(mouse) && MouseInput.isPressed()) {
            Assets.buttonSelected.play();
            State.changeState(new MenuState());
            MouseInput.releaseClick();
        }
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, Constants.WIDTH , Constants.HEIGHT);

        // --- Texto "GAME OVER" ---
        long elapsed = System.currentTimeMillis() - startTime;
        double offsetX = Math.sin(elapsed * shakeSpeed) * shakeAmplitude;
        double offsetY = Math.cos(elapsed * shakeSpeed * 0.12) * shakeAmplitude;

        g2d.setFont(Assets.fontBig);
        g2d.setColor(Color.RED);
        String gameOverText = "GAME OVER";
        int textWidth = g2d.getFontMetrics().stringWidth(gameOverText);
        int textX = (Constants.WIDTH  - textWidth) / 2 + (int) offsetX;
        int textY = 250 + (int) offsetY;
        g2d.drawString(gameOverText, textX, textY);

        // --- Puntuacion y oleada ---
        g2d.setFont(Assets.fontMed);

        // PUNTUACION
        String scoreLabel = "PUNTUACION ALCANZADA: ";
        String scoreValue = String.valueOf(score);
        int scoreLabelWidth = g2d.getFontMetrics().stringWidth(scoreLabel);
        int totalWidthScore = scoreLabelWidth + g2d.getFontMetrics().stringWidth(scoreValue);
        int scoreX = (Constants.WIDTH  - totalWidthScore) / 2;
        int scoreY = 330;
        g2d.setColor(new Color(135, 206, 250)); // azul claro
        g2d.drawString(scoreLabel, scoreX, scoreY);
        g2d.setColor(Color.WHITE);
        g2d.drawString(scoreValue, scoreX + scoreLabelWidth, scoreY);

        // OLEADA
        String waveLabel = "OLEADA ALCANZADA: ";
        String waveValue = String.valueOf(wave);
        int waveLabelWidth = g2d.getFontMetrics().stringWidth(waveLabel);
        int totalWidthWave = waveLabelWidth + g2d.getFontMetrics().stringWidth(waveValue);
        int waveX = (Constants.WIDTH  - totalWidthWave) / 2;
        int waveY = 370;
        g2d.setColor(new Color(135, 206, 250));
        g2d.drawString(waveLabel, waveX, waveY);
        g2d.setColor(Color.WHITE);
        g2d.drawString(waveValue, waveX + waveLabelWidth, waveY);

        // --- Botones ---
        drawButton(g2d, playAgainButton, "JUGAR DE NUEVO", MouseInput.getMousePosition());
        drawButton(g2d, selectShipButton, "ELEGIR NAVE", MouseInput.getMousePosition());
        drawButton(g2d, menuButton, "MENU PRINCIPAL", MouseInput.getMousePosition());
    }

    private void drawButton(Graphics2D g2d, Rectangle rect, String text, Point mouse) {
        boolean hover = rect.contains(mouse);
        BufferedImage img = hover ? btnHover : btnNormal;

        g2d.drawImage(img, rect.x, rect.y, rect.width, rect.height, null);
        g2d.setColor(Color.BLACK);
        g2d.setFont(Assets.fontMed);

        int textWidth = g2d.getFontMetrics().stringWidth(text);
        int textX = rect.x + (rect.width - textWidth) / 2;
        int textY = rect.y + rect.height / 2 + 7;
        g2d.drawString(text, textX, textY);
    }
}