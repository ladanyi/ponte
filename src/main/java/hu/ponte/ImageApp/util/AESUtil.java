package hu.ponte.ImageApp.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility osztály AES titkosításhoz és visszafejtéshez.
 * Kezeli a titkosítási kulcs generálását, betöltését, valamint a titkosítást és visszafejtést.
 */
public class AESUtil {

    private static final String AES = "AES";

    /**
     * Titkosító kulcs generálása AES algoritmushoz.
     * A generált kulcsot fájlba menti (secretKey.key).
     *
     * @return A generált titkos kulcs.
     * @throws Exception ha a kulcs generálása sikertelen.
     */
    public static SecretKey generateSecretKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(AES);
        keyGen.init(256);  // 256 bites kulcs
        SecretKey secretKey = keyGen.generateKey();
        Files.write(Paths.get("secretKey.key"), secretKey.getEncoded());
        return secretKey;
    }

    /**
     * Betölti a korábban mentett titkos kulcsot a secretKey.key fájlból.
     * Ha a fájl nem létezik, új kulcsot generál és elmenti.
     *
     * @return A betöltött vagy újonnan generált titkos kulcs.
     * @throws Exception ha a kulcs betöltése vagy generálása sikertelen.
     */
    public static SecretKey loadSecretKey() throws Exception {
        if (!Files.exists(Paths.get("secretKey.key"))) {
            return generateSecretKey();
        }

        byte[] keyBytes = Files.readAllBytes(Paths.get("secretKey.key"));
        return new SecretKeySpec(keyBytes, AES);
    }

    /**
     * Adatok titkosítása az AES algoritmus használatával.
     *
     * @param data A titkosítandó adatok byte tömbje.
     * @return A titkosított byte tömb.
     * @throws Exception ha a titkosítás sikertelen.
     */
    public static byte[] encrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.ENCRYPT_MODE, loadSecretKey());
        return cipher.doFinal(data);
    }

    /**
     * Adatok visszafejtése az AES algoritmus használatával.
     *
     * @param data A visszafejtendő adatok byte tömbje.
     * @return A visszafejtett byte tömb.
     * @throws Exception ha a visszafejtés sikertelen.
     */
    public static byte[] decrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.DECRYPT_MODE, loadSecretKey());
        return cipher.doFinal(data);
    }
}
