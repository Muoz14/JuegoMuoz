package graphics;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class Sound {

    private Clip clip;
    private boolean isMusic = false; // indica si es música o SFX
    private FloatControl volumeControl;
    private boolean looping = false;

    // Constructor simple: solo con el path del sonido
    public Sound(String path) {
        this(path, false, false);
    }

    // Constructor con opción de loop
    public Sound(String path, boolean loop) {
        this(path, loop, false);
    }

    // Constructor completo: loop + música
    public Sound(String path, boolean loop, boolean isMusic) {
        try {
            URL url = Sound.class.getResource(path);
            if (url == null) {
                System.err.println("No se encontró el archivo de sonido: " + path);
                return;
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            clip = AudioSystem.getClip();
            clip.open(ais);

            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            }

            this.looping = loop;
            this.isMusic = isMusic;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    // Indica si es música
    public boolean isMusic() {
        return isMusic;
    }

    // Marca el sonido como música o SFX
    public void setMusic(boolean isMusic) {
        this.isMusic = isMusic;
    }

    // Reproducir sonido desde el inicio
    public void play() {
        if (clip == null) return;

        clip.stop();
        clip.setFramePosition(0);
        clip.start();

        if (looping) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    // Detener sonido
    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    // Pausar sonido
    public void pause() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    // Reanudar sonido
    public void resume() {
        if (clip != null && !clip.isRunning()) {
            clip.start();
        }
    }

    // Cambiar volumen (0.0f a 1.0f)
    public void setVolume(float volume) {
        if (volumeControl == null) return;

        volume = Math.max(0f, Math.min(volume, 1f));
        float min = volumeControl.getMinimum();  // suele ser -80 dB
        float max = volumeControl.getMaximum();  // suele ser +6 dB
        float gain = min + (max - min) * volume;
        volumeControl.setValue(gain);
    }

    // Devuelve el volumen actual en decibeles
    public float getVolumeDecibel() {
        if (volumeControl == null) return 0f;
        return volumeControl.getValue();
    }

    // Cambiar volumen en decibeles directamente
    public void changeVolume(float dB) {
        if (volumeControl != null) {
            volumeControl.setValue(dB);
        }
    }

    // Reproducir en loop
    public void loop() {
        if (clip != null) {
            looping = true;
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    // Devuelve si el sonido se está reproduciendo
    public boolean isPlaying() {
        return clip != null && clip.isRunning();
    }

    // Devuelve la posición del frame actual
    public int getFramePosition() {
        if (clip == null) return 0;
        return clip.getFramePosition();
    }

    // Liberar recursos
    public void close() {
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }
}
