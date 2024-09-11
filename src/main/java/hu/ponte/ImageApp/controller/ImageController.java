package hu.ponte.ImageApp.controller;

import hu.ponte.ImageApp.entity.ImageEntity;
import hu.ponte.ImageApp.service.ImageService;
import hu.ponte.ImageApp.util.AESUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/files")
public class ImageController {

    private final ImageService imageService;

    @Autowired
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * Képfájlok feltöltése és átméretezése.
     *
     * @param files  Több képfájl is feltölthető egyszerre.
     * @param width  Az átméretezéshez szükséges szélesség pixelben.
     * @param height Az átméretezéshez szükséges magasság pixelben.
     * @return HTTP válasz, amely siker vagy hibajelzést ad vissza.
     * @throws Exception Ha valamilyen hiba történik a feltöltés vagy átméretezés során.
     */
    @Operation(summary = "Képfájlok feltöltése", description = "Több kép feltöltése és átméretezése.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sikeres feltöltés."),
            @ApiResponse(responseCode = "400", description = "Érvénytelen fájlformátum vagy méret."),
            @ApiResponse(responseCode = "500", description = "Szerverhiba történt.")
    })
    @PostMapping
    public ResponseEntity<String> uploadFiles(@RequestParam("files") MultipartFile[] files,
                                              @RequestParam("width") int width,
                                              @RequestParam("height") int height) throws Exception {
        try {
            imageService.handleImageUpload(files, width, height);
            return ResponseEntity.ok("Fájlok sikeresen feltöltve.");
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Titkosított kép letöltése fájlnév alapján.
     *
     * @param fileName A letölteni kívánt fájl neve.
     * @return HTTP válasz a fájl tartalmával vagy hibaüzenettel.
     * @throws Exception Ha a fájl letöltése sikertelen.
     */
    @Operation(summary = "Kép letöltése", description = "Titkosított kép letöltése fájlnév alapján.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sikeres letöltés."),
            @ApiResponse(responseCode = "404", description = "A megadott fájl nem található."),
            @ApiResponse(responseCode = "500", description = "Szerverhiba történt.")
    })
    @GetMapping("/{fileName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable("fileName") String fileName) throws Exception {
        try {
            byte[] fileData = imageService.downloadFile(fileName);
            String fileExtension = getFileExtension(fileName);
            String contentType;
            if ("png".equalsIgnoreCase(fileExtension)) {
                contentType = "image/png";
            } else if ("jpg".equalsIgnoreCase(fileExtension) || "jpeg".equalsIgnoreCase(fileExtension)) {
                contentType = "image/jpeg";
            } else {
                return ResponseEntity.badRequest().body(null);
            }

            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .body(fileData);

        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * A fájl kiterjesztésének meghatározása.
     *
     * @param fileName A fájl neve.
     * @return A fájl kiterjesztése, ha létezik, különben üres string.
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex >= 0) ? fileName.substring(dotIndex + 1) : "";
    }

    /**
     * Az összes fájl letöltése egy ZIP fájlban.
     *
     * @return ZIP fájl a titkosított képekkel vagy hibaüzenettel.
     * @throws Exception Ha a ZIP fájl létrehozása vagy a fájlok beillesztése sikertelen.
     */
    @Operation(summary = "Minden fájl letöltése ZIP-ben", description = "Az összes fájl letöltése egy ZIP fájlban.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sikeres ZIP letöltés."),
            @ApiResponse(responseCode = "500", description = "Szerverhiba történt.")
    })
    @GetMapping("/zip")
    public ResponseEntity<byte[]> downloadAllFilesAsZip() throws Exception {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ZipOutputStream zipOut = new ZipOutputStream(byteArrayOutputStream);

            List<ImageEntity> images = imageService.getAllImages();
            for (ImageEntity image : images) {
                ZipEntry zipEntry = new ZipEntry(image.getFileName());
                zipOut.putNextEntry(zipEntry);
                byte[] decryptedImage = AESUtil.decrypt(image.getEncryptedData());
                zipOut.write(decryptedImage);
                zipOut.closeEntry();
            }

            zipOut.close();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=images.zip")
                    .header(HttpHeaders.CONTENT_TYPE, "application/zip")
                    .body(byteArrayOutputStream.toByteArray());
        } catch (Exception e) {
            throw e;
        }
    }
}
