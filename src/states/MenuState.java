package states;

import gameObject.*;
import graphics.Assets;
import graphics.MenuBackground;
import input.MouseInput;
import math.Vector2D;
import ui.Action;
import ui.Button;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

public class MenuState extends State {

    private ArrayList<Button> buttons;
    private ArrayList<MenuMeteor> meteors;
    private ArrayList<MenuExplosion> explosions;

    private MenuBackground menuBackground;

    // Posición Y del primer botón
    private int startY;

    public MenuState() {
        buttons = new ArrayList<>();
        meteors = new ArrayList<>();
        explosions = new ArrayList<>();

        menuBackground = new MenuBackground();

        // ------------------ BOTONES (MODIFICADO) ------------------
        int buttonWidth = Assets.buttonS1.getWidth();
        int buttonHeight = Assets.buttonS1.getHeight();
        final int SPACING = 20;
        int centerX = Constants.WIDTH / 2 - buttonWidth / 2;

        // Ahora son 3 botones principales
        final int TOTAL_GROUP_HEIGHT = 3 * buttonHeight + 2 * SPACING;

        // Lo centramos, y luego le sumamos 50 píxeles para bajarlo todo
        this.startY = (Constants.HEIGHT / 2 - TOTAL_GROUP_HEIGHT / 2) + 50;

        // Boton JUGAR
        buttons.add(new Button(Assets.buttonS1, Assets.buttonS2, centerX, startY, Constants.PLAY, new Action() {
            @Override
            public void doAction() {
                Assets.buttonSelected.play();
                State.changeState(new ShipSelectionState());
            }
        }));

        // Boton PUNTUACIONES (Ajustar Y)
        buttons.add(new Button(Assets.buttonS1, Assets.buttonS2, centerX, startY + 1 * (buttonHeight + SPACING), Constants.SCORE, new Action() {
            @Override
            public void doAction() {
                Assets.buttonSelected.play();
                State.changeState(new ScoreState());
            }
        }));

        // Boton AJUSTES (Ajustar Y)
        buttons.add(new Button(Assets.buttonS1, Assets.buttonS2, centerX, startY + 2 * (buttonHeight + SPACING), Constants.SETTINGS, new Action() {
            @Override
            public void doAction() {
                Assets.buttonSelected.play();
                State.changeState(new SettingsState(MenuState.this));
            }
        }));

        // Boton SALIR (Sin cambios)
        final int MARGIN_X = 25;
        final int MARGIN_Y = 45;
        buttons.add(new Button(Assets.buttonS1, Assets.buttonS2, Constants.WIDTH - buttonWidth - MARGIN_X, Constants.HEIGHT - buttonHeight - MARGIN_Y, Constants.EXIT, new Action() {
            @Override
            public void doAction() {
                Assets.buttonSelected.play();
                System.exit(0);
            }
        }));

        // ------------------ METEORITOS DECORATIVOS ------------------
        int initialMeteors = 5 + (int) (Math.random() * 6);
        for (int i = 0; i < initialMeteors; i++) {
            addRandomMeteor();
        }
    }

    private void addRandomMeteor() {
        double x = Math.random() * Constants.WIDTH;
        double y = Math.random() * Constants.HEIGHT;
        Vector2D position = new Vector2D(x, y);
        Vector2D velocity = new Vector2D(Math.random() * 2 - 1, Math.random() * 2 - 1)
                .normalize()
                .scale(Math.random() + 0.5);
        int family = (int) (Math.random() * Assets.bigs.length);
        MenuMeteor meteor = new MenuMeteor(position, velocity, 1.5, Size.BIG, family);
        meteors.add(meteor);
    }

