package gameObject;

import graphics.Assets;
import graphics.Text;
import math.Vector2D;
import states.GameState;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

public class FinalBoss extends MovingObject {

    private BossPhase currentPhase;
    private int hitsTaken;
    private final int MAX_HITS;
    private boolean isIndestructible = false;

    // Temporizadores para ataques y spawns
    private Chronometer laserTimer;
    private Chronometer specialTimer;
    private Chronometer minionTimer;

    // Temporizadores para movimiento y efectos
    private Chronometer stateTimer;
    private boolean flicker = false;

    // Posición Fase 1: El borde inferior del jefe estará en Y=280.
    private final double Y_POS_PHASE_1 = -515;

    // Posición Fase 2: El borde superior del jefe estará en Y=440.
    private final double Y_POS_PHASE_2 = 440;


    public FinalBoss(Vector2D position, GameState gameState) {
        super(position, new Vector2D(), Constants.BOSS_CHARGE_SPEED, Assets.finalBoss, gameState);
        this.gameState = gameState;
        this.MAX_HITS = Constants.FINAL_BOSS_HEALTH;
        this.hitsTaken = 0;
        this.immuneToMeteors = true;

        this.currentPhase = BossPhase.ENTERING;
        this.velocity = new Vector2D(0, Constants.BOSS_ENTER_SPEED);

        this.laserTimer = new Chronometer();
        this.specialTimer = new Chronometer();
        this.minionTimer = new Chronometer();
        this.stateTimer = new Chronometer();

        laserTimer.run(3000);
        minionTimer.run(5000);
    }

    /**
     * Devuelve si el jefe está en una fase indestructible.
     */
    public boolean isIndestructible() {
        return isIndestructible;
    }

    /**
     * Sobrescribe getBounds() para devolver solo la parte visible del jefe.
     */
    @Override
    public Rectangle getBounds() {
        int y_start = (int)Math.max(0, position.getY());
        int y_end = (int)Math.min(Constants.HEIGHT, position.getY() + height);

        int h = y_end - y_start;
        if (h < 0) h = 0;

        int x = (int) position.getX();
        int w = width;

        if (y_start < 40) {
            h = h - (40 - y_start);
            y_start = 40;
            if (h < 0) h = 0;
        }

        return new Rectangle(x, y_start, w, h);
    }

    public BossPhase getPhase() {
        return currentPhase;
    }

    @Override
    public void update() {
        if (currentPhase == BossPhase.DEFEATED) return;

        laserTimer.update();
        specialTimer.update();
        minionTimer.update();
        stateTimer.update();

        updatePhase();
        position = position.add(velocity);
        collidesWithPlayerLasers();
    }

    /**
     * Lógica principal de la IA del jefe.
     */
    private void updatePhase() {
        switch (currentPhase) {

            case ENTERING:
                // Moverse a la posición superior (Y = -515)
                if (position.getY() >= Y_POS_PHASE_1) {
                    position.setY(Y_POS_PHASE_1);
                    velocity = new Vector2D();
                    currentPhase = BossPhase.PHASE_1;
                    laserTimer.run(2000);
                    minionTimer.run(5000);
                    specialTimer.run(8000);
                }
                break;

            case PHASE_1:
                attackPattern1();
                attackSpecial();
                spawnMinions();
                if (hitsTaken >= MAX_HITS / 2) {
                    startTransition();
                }
                break;

            case TRANSITION_1:
                if (position.getY() >= (Constants.HEIGHT / 2.0 - height / 2.0)) {
                    velocity = new Vector2D();
                    currentPhase = BossPhase.TRANSITION_2;
                    stateTimer.run(500);
                }
                break;

            case TRANSITION_2:
                if (stateTimer.isFinished()) {
                    if (velocity.getY() == 0) velocity = new Vector2D(0, -Constants.BOSS_CHARGE_SPEED);

                    if (position.getY() < -height) {
                        currentPhase = BossPhase.TRANSITION_3;
                        position.setY(Constants.HEIGHT);
                        velocity = new Vector2D(0, -Constants.BOSS_CHARGE_SPEED);
                    }
                }
                break;

            case TRANSITION_3:
                if (position.getY() <= Y_POS_PHASE_2) {
                    position.setY(Y_POS_PHASE_2);
                    velocity = new Vector2D();
                    isIndestructible = false;
                    currentPhase = BossPhase.PHASE_2;
                    laserTimer.run(1000);
                    minionTimer.run(3000);
                    specialTimer.run(10000);
                }
                break;

            case PHASE_2:
                attackPattern1();
                attackSpecial();
                spawnMinions();
                if (hitsTaken >= MAX_HITS) {
                    startDying();
                }
                break;

            case DYING:
                if (stateTimer.isFinished()) {
                    flicker = !flicker;
                    stateTimer.run(100);
                }
                break;
            case DEFEATED:
                break;
        }
    }

