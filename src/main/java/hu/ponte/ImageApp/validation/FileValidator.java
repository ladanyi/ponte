package hu.ponte.ImageApp.validation;

import hu.ponte.ImageApp.repository.ImageRepository;
import hu.ponte.ImageApp.util.ErrorMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.Arrays;

/**
 * A fájlok típusának, méreteinek és meglétének ellenőrzéséért felelős osztály.
 */
@Component
public class FileValidator {

    @Value("${image.max.width}")
    private int maxWidth;

    @Value("${image.max.height}")
    private int maxHeight;

    @Value("${app.allowedFileTypes}")
    private String[] allowedFileTypes;

    private final ImageRepository imageRepository;

    /**
     * Konstruktor a FileValidator osztályhoz.
     *
     * @param imageRepository a repository, amely segítségével ellenőrizzük, hogy létezik-e már a fájl
     */
    @Autowired
    public FileValidator(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    /**
     * Ellenőrzi a feltöltött fájlt: típusát, méreteit, valamint hogy létezik-e már a repositoryban.
     *
     * @param file a feltöltött fájl
     * @throws IOException ha I/O hiba lép fel az ellenőrzés közben
     */
    public void validateFile(MultipartFile file) throws IOException {
        validateFileType(file);
        validateImageDimensions(file);
        validateFileAlreadyExists(file);
    }

    /**
     * Ellenőrzi, hogy a fájl típusa PNG vagy JPG-e.
     *
     * @param file a feltöltött fájl
     * @throws IllegalArgumentException ha a fájl típusa nem támogatott
     */
    private void validateFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (!Arrays.asList(allowedFileTypes).contains(contentType)) {
            throw new IllegalArgumentException(ErrorMessages.INVALID_FILE_TYPE);
        }
    }

    /**
     * Ellenőrzi, hogy a kép méretei nem haladják meg a maximálisan megengedett szélességet és magasságot.
     *
     * @param file a feltöltött fájl
     * @throws IllegalArgumentException ha a kép méretei meghaladják a megengedett maximumot
     * @throws IOException              ha hiba történik a kép beolvasása közben
     */
    private void validateImageDimensions(MultipartFile file) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
        int imageWidth = bufferedImage.getWidth();
        int imageHeight = bufferedImage.getHeight();

        if (imageWidth > maxWidth || imageHeight > maxHeight) {
            throw new IllegalArgumentException(String.format(ErrorMessages.IMAGE_SIZE_EXCEEDS_LIMIT, maxWidth, maxHeight));
        }
    }

    /**
     * Ellenőrzi, hogy a fájl már létezik-e a repositoryban.
     *
     * @param file a feltöltött fájl
     * @throws FileAlreadyExistsException ha a fájl már létezik
     */
    private void validateFileAlreadyExists(MultipartFile file) throws FileAlreadyExistsException {
        if (imageRepository.existsByFileName(file.getOriginalFilename())) {
            throw new FileAlreadyExistsException(ErrorMessages.FILE_ALREADY_EXISTS + file.getOriginalFilename());
        }
    }
}
