package states;

import gameObject.Constants;
import gameObject.ScoreEntry;
import gameObject.ScoreManager;
import graphics.Assets;
import graphics.MenuBackground;
import graphics.Text;
import math.Vector2D;
import ui.Action;
import ui.Button;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

public class ScoreState extends State {

    private MenuBackground menuBackground;
    private Button backButton;
    private List<ScoreEntry> highScores;

    // Posiciones X para las columnas (ajusta esto a tu gusto)
    private final int X_RANK = Constants.WIDTH / 2 - 250;
    private final int X_NAME = Constants.WIDTH / 2 - 150;
    private final int X_SCORE = Constants.WIDTH / 2 + 150;
    private final int Y_START = 200;
    private final int Y_SPACING = 35;

    public ScoreState() {
        menuBackground = new MenuBackground();

        // Cargar las 15 mejores puntuaciones
        highScores = ScoreManager.getHighScores(15);

        // Botón de Volver
        int btnWidth = Assets.buttonS1.getWidth();
        int btnHeight = Assets.buttonS1.getHeight();
        backButton = new Button(Assets.buttonS1, Assets.buttonS2,
                Constants.WIDTH / 2 - btnWidth / 2, // Centrado
                Constants.HEIGHT - btnHeight - 60, // Abajo
                "VOLVER",
                new Action() {
                    @Override
                    public void doAction() {
                        Assets.buttonSelected.play();
                        State.changeState(new MenuState());
                    }
                }
        );
    }

    @Override
    public void update() {
        menuBackground.update();
        backButton.update();
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // 1. Fondo
        menuBackground.draw(g);

        // 2. Título
        Text.drawText(g2d, "MEJORES PUNTUACIONES",
                new Vector2D(Constants.WIDTH / 2, 100),
                true, Color.WHITE, Assets.fontBig);

        // 3. Encabezados de la "tabla" (sin líneas)
        g.setFont(Assets.fontMed); // Usamos la fuente mediana

        // Color de encabezado (ej. Amarillo)
        g.setColor(new Color(255, 215, 0));

        // Dibujamos texto normal (no centrado)
        g.drawString("POS.", X_RANK, Y_START - Y_SPACING);
        g.drawString("NOMBRE", X_NAME, Y_START - Y_SPACING);
        g.drawString("PUNTOS", X_SCORE, Y_START - Y_SPACING);

        // 4. Dibujar las puntuaciones
        g.setColor(Color.WHITE); // Puntuaciones en blanco

        for (int i = 0; i < highScores.size(); i++) {
            ScoreEntry entry = highScores.get(i);
            int currentY = Y_START + (i * Y_SPACING);

            String rank = String.valueOf(i + 1) + ".";
            String name = entry.name.toUpperCase();
            String score = String.valueOf(entry.score);

            g.drawString(rank, X_RANK, currentY);
            g.drawString(name, X_NAME, currentY);
            g.drawString(score, X_SCORE, currentY);
        }

        // 5. Botón de Volver
        backButton.draw(g);
    }
}