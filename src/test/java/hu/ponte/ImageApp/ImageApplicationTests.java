package hu.ponte.ImageApp;

import hu.ponte.ImageApp.util.ErrorMessages;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ImageApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Value("${image.max.width}")
    private int maxWidth;

    @Value("${image.max.height}")
    private int maxHeight;

    @Value("${app.allowedFileTypes}")
    private String[] allowedFileTypes;

    @Test
    public void testFileUploadAndDownload() throws Exception {
        byte[] imageBytes = Files.readAllBytes(Paths.get("src/test/resources/test.jpg"));

        MockMultipartFile file = new MockMultipartFile("files", "test.jpg", "image/jpeg", imageBytes);

        // Fájl feltöltése
        mockMvc.perform(multipart("/api/files")
                        .file(file)
                        .param("width", "200")
                        .param("height", "200"))
                .andExpect(status().isOk())
                .andExpect(content().string("Fájlok sikeresen feltöltve."));

        // Kép letöltése
        mockMvc.perform(get("/api/files/test.jpg"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"test.jpg\""));
    }

    @Test
    public void testInvalidFileFormat() throws Exception {
        // Hibás formátumú fájl feltöltése
        MockMultipartFile invalidFile = new MockMultipartFile("files", "test.txt", "text/plain", "Invalid data".getBytes());

        mockMvc.perform(multipart("/api/files")
                        .file(invalidFile)
                        .param("width", "200")
                        .param("height", "200"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(ErrorMessages.INVALID_FILE_TYPE));
    }

    @Test
    public void testFileNotFound() throws Exception {
        // Nem létező fájl letöltése
        mockMvc.perform(get("/api/files/nonexistent.jpg"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Fájl nem található: nonexistent.jpg"));
    }

    @Test
    public void testDownloadAllFilesAsZip() throws Exception {
        // Valódi JPG képek használata a teszt során
        byte[] imageBytes1 = Files.readAllBytes(Paths.get("src/test/resources/test.jpg"));
        byte[] imageBytes2 = Files.readAllBytes(Paths.get("src/test/resources/test2.jpg"));

        MockMultipartFile file1 = new MockMultipartFile("files", "test.jpg", "image/jpeg", imageBytes1);
        MockMultipartFile file2 = new MockMultipartFile("files", "test2.jpg", "image/jpeg", imageBytes2);

        // Fájlok feltöltése
        mockMvc.perform(multipart("/api/files")
                        .file(file1)
                        .file(file2)
                        .param("width", "200")
                        .param("height", "200"))
                .andExpect(status().isOk())
                .andExpect(content().string("Fájlok sikeresen feltöltve."));

        // ZIP fájl letöltése
        mockMvc.perform(get("/api/files/zip"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=images.zip"))
                .andExpect(content().contentType("application/zip"));
    }

    @Test
    public void testFileSizeExceedsLimit() throws Exception {
        // Nagy képfájl létrehozása
        int width = maxWidth + 1;
        int height = maxHeight + 1;
        BufferedImage largeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(largeImage, "jpg", baos);
        byte[] largeFileData = baos.toByteArray();

        MockMultipartFile largeFile = new MockMultipartFile("files", "large.jpg", "image/jpeg", largeFileData);

        // Fájl feltöltése
        mockMvc.perform(multipart("/api/files")
                        .file(largeFile)
                        .param("width", String.valueOf(width))
                        .param("height", String.valueOf(height)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(String.format(ErrorMessages.IMAGE_SIZE_EXCEEDS_LIMIT, maxWidth, maxHeight)));
    }

    @Test
    public void testFileAlreadyExists() throws Exception {
        byte[] imageBytes = Files.readAllBytes(Paths.get("src/test/resources/test.jpg"));

        MockMultipartFile file = new MockMultipartFile("files", "duplicate.jpg", "image/jpeg", imageBytes);

        // Első feltöltés sikeres
        mockMvc.perform(multipart("/api/files")
                        .file(file)
                        .param("width", "200")
                        .param("height", "200"))
                .andExpect(status().isOk());

        // Második feltöltés ugyanazzal a fájlnévvel
        mockMvc.perform(multipart("/api/files")
                        .file(file)
                        .param("width", "200")
                        .param("height", "200"))
                .andExpect(status().isConflict())
                .andExpect(content().string(ErrorMessages.FILE_ALREADY_EXISTS + "duplicate.jpg"));
    }
}
