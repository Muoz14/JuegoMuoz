package states;

import gameObject.ShipLibrary;
import gameObject.ShipData;
import graphics.Assets;
import graphics.MenuBackground;
import graphics.Animation; // Importar Animation
import math.Vector2D; // Importar Vector2D
import input.KeyBoard;
import ui.Action;
import ui.Button;
import gameObject.Constants;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ShipSelectionState extends State {

    private ArrayList<Button> buttons;
    private int selectedShipIndex = 0;
    private int selectedLaserIndex = 0;

    private MenuBackground menuBackground;

    // --- INICIO DE LA MODIFICACION ---
    private Animation boltPreviewAnimation; // Animacion para el laser bolt
    private Animation sparkPreviewAnimation; // Animacion para el laser spark
    private Animation pulsePreviewAnimation; // Animacion para el laser pulse
    // --- FIN DE LA MODIFICACION ---

    // Variables para ajustar posiciones
    private int textOffsetX = -100;
    private int textOffsetY = 0;
    private int shipOffsetX = -10;
    private int shipOffsetY = 50;
    private int laserOffsetX = -10;
    private int laserOffsetY = 20;
    private int laserPreviewWidth = 30;
    private int laserPreviewHeight = 80;

    // Evitar parpadeo
    private boolean leftPressedLastFrame = false;
    private boolean rightPressedLastFrame = false;
    private boolean upPressedLastFrame = false;
    private boolean downPressedLastFrame = false;

    public ShipSelectionState() {

        menuBackground = new MenuBackground();

        buttons = new ArrayList<>();

        // --- INICIO DE LA MODIFICACION ---
        // Inicializar las animaciones de preview (100ms por frame, en bucle)
        boltPreviewAnimation = new Animation(Assets.boltLasers, 100, new Vector2D(), true);
        sparkPreviewAnimation = new Animation(Assets.sparkLasers, 100, new Vector2D(), true);
        pulsePreviewAnimation = new Animation(Assets.pulseLasers, 100, new Vector2D(), true);
        // --- FIN DE LA MODIFICACION ---

        int buttonWidth = Assets.buttonS1.getWidth();
        int buttonHeight = Assets.buttonS1.getHeight();

        // Posicion base de botones
        int baseY = Constants.HEIGHT - 150;
        int startX = Constants.WIDTH / 2 - 320;
        int spacing = 350; // separacion de botones

        // Boton "CONFIRMAR"
        buttons.add(new Button(Assets.buttonS1, Assets.buttonS2, startX, baseY,
                "CONFIRMAR", new Action() {
            @Override
            public void doAction() {
                Assets.buttonSelected.play();
                ShipLibrary.setSelectedShip(selectedShipIndex);
                ShipLibrary.setSelectedLaser(selectedLaserIndex);

                State.changeState(new NameInputState());
            }
        }));

        // Boton "VOLVER"
        buttons.add(new Button(Assets.buttonS1, Assets.buttonS2, startX + spacing, baseY,
                "VOLVER", new Action() {
            @Override
            public void doAction() {
                Assets.buttonSelected.play();
                State.changeState(new MenuState());
            }
        }));
    }

    @Override
    public void update() {

        menuBackground.update();

        // --- INICIO DE LA MODIFICACION ---
        boltPreviewAnimation.update(); // Actualizar la animacion bolt
        sparkPreviewAnimation.update(); // Actualizar la animacion spark
        pulsePreviewAnimation.update(); // Actualizar la animacion pulse
        // --- FIN DE LA MODIFICACION ---

        for (Button b : buttons) {
            b.update();
        }

        // Navegar entre naves con A/D
        if (KeyBoard.A && !leftPressedLastFrame) {
            selectedShipIndex--;
            if (selectedShipIndex < 0) selectedShipIndex = ShipLibrary.getShips().size() - 1;
            leftPressedLastFrame = true;
        } else if (!KeyBoard.A) {
            leftPressedLastFrame = false;
        }

        if (KeyBoard.D && !rightPressedLastFrame) {
            selectedShipIndex++;
            if (selectedShipIndex >= ShipLibrary.getShips().size()) selectedShipIndex = 0;
            rightPressedLastFrame = true;
        } else if (!KeyBoard.D) {
            rightPressedLastFrame = false;
        }

        // --- INICIO DE LA MODIFICACION ---
        // Navegar entre laseres con W/S (ahora hasta 4)
        if (KeyBoard.W && !upPressedLastFrame) {
            selectedLaserIndex--;
            if (selectedLaserIndex < 0) selectedLaserIndex = 4; // <-- MODIFICADO (era 3)
            upPressedLastFrame = true;
        } else if (!KeyBoard.W) {
            upPressedLastFrame = false;
        }

        if (KeyBoard.S && !downPressedLastFrame) {
            selectedLaserIndex++;
            if (selectedLaserIndex > 4) selectedLaserIndex = 0; // <-- MODIFICADO (era 3)
            downPressedLastFrame = true;
        } else if (!KeyBoard.S) {
            downPressedLastFrame = false;
        }
        // --- FIN DE LA MODIFICACION ---
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        menuBackground.draw(g);

        int shipPreviewWidth = 110;
        int shipPreviewHeight = 100;

        int shipX = Constants.WIDTH / 2 - shipPreviewWidth / 2 + shipOffsetX;
        int shipY = 170 + shipOffsetY;


        g.setColor(Color.WHITE);

        g.setFont(Assets.fontBig);
        g.drawString("SELECCIONA TU NAVE", Constants.WIDTH / 2 - 200 + textOffsetX, 80 + textOffsetY);

        g.setFont(Assets.fontMed);
        g.drawString("NAVE: " + (selectedShipIndex + 1), Constants.WIDTH / 2 - 50, 200);

        ShipData currentShip = ShipLibrary.getShip(selectedShipIndex);
        BufferedImage shipImage = currentShip.getTexture();
        g2d.drawImage(shipImage, shipX, shipY, shipPreviewWidth, shipPreviewHeight, null);

        // Mostrar tipo de laser
        g.setFont(Assets.fontMed);
        g.drawString("LASER: " + (selectedLaserIndex + 1), Constants.WIDTH / 2 - 60, 400);

        // Coordenadas base (centrado en pantalla)
        int laserBaseX = Constants.WIDTH / 2 - laserPreviewWidth / 2;
        int laserBaseY = 400;
        int laserX = laserBaseX + laserOffsetX;
        int laserY = laserBaseY + laserOffsetY;

        // Dibujar el laser segun seleccion actual
        BufferedImage laserImage; // Para los casos estaticos

        switch (selectedLaserIndex) {
            case 0:
                laserImage = Assets.laserPersonalizado1;
                g2d.drawImage(laserImage, laserX, laserY, laserPreviewWidth, laserPreviewHeight, null);
                break;
            case 1:
                laserImage = Assets.laserPersonalizado2;
                g2d.drawImage(laserImage, laserX, laserY, laserPreviewWidth, laserPreviewHeight, null);
                break;
            case 2:
                // Dibujar el frame actual de la animacion bolt
                BufferedImage frameBolt = boltPreviewAnimation.getCurrentFrame();
                g2d.drawImage(frameBolt, laserX, laserY, laserPreviewWidth, laserPreviewHeight, null);
                break;
            case 3:
                // Dibujar el frame actual de la animacion spark
                BufferedImage frameSpark = sparkPreviewAnimation.getCurrentFrame();
                g2d.drawImage(frameSpark, laserX, laserY, laserPreviewWidth, laserPreviewHeight, null);
                break;
            case 4:
                // Dibujar el frame actual de la animacion pulse
                BufferedImage framePulse = pulsePreviewAnimation.getCurrentFrame();
                g2d.drawImage(framePulse, laserX, laserY, laserPreviewWidth, laserPreviewHeight, null);
                break;
            default:
                laserImage = Assets.laserPersonalizado1;
                g2d.drawImage(laserImage, laserX, laserY, laserPreviewWidth, laserPreviewHeight, null);
                break;
        }

        // Botones
        for (Button b : buttons) {
            b.draw(g);
        }
    }

}