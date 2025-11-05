package gameObject;

import graphics.Animation;
import graphics.Assets;
import math.Vector2D;
import states.GameState;

import java.awt.*;

public class Explosion extends MovingObject {

    protected Animation animation;

    public Explosion(Vector2D position, GameState gameState) {
        super(position, new Vector2D(), 0, Assets.exp[0], gameState);
        this.animation = new Animation(Assets.exp, 110, position.subtract(new Vector2D(Assets.exp[0].getWidth() / 2, Assets.exp[0].getHeight() / 2)));
    }

    @Override
    public void update() {
        animation.update();
        if (!animation.isRunning()) {
            Destroy();
        }
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(animation.getCurrentFrame(), (int) animation.getPosition().getX(), (int) animation.getPosition().getY(), null);
    }
}
