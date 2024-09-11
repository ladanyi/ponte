package hu.ponte.ImageApp.processor;

import java.io.File;
import java.io.IOException;

/**
 * Az ImageMagickProcessor osztály az ImageMagick eszközt használja a képek átméretezésére.
 * Az ImageProcessor interfészt implementálja, és egy konkrét megvalósítást biztosít az ImageMagick számára.
 */
public class ImageMagickProcessor implements ImageProcessor {

    /**
     * Átméretezi a megadott képet a megadott szélességre és magasságra az ImageMagick eszköz segítségével.
     *
     * @param inputFile      A bemeneti kép fájlja.
     * @param width          A kívánt szélesség pixelben.
     * @param height         A kívánt magasság pixelben.
     * @param outputFilePath Az átméretezett kép kimeneti fájl elérési útja.
     * @return A kimeneti fájl, amely az átméretezett képet tartalmazza.
     * @throws IOException          Ha hiba történik a fájlírás vagy olvasás során.
     * @throws InterruptedException Ha a képátméretezési folyamat megszakad.
     */
    @Override
    public File resizeImage(File inputFile, int width, int height, String outputFilePath) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("magick", inputFile.getAbsolutePath(), "-resize", width + "x" + height, outputFilePath);
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("ImageMagick process failed with exit code " + exitCode);
        }
        return new File(outputFilePath);
    }
}
