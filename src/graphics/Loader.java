package graphics;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Loader {

    // Objeto para cargar imagenes
    public static BufferedImage ImageLoader(String path) {
        try {
            return ImageIO.read(Loader.class.getResource(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Font loadFont(String path, int size) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, Loader.class.getResourceAsStream(path))
                    .deriveFont(Font.PLAIN, size);
        } catch (FontFormatException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}