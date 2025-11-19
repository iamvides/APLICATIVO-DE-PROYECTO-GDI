/**
 * Define los módulos de Java.
 * El nombre del módulo (GestionCronograma) debe coincidir con el nombre de tu proyecto.
 * NOTA: Este archivo es clave solo si decides seguir usando el sistema de módulos de Java (Project Jigsaw).
 */
module GestionCronograma {
    // 1. Módulos obligatorios para la funcionalidad

    // Requerimos java.sql para la conexión JDBC a MySQL
    requires java.sql;
    
    // Requerimos java.desktop para la interfaz gráfica (Swing) y JOptionPane
    requires java.desktop;

    // 2. Exportamos el paquete principal para que las clases del módulo 
    //    puedan ser usadas por otras partes (aunque estemos en un solo paquete).
    exports com.cmhuancayo.creditos.unico;
}