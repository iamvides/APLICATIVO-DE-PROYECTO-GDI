package com.cmhuancayo.creditos.unico;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class ConexionDB {

    // --- CONFIGURACIÓN DE LA BASE DE DATOS (POSTGRESQL) ---
    //
    // Asegúrate de que en PostgreSQL creaste:
    //   CREATE USER tu_usuario WITH PASSWORD 'tu_contraseña_app';
    //   CREATE DATABASE bd_creditos_personales_cmhuancayo OWNER tu_usuario;
    //
    // Si PostgreSQL está en tu propia PC, el host normalmente es localhost
    // y el puerto por defecto es 5432.
    private static final String URL =
            "jdbc:postgresql://127.0.0.1:5432/bd_creditos_personales_cmhuancayo";

    private static final String USUARIO  = "administrador";
    private static final String PASSWORD = "admin123";

    public static Connection getConnection() {
        Connection conexion = null;
        try {
            // Cargar el driver JDBC de PostgreSQL
            Class.forName("org.postgresql.Driver");

            // Establecer la conexión
            conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);

        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "Error: No se encontró el driver JDBC de PostgreSQL.\n" +
                    "Verifica que el JAR del driver esté agregado al proyecto.",
                    "ERROR",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "Error de conexión a PostgreSQL: " + ex.getSQLState() + " - " + ex.getMessage()
                            + "\nVerifica credenciales y si el servidor PostgreSQL está activo.",
                    "ERROR",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        return conexion;
    }

    public static void close(Connection conexion) {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException ex) {
            System.err.println("Error al cerrar la conexión: " + ex.getMessage());
        }
    }
}
