package ui;
import graphics.Assets;
import graphics.Sound;
import graphics.Text;
import input.MouseInput;
import math.Vector2D;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Button {

    private BufferedImage mouseOutImg;
    private BufferedImage mouseInImg;
    private boolean mouseIn;
    private Rectangle boundingBox;
    private String text;

    private Action action;

    private Sound hoverSound = new Sound("/sounds/HoverBSound.wav");
    private boolean wasMouseIn = false;

    public Button(BufferedImage mouseOutImg, BufferedImage mouseInImg, int x, int y, String text, Action action){
        this.mouseInImg = mouseInImg;
        this.mouseOutImg = mouseOutImg;
        this.text = text;
        // Crea el rectángulo de colisión/posición con el tamaño de la imagen
        boundingBox = new Rectangle(x, y, mouseInImg.getWidth(), mouseInImg.getHeight());
        this.action = action;
    }

    public void update(){

        // --- Obtener posición actual del mouse ---
        Point mouse = MouseInput.getMousePosition();

        // --- Lógica de detección de hover ---
        if (boundingBox.contains(mouse)) {
            mouseIn = true;

            if (!wasMouseIn) {
                wasMouseIn = true;
                hoverSound.play();
            }

        } else {
            mouseIn = false;
            wasMouseIn = false;
        }

        // --- Acciones de botón (click) ---
        if (mouseIn && MouseInput.isPressed()) {
            action.doAction();
            MouseInput.releaseClick(); // <--- evita múltiples ejecuciones por un solo click
        }

    }

    public void draw(Graphics g){

        // Dibuja el botón (imagen)
        if(mouseIn){
            g.drawImage(mouseInImg, boundingBox.x, boundingBox.y, null);
        } else {
            g.drawImage(mouseOutImg, boundingBox.x, boundingBox.y, null);
        }

        // --- LÓGICA DE CENTRADO DE TEXTO ---

        // 1. Calcular la posición central absoluta del botón
        Vector2D textPos = new Vector2D(
                boundingBox.getX() + boundingBox.getWidth() / 2,
                boundingBox.getY() + boundingBox.getHeight() / 2
        );

        // 2. Dibujar el texto centrado
        Text.drawText((Graphics2D) g, text, textPos, true, Color.BLACK, Assets.fontMed);

    }

}