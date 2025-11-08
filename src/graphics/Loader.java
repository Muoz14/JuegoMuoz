package graphics;

import javax.imageio.ImageIO;
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

    // --- METODO DE CARGA FONDO CON ESCALA ---
    /**
     * Carga una imagen y la escala a un tamano especifico.
     * @param path Ruta a la imagen
     * @param targetWidth Ancho deseado
     * @param targetHeight Alto deseado
     * @return La imagen escalada
     */
    public static BufferedImage ImageLoader(String path, int targetWidth, int targetHeight) {
        try {
            // 1. Cargar la imagen original
            BufferedImage originalImage = ImageIO.read(Loader.class.getResource(path));

            // 2. Crear una nueva imagen con el tamano deseado
            BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);

            // 3. Dibujar la imagen original sobre la nueva, escalandola
            Graphics2D g2d = scaledImage.createGraphics();

            // Usar 'hints' para un escalado de mejor calidad
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
            g2d.dispose();

            // 4. Devolver la imagen ya escalada
            return scaledImage;

        } catch (IOException e) {
            throw new RuntimeException("Error al cargar y escalar imagen: " + path, e);
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