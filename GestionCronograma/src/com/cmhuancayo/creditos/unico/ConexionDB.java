package com.cmhuancayo.creditos.unico;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class ConexionDB {

    // --- CONFIGURACIÓN DE LA BASE DE DATOS ---
    // Corregido: se agregó allowPublicKeyRetrieval=true
    // para evitar el error "Public Key Retrieval is not allowed"
    private static final String URL =
            "jdbc:mysql://127.0.0.1:3306/bd_creditos_personales_cmhuancayo"
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=America/Lima"
            + "&noAccessToProcedureBodies=true";

    private static final String USUARIO  = "tu_usuario";
    private static final String PASSWORD = "tu_contraseña_app";

    public static Connection getConnection() {
        Connection conexion = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);

        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "Error: No se encontró el driver JDBC. Verifica el JAR en el Build Path.",
                    "ERROR",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "Error de Conexión a MySQL: " + ex.getErrorCode() + " - " + ex.getMessage()
                            + "\nVerifica credenciales y si MySQL Server está activo.",
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
