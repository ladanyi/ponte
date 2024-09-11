package hu.ponte.ImageApp.service;

import hu.ponte.ImageApp.entity.ImageEntity;
import hu.ponte.ImageApp.processor.ImageProcessor;
import hu.ponte.ImageApp.repository.ImageRepository;
import hu.ponte.ImageApp.util.AESUtil;
import hu.ponte.ImageApp.validation.FileValidator;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@Service
public class ImageService {

    private final ImageRepository imageRepository;
    private final FileValidator fileValidator;
    private final ImageProcessor imageProcessor;

    /**
     * Konstruktor a függőségek injektálására.
     *
     * @param imageRepository Az adatbázis kezeléséért felelős repository osztály.
     * @param fileValidator   Fájlvalidációt végző osztály.
     * @param imageProcessor  Kép feldolgozását végző osztály (ImageMagick vagy GraphicsMagick).
     */
    @Autowired
    public ImageService(ImageRepository imageRepository, FileValidator fileValidator, ImageProcessor imageProcessor) {
        this.imageRepository = imageRepository;
        this.fileValidator = fileValidator;
        this.imageProcessor = imageProcessor;
    }

    /**
     * Képfájlok feltöltését és átméretezését végzi. A fájlokat ellenőrzi,
     * lementi, átméretezi, titkosítja, majd adatbázisba menti.
     *
     * @param files  A feltöltött képfájlok.
     * @param width  Az átméretezés szélessége.
     * @param height Az átméretezés magassága.
     * @throws Exception Ha hiba történik a fájlok feldolgozása vagy mentése során.
     */
    public void handleImageUpload(MultipartFile[] files, int width, int height) throws Exception {
        for (MultipartFile file : files) {
            fileValidator.validateFile(file);
        }

        String uploadDirPath = System.getProperty("user.dir") + File.separator + "uploads";
        File uploadDir = new File(uploadDirPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        for (MultipartFile file : files) {
            // Fájl mentése az uploads könyvtárba
            File savedFile = new File(uploadDir, file.getOriginalFilename());
            file.transferTo(savedFile);  // Mentés az eredeti fájlba

            // Kép átméretezése a konfigurált processzorral
            File resizedFile = imageProcessor.resizeImage(savedFile, width, height, uploadDirPath + File.separator + "resized_" + file.getOriginalFilename());

            // Átméretezett kép byte[]-ra alakítása
            byte[] imageData = Files.readAllBytes(resizedFile.toPath());

            // Titkosítás
            byte[] encryptedData = AESUtil.encrypt(imageData);

            // Mentés adatbázisba
            ImageEntity imageEntity = new ImageEntity();
            imageEntity.setFileName(file.getOriginalFilename());
            imageEntity.setEncryptedData(encryptedData);
            imageRepository.save(imageEntity);

            // Eltávolítjuk az ideiglenes fájlokat
            savedFile.delete();
            resizedFile.delete();
        }
    }

    /**
     * Kép letöltése fájlnév alapján az adatbázisból. A letöltött fájl titkosítása visszafejtésre kerül.
     *
     * @param fileName A keresett fájl neve.
     * @return A fájl titkosítatlan byte[] reprezentációja.
     * @throws Exception Ha a fájl nem található, vagy hiba történik a letöltés során.
     */
    @Transactional(readOnly = true)
    public byte[] downloadFile(String fileName) throws Exception {
        ImageEntity imageEntity = imageRepository.findByFileName(fileName);
        if (imageEntity == null) {
            throw new hu.ponte.ImageApp.exception.FileNotFoundException("Fájl nem található: " + fileName);
        }
        return AESUtil.decrypt(imageEntity.getEncryptedData());
    }

    /**
     * Visszaadja az összes feltöltött képet az adatbázisból.
     *
     * @return Az összes kép listája.
     */
    public List<ImageEntity> getAllImages() {
        return imageRepository.findAll();
    }
}