    // --- Lógica de Transición y Muerte ---

    private void startTransition() {
        currentPhase = BossPhase.TRANSITION_1;
        isIndestructible = true;
        velocity = new Vector2D(0, Constants.BOSS_CHARGE_SPEED);
        for (MovingObject m : gameState.getMovingObjects()) {
            if (m instanceof Minion) m.Destroy();
        }
    }

    private void startDying() {
        currentPhase = BossPhase.DYING;
        isIndestructible = true;
        velocity = new Vector2D();

        for (MovingObject m : gameState.getMovingObjects()) {
            if (m instanceof Minion || m instanceof Ufo) m.Destroy();
        }

        new Thread(() -> {
            for(int i = 0; i < 30; i++) {
                Vector2D pos = getCenter().add(
                        new Vector2D(Math.random() * width - width / 2, Math.random() * height - height / 2)
                );
                gameState.playExplosion(pos);
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            }
            Destroy();
        }).start();

        stateTimer.run(100);
    }

    @Override
    protected void Destroy() {
        // Esta es la destrucción final
        currentPhase = BossPhase.DEFEATED;
        gameState.onFinalBossDefeated();

        Message winMsg = new Message(
                new Vector2D(Constants.WIDTH / 2, Constants.HEIGHT / 2),
                false, "VENCISTE AL JEFE", Color.CYAN, true, Assets.fontBig, gameState
        );
        winMsg.setLifespan(5000);
        gameState.addMessage(winMsg);

        gameState.getPlayer().activateAllPowerUps(90000);

        super.Destroy();
    }

    // --- Lógica de Ataques y Spawns ---

    private void spawnMinions() {
        if (minionTimer.isFinished()) {
            int currentMinions = 0;
            for (MovingObject m : gameState.getMovingObjects()) {
                if (m instanceof Minion) currentMinions++;
            }

            if (currentMinions < 2) {
                if (currentPhase == BossPhase.PHASE_2) {
                    gameState.addObject(new Minion(
                            new Vector2D(position.getX() + width / 4, position.getY() - 100), gameState
                    ));
                    gameState.addObject(new Minion(
                            new Vector2D(position.getX() + (3 * width / 4), position.getY() - 100), gameState
                    ));
                } else {
                    gameState.addObject(new Minion(
                            getCenter().add(new Vector2D(-width/4, 0)), gameState
                    ));
                    gameState.addObject(new Minion(
                            getCenter().add(new Vector2D(width/4, 0)), gameState
                    ));
                }
            }

            minionTimer.run(15000);
        }
    }

    private void attackPattern1() {
        if (laserTimer.isFinished()) {
            Player p = gameState.getPlayer();
            Vector2D playerPos = (p != null) ? p.getCenter() : new Vector2D(Constants.WIDTH/2, Constants.HEIGHT/2);

            if (currentPhase == BossPhase.PHASE_2) {
                // FASE 2: Disparar hacia ARRIBA
                // Spawnea 1px *fuera* de la hitbox (Y=440), por lo que usamos Y=439
                double topY = position.getY() - 1;

                shootLaser(new Vector2D(position.getX() + width/4, topY), new Vector2D(0, -1));
                shootLaser(new Vector2D(position.getX() + 3*width/4, topY), new Vector2D(0, -1));

                Vector2D dir1 = playerPos.subtract(new Vector2D(position.getX(), topY)).normalize();
                Vector2D dir2 = playerPos.subtract(new Vector2D(position.getX() + width, topY)).normalize();
                shootLaser(new Vector2D(position.getX(), topY), dir1);
                shootLaser(new Vector2D(position.getX() + width, topY), dir2);

            } else {
                // FASE 1: Disparar hacia ABAJO
                // Spawnea 1px *fuera* de la hitbox (Y=280), por lo que usamos Y=281
                double bottomY = position.getY() + height + 1;

                shootLaser(new Vector2D(position.getX() + width / 4, bottomY), new Vector2D(0, 1));
                shootLaser(new Vector2D(position.getX() + 3 * width / 4, bottomY), new Vector2D(0, 1));

                Vector2D dir1 = playerPos.subtract(new Vector2D(position.getX() + width / 4, bottomY)).normalize();
                Vector2D dir2 = playerPos.subtract(new Vector2D(position.getX() + 3 * width / 4, bottomY)).normalize();
                shootLaser(new Vector2D(position.getX() + width / 4, bottomY), dir1);
                shootLaser(new Vector2D(position.getX() + 3 * width / 4, bottomY), dir2);
                // --- FIN DE LA CORRECCIÓN ---
            }

            laserTimer.run(3000);
        }
    }

