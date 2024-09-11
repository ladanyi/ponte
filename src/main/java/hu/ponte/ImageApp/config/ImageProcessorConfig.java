package hu.ponte.ImageApp.config;

import hu.ponte.ImageApp.processor.ImageMagickProcessor;
import hu.ponte.ImageApp.processor.GraphicsMagickProcessor;
import hu.ponte.ImageApp.processor.ImageProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

/**
 * Konfigurációs osztály, amely az alkalmazásban használt képkezelő processzort állítja be.
 * A processzor típusa a konfigurációs fájlban megadott érték alapján választódik ki.
 */
@Configuration
public class ImageProcessorConfig {

    /**
     * Visszaadja a megfelelő ImageProcessor implementációt.
     * A processzor típusa a konfigurációs fájl "image.processor" kulcsának értéke alapján kerül kiválasztásra.
     *
     * @param processorType A használandó processzor típusa, ami lehet "imagemagick" vagy "graphicsmagick".
     * @return A megfelelő ImageProcessor implementáció (ImageMagickProcessor vagy GraphicsMagickProcessor).
     */
    @Bean
    public ImageProcessor imageProcessor(@Value("${image.processor}") String processorType) {
        if ("graphicsmagick".equalsIgnoreCase(processorType)) {
            return new GraphicsMagickProcessor();
        } else {
            return new ImageMagickProcessor();
        }
    }
}
