package hu.ponte.ImageApp.repository;

import hu.ponte.ImageApp.entity.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Az ImageEntity entitáshoz tartozó JPA repository, amely az adatbázisműveletek
 * elvégzéséhez szükséges metódusokat biztosítja.
 */
public interface ImageRepository extends JpaRepository<ImageEntity, Long> {

    /**
     * Megkeresi a képet a fájl neve alapján.
     *
     * @param fileName A kép fájlneve, amely alapján keresés történik.
     * @return Az ImageEntity entitás, ha megtalálható, különben null.
     */
    ImageEntity findByFileName(String fileName);

    /**
     * Ellenőrzi, hogy létezik-e már egy kép a megadott fájlnévvel az adatbázisban.
     *
     * @param fileName A keresett fájl neve.
     * @return true, ha a fájlnévvel rendelkező kép már létezik, különben false.
     */
    boolean existsByFileName(String fileName);
}