    @Override
    public void update() {
        MouseInput.update();
        menuBackground.update(); // Actualizar el fondo

        // Actualizar botones
        for (Button b : buttons) {
            b.update();
        }

        ArrayList<MenuMeteor> newMeteors = new ArrayList<>();
        ArrayList<MenuExplosion> newExplosions = new ArrayList<>();

        // Actualizar meteoritos
        Iterator<MenuMeteor> it = meteors.iterator();
        while (it.hasNext()) {
            MenuMeteor m = it.next();
            m.update();

            // Rebote con los bordes de pantalla
            if (m.getPosition().getX() < 0 || m.getPosition().getX() + m.getTexture().getWidth() > Constants.WIDTH) {
                m.setVelocity(new Vector2D(-m.getVelocity().getX(), m.getVelocity().getY()));
            }
            if (m.getPosition().getY() < 0 || m.getPosition().getY() + m.getTexture().getHeight() > Constants.HEIGHT) {
                m.setVelocity(new Vector2D(m.getVelocity().getX(), -m.getVelocity().getY()));
            }

            // Destruir meteorito con clic izquierdo
            Rectangle bounds = new Rectangle((int) m.getPosition().getX(), (int) m.getPosition().getY(), m.getTexture().getWidth(), m.getTexture().getHeight());
            if (MouseInput.isPressed() && bounds.contains(MouseInput.getMousePosition())) {
                // Anadir explosion
                newExplosions.add(new MenuExplosion(m.getCenter()));

                Assets.explosion.play();

                // Dividir meteorito si no es TINY
                newMeteors.addAll(m.split());
                it.remove();
                MouseInput.releaseClick();
            }
        }

        // Agregar meteoritos nuevos despues del bucle
        meteors.addAll(newMeteors);
        explosions.addAll(newExplosions);

        // Actualizar explosiones
        Iterator<MenuExplosion> expIt = explosions.iterator();
        while (expIt.hasNext()) {
            MenuExplosion e = expIt.next();
            e.update();
            if (!e.isRunning()) {
                expIt.remove();
            }
        }

        // Mantener meteoritos minimos
        while (meteors.size() < 5) {
            addRandomMeteor();
        }
    }


    @Override
    public void draw(Graphics g) {
        // Fondo
        menuBackground.draw(g);

        // --- INICIO DE LA MODIFICACIÓN (ORDEN DE DIBUJADO) ---

        // 1. Dibujar meteoritos
        for (MenuMeteor m : meteors) {
            m.draw(g);
        }

        // 2. Dibujar explosiones
        for (MenuExplosion e : explosions) {
            e.draw(g);
        }

        // 3. DIBUJAR TÍTULO (Tamaño Original 334x260)
        if (Assets.gameTitle != null) {

            // Obtener el ancho y alto de la imagen
            int titleWidth = Assets.gameTitle.getWidth();  // 334
            int titleHeight = Assets.gameTitle.getHeight(); // 260

            // Centrarlo horizontalmente
            int titleX = Constants.WIDTH / 2 - titleWidth / 2;

            // Posicionarlo 10px arriba del primer botón (startY)
            int titleY = this.startY - titleHeight - 10;

            // Dibujar la imagen sin escalar
            g.drawImage(Assets.gameTitle, titleX, titleY, null);
        }

        // 4. Dibujar botones
        for (Button b : buttons) {
            b.draw(g);
        }
        // --- FIN DE LA MODIFICACIÓN ---
    }

    // ------------------ SUBCLASES INTERNAS ------------------
    private class MenuMeteor extends Meteor {
        public MenuMeteor(Vector2D position, Vector2D velocity, double maxVel, Size size, int family) {
            super(position, velocity, maxVel, size.getTexture(family), null, size, family);
        }

        @Override
        protected void Destroy() {
        }

        // Dividir meteorito en meteoritos mas pequenos
        public ArrayList<MenuMeteor> split() {
            ArrayList<MenuMeteor> result = new ArrayList<>();
            Size nextSize = size.getNextSize();
            if (nextSize == null) return result;

            for (int i = 0; i < size.quantity; i++) {
                Vector2D newVel = new Vector2D(Math.random() * 2 - 1, Math.random() * 2 - 1)
                        .normalize()
                        .scale(Math.random() * 2 + 1);
                result.add(new MenuMeteor(this.getCenter(), newVel, 1.5, nextSize, family));
            }
            return result;
        }
    }

    private class MenuExplosion extends Explosion {
        public MenuExplosion(Vector2D position) {
            super(position, null);
        }

        @Override
        protected void Destroy() {
        }

        public boolean isRunning() {
            return animation.isRunning();
        }
    }
}