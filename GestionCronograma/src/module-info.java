/**
 * Define los módulos de Java.
 */
module GestionCronograma {
    // JDBC
    requires java.sql;

    // Swing / AWT
    requires java.desktop;

    // PDFBox
    requires org.apache.pdfbox;

    // Dependencias de PDFBox
    requires org.apache.fontbox;   // fontbox-2.0.35.jar
    requires commons.logging;      // commons-logging-1.2.jar

    // Exportamos nuestro paquete
    exports com.cmhuancayo.creditos.unico;
}
