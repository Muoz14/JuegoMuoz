package graphics;

import java.util.ArrayList;
import java.util.List;

public class SoundManager {

    private static SoundManager instance;
    private List<Sound> sounds = new ArrayList<>();

    // Volumen global (0.0 a 1.0)
    private float globalVolume = 1.0f;

    // Volumen específico
    private float musicVolume = 0.8f;
    private float sfxVolume = 0.8f;

    private SoundManager() {
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    // Registrar un sonido para gestión centralizada
    public void registerSound(Sound sound) {
        if (!sounds.contains(sound)) {
            sounds.add(sound);
            if (sound.isMusic()) {
                sound.setVolume(musicVolume);
            } else {
                sound.setVolume(sfxVolume);
            }
        }
    }

    public void setGlobalVolume(float volume) {
        globalVolume = Math.max(0f, Math.min(volume, 1f));
        for (Sound s : sounds) {
            if (s.isMusic()) s.setVolume(musicVolume);
            else s.setVolume(sfxVolume);
        }
    }

    public void setMusicVolume(float volume) {
        musicVolume = Math.max(0f, Math.min(volume, 1f));
        for (Sound s : sounds) {
            if (s.isMusic()) s.setVolume(musicVolume);
        }
    }

    public void setSFXVolume(float volume) {
        sfxVolume = Math.max(0f, Math.min(volume, 1f));
        for (Sound s : sounds) {
            if (!s.isMusic()) s.setVolume(sfxVolume);
        }
    }

    public float getGlobalVolume() {
        return globalVolume;
    }

    public void pauseAll() {
        for (Sound s : sounds) {
            s.pause();
        }
    }

    public void resumeAll() {
        for (Sound s : sounds) {
            s.resume();
        }
    }

    public void stopAll() {
        for (Sound s : sounds) {
            s.stop();
        }
    }
}
