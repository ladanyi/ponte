## Alapvető konfigurációk
Az application.properties-ben egyebek mellett megadhatók az adatbáziskapcsolathoz tartozó adatok, valamint a használni kívánt képfeldolgozási eszköz (image.processor=graphicsmagick vagy image.processor=imagemagick)
## A projekt fordítása: 
mvn clean install 

## Az alkalmazás futtatása
mvn spring-boot:run

## Az alkalmazás elérése
http://localhost:8080

## Tesztelés
mvn test

## API dokumentáció (Swagger): 
http://localhost:8080/swagger-ui.html 
