package com.cmhuancayo.creditos.unico;

import java.sql.Connection;
import java.sql.DriverManager;

public class CreadorCompletoDB {

    private static final String DB_NAME = "bd_creditos_personales_cmhuancayo";

    /**
     * Crea rol, base de datos y estructura completa.
     * Se llama solo una vez, usando usuario admin (postgres u otro).
     */
    public static void crearTodo(String port, String adminUser, String adminPassword) {
        Connection connAdmin = null;
        try {
            Class.forName("org.postgresql.Driver");

            // 1. Conexión a BD "postgres" con el usuario admin que ingresa el usuario
            String urlAdmin = "jdbc:postgresql://127.0.0.1:" + port + "/postgres";
            connAdmin = DriverManager.getConnection(urlAdmin, adminUser, adminPassword);

            // 2. Crear rol administrador si no existe
            CargaDb1.ejecutar(connAdmin);

            // 3. Crear base de datos si no existe
            CargaDb2.ejecutar(connAdmin);

            try { Thread.sleep(1000); } catch (InterruptedException ignore) {}

            // 4. Crear toda la estructura dentro de la BD, como usuario administrador
            Connection connNueva = null;
            try {
                String urlApp = "jdbc:postgresql://127.0.0.1:" + port + "/" + DB_NAME;
                connNueva = DriverManager.getConnection(urlApp, "administrador", "admin123");
                CargaDb3.ejecutar(connNueva);
            } finally {
                if (connNueva != null) {
                    try { connNueva.close(); } catch (Exception ignore) {}
                }
            }
        } catch (Exception ex) {
            // Propaga error genérico para que la conexión lo maneje
            throw new RuntimeException("Error al crear la base de datos y su estructura.", ex);
        } finally {
            if (connAdmin != null) {
                try { connAdmin.close(); } catch (Exception ignore) {}
            }
        }
    }
}
