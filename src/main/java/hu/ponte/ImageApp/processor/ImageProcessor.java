package hu.ponte.ImageApp.processor;

import java.io.File;
import java.io.IOException;

/**
 * Az ImageProcessor interfész meghatározza a képkezelő funkciókat, mint például
 * a képek átméretezése. Ez az interfész lehetővé teszi különböző képkezelő
 * eszközök cseréjét (pl. ImageMagick, GraphicsMagick) anélkül, hogy módosítani
 * kellene az alkalmazás fő logikáját.
 */
public interface ImageProcessor {

    /**
     * Átméretezi a megadott képet a kívánt szélességre és magasságra.
     *
     * @param inputFile      A bemeneti kép fájlja.
     * @param width          A kívánt szélesség pixelben.
     * @param height         A kívánt magasság pixelben.
     * @param outputFilePath Az átméretezett kép kimeneti fájl elérési útja.
     * @return A kimeneti fájl, amely az átméretezett képet tartalmazza.
     * @throws IOException          Ha hiba történik a fájlírás vagy olvasás során.
     * @throws InterruptedException Ha a képátméretezési folyamat megszakad.
     */
    File resizeImage(File inputFile, int width, int height, String outputFilePath) throws IOException, InterruptedException;
}
