package states;

import gameObject.ShipLibrary;
import gameObject.ShipData;
import graphics.Assets;
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

    // Variables para ajustar posiciones de texto e imagen y laserPreview
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

        buttons = new ArrayList<>();

        int buttonWidth = Assets.buttonS1.getWidth();
        int buttonHeight = Assets.buttonS1.getHeight();

        // Posicion base de botones
        int baseY = Constants.HEIGHT - 150;
        int startX = Constants.WIDTH / 2 - 320;
        int spacing = 350; // separacion de botones

        // Boton “CONFIRMAR”
        buttons.add(new Button(Assets.buttonS1, Assets.buttonS2, startX, baseY,
                "CONFIRMAR", new Action() {
            @Override
            public void doAction() {
                Assets.buttonSelected.play();
                ShipLibrary.setSelectedShip(selectedShipIndex);
                ShipLibrary.setSelectedLaser(selectedLaserIndex);
                State.changeState(new GameState());
            }
        }));

        // Boton “VOLVER”
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

        // Navegar entre laseres con W/S
        if (KeyBoard.W && !upPressedLastFrame) {
            selectedLaserIndex--;
            if (selectedLaserIndex < 0) selectedLaserIndex = 1; // ajusta si agregas mas tipos de laser
            upPressedLastFrame = true;
        } else if (!KeyBoard.W) {
            upPressedLastFrame = false;
        }

        if (KeyBoard.S && !downPressedLastFrame) {
            selectedLaserIndex++;
            if (selectedLaserIndex > 1) selectedLaserIndex = 0; // ajusta si agregas más tipos de laser
            downPressedLastFrame = true;
        } else if (!KeyBoard.S) {
            downPressedLastFrame = false;
        }
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        int shipPreviewWidth = 110;   // ancho deseado de la nave (ajustalo a gusto)
        int shipPreviewHeight = 100;  // alto deseado

        int shipX = Constants.WIDTH / 2 - shipPreviewWidth / 2 + shipOffsetX;
        int shipY = 170 + shipOffsetY;


        g.setColor(Color.WHITE);

        // Texto "SELECCIONA TU NAVE" con offsets
        g.setFont(Assets.fontBig);
        g.drawString("SELECCIONA TU NAVE", Constants.WIDTH / 2 - 200 + textOffsetX, 80 + textOffsetY);

        g.setFont(Assets.fontMed);
        g.drawString("NAVE: " + (selectedShipIndex + 1), Constants.WIDTH / 2 - 50, 200);

        // Nave seleccionada con offsets
        ShipData currentShip = ShipLibrary.getShip(selectedShipIndex);
        BufferedImage shipImage = currentShip.getTexture();
        g2d.drawImage(shipImage, shipX, shipY, shipPreviewWidth, shipPreviewHeight, null);

        // Dibujar el laser segun seleccion actual
        BufferedImage laserImage;
        switch (selectedLaserIndex) {
            case 0:
                laserImage = Assets.laserPersonalizado1;
                break;
            case 1:
                laserImage = Assets.laserPersonalizado2;
                break;
            default:
                laserImage = Assets.laserPersonalizado1;
                break;
        }

        // Mostrar tipo de laser
        g.setFont(Assets.fontMed);
        g.drawString("LASER: " + (selectedLaserIndex + 1), Constants.WIDTH / 2 - 60, 400);

        // Coordenadas base (centrado en pantalla)
        int laserBaseX = Constants.WIDTH / 2 - laserPreviewWidth / 2;
        int laserBaseY = 400; // posicion base vertical

        // Aplicar offsets manuales
        int laserX = laserBaseX + laserOffsetX;
        int laserY = laserBaseY + laserOffsetY;

        // Dibujar preview del laser
        g2d.drawImage(
                laserImage,
                laserX,
                laserY,
                laserPreviewWidth,
                laserPreviewHeight,
                null
        );

        // Botones
        for (Button b : buttons) {
            b.draw(g);
        }
    }

}