    private void attackSpecial() {
        if (specialTimer.isFinished()) {

            // --- INICIO DE LA MODIFICACIÓN (Ataque especial Izquierda/Derecha) ---
            boolean attackLeftHalf = Math.random() > 0.5; // Ataca mitad izquierda o derecha
            double startAngle;
            double endAngle;
            Vector2D origin;

            if (currentPhase == BossPhase.PHASE_2) {
                // FASE 2: Jefe está ABAJO, jugador está ARRIBA.
                // Origen: Borde superior central
                origin = new Vector2D(position.getX() + width / 2, position.getY() - 1); // -1 para estar fuera

                if (attackLeftHalf) {
                    // Mitad superior-izquierda (180 a 270 grados)
                    startAngle = Math.PI;       // 180°
                    endAngle = Math.PI * 1.5;   // 270°
                } else {
                    // Mitad superior-derecha (270 a 360 grados)
                    startAngle = Math.PI * 1.5;   // 270°
                    endAngle = Math.PI * 2;     // 360°
                }
            } else {
                // FASE 1: Jefe está ARRIBA, jugador está ABAJO.
                // Origen: Borde inferior central
                origin = new Vector2D(position.getX() + width / 2, position.getY() + height + 1); // +1 para estar fuera

                if (attackLeftHalf) {
                    // Mitad inferior-izquierda (90 a 180 grados)
                    startAngle = Math.PI / 2; // 90°
                    endAngle = Math.PI;       // 180°
                } else {
                    // Mitad inferior-derecha (0 a 90 grados)
                    startAngle = 0;           // 0°
                    endAngle = Math.PI / 2; // 90°
                }
            }

            // Disparar 20 láseres en un arco de 90° (media pantalla)
            int laserCount = 20;

            for (int i = 0; i <= laserCount; i++) {
                double t = (double) i / laserCount;
                double angle = startAngle + t * (endAngle - startAngle);

                Vector2D heading = new Vector2D(Math.cos(angle), Math.sin(angle));
                shootLaser(origin, heading);
            }

            specialTimer.run(15000);
        }
    }

    private void shootLaser(Vector2D origin, Vector2D heading) {
        double angle = Math.atan2(heading.getY(), heading.getX()) + Math.PI / 2;
        Laser laser = new Laser(
                origin, heading, Constants.LASER_VEL, angle,
                Assets.laserPersonalizado1,
                gameState, false
        );
        gameState.addObject(laser);
        Assets.ufoShoot.play();
    }

    // --- Colisiones ---

    private void collidesWithPlayerLasers() {
        if (isIndestructible) return;

        ArrayList<MovingObject> objects = gameState.getMovingObjects();
        for (int i = objects.size() - 1; i >= 0; i--) {
            MovingObject m = objects.get(i);

            if (m instanceof Laser && ((Laser)m).isPlayerLaser()) {

                Laser laser = (Laser) m;
                if (laser.hitbox.intersects(getBounds())) {

                    m.Destroy();
                    hitsTaken++;
                    gameState.playExplosion(m.getCenter());

                    if (hitsTaken >= MAX_HITS && currentPhase != BossPhase.DYING) {
                        startDying();
                    }
                }
            }
        }
    }

    public void pauseTimers() {
        laserTimer.pause();
        specialTimer.pause();
        minionTimer.pause();
        stateTimer.pause();
    }

    public void resumeTimers() {
        laserTimer.resume();
        specialTimer.resume();
        minionTimer.resume();
        stateTimer.resume();
    }

    @Override
    public void draw(Graphics g) {
        if (currentPhase == BossPhase.DEFEATED) return;

        if (currentPhase == BossPhase.DYING && flicker) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        AffineTransform at = AffineTransform.getTranslateInstance(position.getX(), position.getY());
        g2d.drawImage(texture, at, null);

        drawHealthBar(g);
    }

    private void drawHealthBar(Graphics g) {
        double lifePercent = (double) (MAX_HITS - hitsTaken) / MAX_HITS;
        int barWidth = 400;
        int barHeight = 20;
        int x = (Constants.WIDTH - barWidth) / 2;
        int y = 20;

        g.setColor(Color.RED);
        g.fillRect(x, y, barWidth, barHeight);

        g.setColor(Color.GREEN);
        g.fillRect(x, y, (int) (barWidth * lifePercent), barHeight);

        g.setColor(Color.WHITE);
        g.drawRect(x, y, barWidth, barHeight);

        g.setFont(Assets.fontMed);
        Text.drawText((Graphics2D)g, "JEFE", new Vector2D(x + barWidth / 2, y - 5), true, Color.WHITE, Assets.fontMed);
    }
}