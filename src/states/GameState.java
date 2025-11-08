package states;

import gameObject.*;
import graphics.Animation;
import graphics.Assets;
import graphics.Sound;
import graphics.SoundManager;
import input.KeyBoard;
import input.MouseInput;
import math.Vector2D;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class GameState extends State {

    private Player player;
    private ArrayList<MovingObject> movingObjects = new ArrayList<>();
    private ArrayList<MovingObject> objectsToAdd = new ArrayList<>();
    private ArrayList<MovingObject> objectsToRemove = new ArrayList<>();
    private ArrayList<Animation> explosion = new ArrayList<>();
    private ArrayList<Message> messages = new ArrayList<>();
    private ArrayList<Message> messagesToRemove = new ArrayList<>();

    public int meteors;
    private int waves = 1;
    private int lives = 100;

    // Puntuaciones
    private int score = 0;

    // Control de oleadas
    private boolean waveCleared = false;
    private long waveClearTime = 0;
    private boolean nextWaveStarting = false;

    private boolean paused = false;
    private boolean startingCountdown = true;
    private long countdownStartTime;
    private int countdownValue = 3;
    private boolean firstWaveStarted = false;
    private boolean escPressedLastFrame = false;

    // Aparicion del UFO
    private long lastUfoSpawnTime = 0;
    private static final long UFO_SPAWN_INTERVAL = 15000;

    // Boton pausa
    private BufferedImage pauseButtonImg;
    private Rectangle pauseButtonBounds;
    private int pauseButtonOffsetX = 20;
    private int pauseButtonOffsetY = 20;

    // Menu de pausa
    private Rectangle resumeButtonBounds;
    private Rectangle settingsButtonBounds;
    private Rectangle menuButtonBounds;
    private BufferedImage resumeButtonImg;
    private BufferedImage settingsButtonImg;
    private BufferedImage menuButtonImg;
    private boolean showPauseMenu = false;
    private float pauseOverlayAlpha = 0.5f;

    // Botones hover
    private BufferedImage resumeButtonHoverImg;
    private BufferedImage settingsButtonHoverImg;
    private BufferedImage menuButtonHoverImg;

    public GameState() {

        ShipData playerData = ShipLibrary.getSelectedShip();
        BufferedImage laserImg = ShipLibrary.getSelectedLaserImage();

        player = new Player(new Vector2D(560, 320), new Vector2D(), this, playerData, laserImg);
        movingObjects.add(player);

        meteors = 1;

        // Boton pausa
        pauseButtonImg = Assets.buttonPause;
        int buttonWidth = pauseButtonImg.getWidth();
        int buttonHeight = pauseButtonImg.getHeight();
        pauseButtonBounds = new Rectangle(pauseButtonOffsetX, pauseButtonOffsetY, buttonWidth, buttonHeight);

        // Botones menu de pausa
        resumeButtonImg = Assets.buttonS1;
        settingsButtonImg = Assets.buttonS1;
        menuButtonImg = Assets.buttonS1;

        // Hover
        resumeButtonHoverImg = Assets.buttonS2;
        settingsButtonHoverImg = Assets.buttonS2;
        menuButtonHoverImg = Assets.buttonS2;

        resumeButtonBounds = new Rectangle(Constants.WIDTH / 2 - 180, Constants.HEIGHT / 2 - 100, 360, 60);
        settingsButtonBounds = new Rectangle(Constants.WIDTH / 2 - 180, Constants.HEIGHT / 2, 360, 60);
        menuButtonBounds = new Rectangle(Constants.WIDTH / 2 - 180, Constants.HEIGHT / 2 + 100, 360, 60);

        startCountdown(); // <--- Metodo de countdown

        // Musica de fondo unica
        // Detener musica anterior si existia
        if (Assets.backgroundMusic != null) {
            Assets.backgroundMusic.stop();
        }

        // (El SoundManager ya deberia tener registrado 'backgroundMusic' desde Assets.init())
        // (Pero re-ajustamos el volumen por si acaso)
        Assets.backgroundMusic.setVolume(SettingsData.getVolume());
        Assets.backgroundMusic.play();
    }

    // ------------------ Countdown ------------------
    public void startCountdown() {
        startingCountdown = true;
        countdownStartTime = System.currentTimeMillis();
        countdownValue = 3;
    }

    private void handleCountdown() {
        long elapsed = System.currentTimeMillis() - countdownStartTime;
        if (elapsed >= 1000) {
            countdownValue--;
            countdownStartTime = System.currentTimeMillis();
        }

        if (countdownValue <= 0) {
            startingCountdown = false;
            if (!firstWaveStarted) {
                startWave();
                firstWaveStarted = true;
            }
        }
    }

    private void startWave() {
        Message waveMessage = new Message(
                new Vector2D(Constants.WIDTH / 2, Constants.HEIGHT / 2),
                false,
                "OLEADA " + waves,
                Color.WHITE,
                true,
                Assets.fontBig,
                this
        );
        waveMessage.setLifespan(3500);
        addMessage(waveMessage);

        for (int i = 0; i < meteors; i++) {
            double x = i % 2 == 0 ? Math.random() * Constants.WIDTH : 0;
            double y = i % 2 == 0 ? 0 : Math.random() * Constants.HEIGHT;
            Vector2D velocity = new Vector2D(Math.random() * 2 - 1, Math.random() * 2 - 1).normalize().scale(2);
            int family = (int) (Math.random() * 3);
            Meteor meteor = new Meteor(
                    new Vector2D(x, y),
                    velocity,
                    2,
                    Assets.bigs[family],
                    this,
                    Size.BIG,
                    family
            );
            addObject(meteor);
        }
    }
    public void playExplosion(Vector2D position) {
        explosion.add(new Animation(
                Assets.exp,
                110,
                position.subtract(new Vector2D(
                        Assets.exp[0].getWidth() / 2,
                        Assets.exp[0].getHeight() / 2))
        ));
    }

    private void spawnUfo() {
        long now = System.currentTimeMillis();
        if (now - lastUfoSpawnTime < UFO_SPAWN_INTERVAL) return;

        lastUfoSpawnTime = now;

        double x = 0, y = 0;
        int side = (int) (Math.random() * 4);
        switch (side) {
            case 0:
                x = Math.random() * Constants.WIDTH;
                y = 0;
                break;
            case 1:
                x = Math.random() * Constants.WIDTH;
                y = Constants.HEIGHT;
                break;
            case 2:
                x = 0;
                y = Math.random() * Constants.HEIGHT;
                break;
            case 3:
                x = Constants.WIDTH;
                y = Math.random() * Constants.HEIGHT;
                break;
        }

        ArrayList<Vector2D> path = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            double posX = Math.random() * Constants.WIDTH;
            double posY = Math.random() * Constants.HEIGHT;
            path.add(new Vector2D(posX, posY));
        }

        Ufo ufo = new Ufo(
                new Vector2D(x, y),
                new Vector2D(),
                Constants.UFO_MAX_VEL,
                Assets.ufo,
                path,
                this
        );
        addObject(ufo);
    }

    @Override
    public void update() {
        KeyBoard.update();
        MouseInput.update();
        Point mouse = MouseInput.getMousePosition();

        // Actualizar explosiones
        for (int i = explosion.size() - 1; i >= 0; i--) {
            Animation anim = explosion.get(i);
            anim.update();
            if (!anim.isRunning()) explosion.remove(i);
        }

        // Pausa con boton
        if (pauseButtonBounds.contains(mouse) && MouseInput.isPressed()) {
            Assets.buttonSelected.play();
            paused = true;
            showPauseMenu = true;
            MouseInput.releaseClick();
            pauseMusic();
            SoundManager.getInstance().pauseAll();
        }

        // Pausa con ESC
        boolean escNow = KeyBoard.isKeyPressed(KeyEvent.VK_ESCAPE);
        if (escNow && !escPressedLastFrame) {
            paused = !paused;
            showPauseMenu = paused;

            if (paused) {
                pauseMusic();
                SoundManager.getInstance().pauseAll();
            } else {
                startCountdown();
                resumeMusic();
            }
        }
        escPressedLastFrame = escNow;

        if (paused && showPauseMenu) {
            handlePauseMenu(mouse);
            KeyBoard.endFrame();
            return;
        }

        if (startingCountdown) {
            handleCountdown();
            return;
        }

        // ------------------ Actualizar objetos ------------------
        for (int i = 0; i < movingObjects.size(); i++) {
            movingObjects.get(i).update();
        }

        // Reaparicion del player
        if (player.isDead() && !player.isSpawning()) {
            player.startRespawn();
        }
        if (player.isSpawning()) {
            player.updateSpawnTimer();
        }

        // Mensajes
        long currentTime = System.currentTimeMillis();
        for (Message msg : messages) {
            msg.update();
            if (msg.isExpired(currentTime)) messagesToRemove.add(msg);
        }

        // Aplicar anadidos y removidos
        if (!objectsToAdd.isEmpty()) {
            movingObjects.addAll(objectsToAdd);
            objectsToAdd.clear();
        }

        if (!objectsToRemove.isEmpty()) {
            movingObjects.removeAll(objectsToRemove);
            objectsToRemove.clear();
        }

        if (!messagesToRemove.isEmpty()) {
            messages.removeAll(messagesToRemove);
            messagesToRemove.clear();
        }

        spawnUfo();
        handleWaveLogic();
    }

    private void handlePauseMenu(Point mouse) {
        if (resumeButtonBounds.contains(mouse) && MouseInput.isPressed()) {
            paused = false;
            showPauseMenu = false;
            Assets.buttonSelected.play();
            startCountdown();
            resumeMusic();
            SoundManager.getInstance().resumeAll();
            MouseInput.releaseClick();
        }

        if (settingsButtonBounds.contains(mouse) && MouseInput.isPressed()) {
            Assets.buttonSelected.play();
            State.changeState(new SettingsState(this));
            MouseInput.releaseClick();
        }

        if (menuButtonBounds.contains(mouse) && MouseInput.isPressed()) {
            Assets.buttonSelected.play();
            stopMusic();
            State.changeState(new MenuState());
            MouseInput.releaseClick();
        }
    }

    private void handleWaveLogic() {
        boolean hayMeteoros = false;
        boolean hayMiniBoss = false; // Nuevo chequeo

        // 1. Contar enemigos
        for (MovingObject obj : movingObjects) {
            if (obj instanceof Meteor) {
                hayMeteoros = true;
            }
            if (obj instanceof MiniBoss) {
                hayMiniBoss = true;
            }
        }

        // 2. Revisar si la oleada normal termino
        if (!hayMeteoros && !hayMiniBoss && firstWaveStarted && !waveCleared && !nextWaveStarting) {
            waveCleared = true;
            waveClearTime = System.currentTimeMillis();

            Message completeMsg = new Message(
                    new Vector2D(Constants.WIDTH / 2, Constants.HEIGHT / 2),
                    false,
                    "OLEADA COMPLETADA!",
                    Color.WHITE,
                    true,
                    Assets.fontBig,
                    this
            );
            completeMsg.setLifespan(3000);
            addMessage(completeMsg);
        }
        // 3.Meteoros despejados, PERO el boss SIGUE VIVO
        else if (!hayMeteoros && hayMiniBoss && firstWaveStarted && !waveCleared && !nextWaveStarting) {
            nextWaveStarting = true;
            spawnMeteorSubWave(waves);
        }


        // 4. Este bloque maneja la transicion a la SIGUIENTE oleada
        if (waveCleared && !nextWaveStarting) {
            long elapsed = System.currentTimeMillis() - waveClearTime;
            if (elapsed >= 3000) {
                waveCleared = false;
                nextWaveStarting = true;
                waves++;
                meteors++;

                Message waveMessage = new Message(
                        new Vector2D(Constants.WIDTH / 2, Constants.HEIGHT / 2),
                        false,
                        "OLEADA " + waves + "!",
                        Color.WHITE,
                        true,
                        Assets.fontBig,
                        this
                );
                waveMessage.setLifespan(3000);
                addMessage(waveMessage);

                new Thread(() -> {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ignored) {}
                    startWave();
                    nextWaveStarting = false;

                    // Aparicion del MiniBoss cada 3 oleadas
                    if (waves % 3 == 0) {
                        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

                        ShipData selectedShip = ShipLibrary.getSelectedShip();
                        int numCannons = selectedShip.getGunOffsets().size();

                        int bossHealth;
                        if (numCannons >= 2) {
                            bossHealth = 30;
                        } else {
                            bossHealth = 12; // Modificado de 10 a 12
                        }

                        Vector2D bossPos = new Vector2D(Constants.WIDTH / 2 - 100, 100);
                        MiniBoss boss = new MiniBoss(bossPos, this, bossHealth);
                        addObject(boss);


                        Message bossMsg = new Message(
                                new Vector2D(Constants.WIDTH / 2, Constants.HEIGHT / 2 - 100),
                                false,
                                "MINI JEFE DETECTADO!",
                                Color.RED,
                                true,
                                Assets.fontBig,
                                this
                        );
                        bossMsg.setLifespan(4000);
                        addMessage(bossMsg);
                    }

                }).start();
            }
        }
    }

    // Spawnear sub-oleada de meteoros "poco a poco"
    private void spawnMeteorSubWave(int count) {
        Message subWaveMsg = new Message(
                new Vector2D(Constants.WIDTH / 2, Constants.HEIGHT / 2 + 60),
                false,
                "REFUERZOS DETECTADOS!",
                Color.ORANGE,
                true,
                Assets.fontMed,
                this
        );
        subWaveMsg.setLifespan(2500);
        addMessage(subWaveMsg);

        new Thread(() -> {
            try {
                for (int i = 0; i < count; i++) {
                    double x = i % 2 == 0 ? Math.random() * Constants.WIDTH : 0;
                    double y = i % 2 == 0 ? 0 : Math.random() * Constants.HEIGHT;
                    Vector2D velocity = new Vector2D(Math.random() * 2 - 1, Math.random() * 2 - 1).normalize().scale(2);
                    int family = (int) (Math.random() * 3);
                    Meteor meteor = new Meteor(
                            new Vector2D(x, y),
                            velocity,
                            2,
                            Assets.bigs[family],
                            this,
                            Size.BIG,
                            family
                    );
                    addObject(meteor);

                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                nextWaveStarting = false;
            }
        }).start();
    }


    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        for (Message msg : messages)
            msg.draw(g2d);

        for (MovingObject obj : movingObjects)
            obj.draw(g);

        for (Animation anim : explosion) {
            g2d.drawImage(
                    anim.getCurrentFrame(),
                    (int) anim.getPosition().getX(),
                    (int) anim.getPosition().getY(),
                    null
            );
        }

        g.drawImage(pauseButtonImg, pauseButtonBounds.x, pauseButtonBounds.y, null);

        if (startingCountdown && countdownValue > 0) {
            g.setFont(Assets.fontBig);
            g.setColor(Color.WHITE);
            String text = countdownValue + "!";
            int textWidth = g.getFontMetrics().stringWidth(text);
            g.drawString(text, (Constants.WIDTH - textWidth) / 2, Constants.HEIGHT / 2);
        }

        if (paused && showPauseMenu) {
            g2d.setColor(new Color(0, 0, 0, (int) (255 * pauseOverlayAlpha)));
            g2d.fillRect(0, 0, Constants.WIDTH, Constants.HEIGHT);

            g.drawImage(
                    resumeButtonBounds.contains(MouseInput.getMousePosition()) ? resumeButtonHoverImg : resumeButtonImg,
                    resumeButtonBounds.x, resumeButtonBounds.y,
                    resumeButtonBounds.width, resumeButtonBounds.height, null
            );
            g.setColor(Color.BLACK);
            g.setFont(Assets.fontMed);
            g.drawString("REANUDAR", resumeButtonBounds.x + 120, resumeButtonBounds.y + 38);

            g.drawImage(
                    settingsButtonBounds.contains(MouseInput.getMousePosition()) ? settingsButtonHoverImg : settingsButtonImg,
                    settingsButtonBounds.x, settingsButtonBounds.y,
                    settingsButtonBounds.width, settingsButtonBounds.height, null
            );
            g.drawString("CONFIGURACIONES", settingsButtonBounds.x + 60, settingsButtonBounds.y + 38);

            g.drawImage(
                    menuButtonBounds.contains(MouseInput.getMousePosition()) ? menuButtonHoverImg : menuButtonImg,
                    menuButtonBounds.x, menuButtonBounds.y,
                    menuButtonBounds.width, menuButtonBounds.height, null
            );
            g.drawString("MENU PRINCIPAL", menuButtonBounds.x + 80, menuButtonBounds.y + 38);
        }

        drawScore(g);
        drawLives(g);
    }

    private void drawScore(Graphics g) {
        Vector2D pos = new Vector2D(1120, 35);
        String scoreToString = Integer.toString(score);

        for (int i = 0; i < scoreToString.length(); i++) {
            g.drawImage(
                    Assets.numbers[Integer.parseInt(scoreToString.substring(i, i + 1))],
                    (int) pos.getX(),
                    (int) pos.getY(),
                    null
            );
            pos.setX(pos.getX() + 15);
        }
    }

    private void drawLives(Graphics g) {
        Vector2D livePosition = new Vector2D(20, 620);
        g.drawImage(Assets.life, (int) livePosition.getX(), (int) livePosition.getY(), null);
        g.drawImage(Assets.numbers[10], (int) livePosition.getX() + 40, (int) livePosition.getY() + 2, null);

        String livesToString = Integer.toString(lives);
        Vector2D pos = new Vector2D(livePosition.getX(), livePosition.getY());
        for (int i = 0; i < livesToString.length(); i++) {
            int number = Integer.parseInt(livesToString.substring(i, i + 1));
            if (number <= 0) break;
            g.drawImage(Assets.numbers[number], (int) pos.getX() + 65, (int) pos.getY() + 2, null);
            pos.setX(pos.getX() + 25);
        }
    }

    // ------------------ Musica ------------------
    public void pauseMusic() {
        if (Assets.backgroundMusic != null)
            Assets.backgroundMusic.pause();
    }

    public void resumeMusic() {
        if (Assets.backgroundMusic != null && !Assets.backgroundMusic.isPlaying())
            Assets.backgroundMusic.resume();
    }

    public void stopMusic() {
        if (Assets.backgroundMusic != null) {
            Assets.backgroundMusic.stop();
        }
    }

    // ------------------ Gestion de objetos y mensajes ------------------
    public Player getPlayer() {
        return player;
    }

    public void subtractLife() {
        lives--;
        if (lives <= 0) {
            stopMusic();
            State.changeState(new GameOverState(score, waves));
            Assets.gameOver.play();
        }
    }

    public void addObject(MovingObject obj) {
        objectsToAdd.add(obj);
    }

    public void removeObject(MovingObject obj) {
        objectsToRemove.add(obj);
    }

    public void addMessage(Message msg) {
        messages.add(msg);
    }

    public void removeMessage(Message msg) {
        messagesToRemove.add(msg);
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public ArrayList<MovingObject> getMovingObjects() {
        return movingObjects;
    }

    public Sound getBackgroundMusic() {
        return Assets.backgroundMusic;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        if (paused)
            pauseMusic();
        else
            resumeMusic();
    }

    public void addScore(int value, Vector2D position) {
        score += value;
        messages.add(new Message(position, true, "+" + value + " puntos", Color.WHITE, false, Assets.fontMed, this));
    }

    // Nuevo metodo para penalizacion del jugador
    public void subtractScore(int value, Vector2D position) {
        score -= value;
        if (score < 0) score = 0; // evitar negativos
        messages.add(new Message(position, true, "-" + value + " puntos", Color.RED, false, Assets.fontMed, this));
    }
}