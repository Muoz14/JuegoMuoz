package gameObject;

import graphics.Text;
import math.Vector2D;
import states.GameState;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class Message {

    private GameState gameState;
    private float alpha;
    private String text;
    private Vector2D position;
    private Color color;
    private boolean center;
    private boolean fade;
    private Font font;
    private final float deltaAlpha = 0.01f;
    private long creationTime;
    private long lifespan = -1; // -1 = no caduca

    public Message(Vector2D position, boolean fade, String text, Color color, boolean center,
                   Font font, GameState gameState){
        this.font = font;
        this.gameState = gameState;
        this.text = text;
        this.position = position;
        this.fade = fade;
        this.color = color;
        this.center = center;
        this.alpha = fade ? 1f : 0f;
        this.creationTime = System.currentTimeMillis();
    }

    // Define duración en ms
    public void setLifespan(long milliseconds) {
        this.lifespan = milliseconds;
    }

    public boolean isExpired(long currentTime) {
        if (lifespan < 0) return false;
        return (currentTime - creationTime) >= lifespan;
    }

    public void update() {
        if (fade) alpha -= deltaAlpha;
        else alpha += deltaAlpha;

        if ((fade && alpha < 0) || (!fade && alpha > 1)) {
            gameState.removeMessage(this);
        }

        // Mensaje sube lentamente
        position.setY(position.getY() - 0.5);
    }

    public void draw(Graphics2D g2d){
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        Text.drawText(g2d, text, position, center, color, font);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }
}
