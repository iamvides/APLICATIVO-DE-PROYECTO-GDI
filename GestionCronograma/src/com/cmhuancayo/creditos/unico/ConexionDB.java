package com.cmhuancayo.creditos.unico;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class ConexionDB {

    // Puerto por defecto. Si el usuario elige otro al crear la BD, se actualizará.
    private static String URL =
        "jdbc:postgresql://127.0.0.1:5432/bd_creditos_personales_cmhuancayo";

    private static final String USUARIO  = "administrador";
    private static final String PASSWORD = "admin123";

    public static Connection getConnection() {
        Connection conexion = null;

        try {
            Class.forName("org.postgresql.Driver");

            try {
                // 1) Intento normal (cuando la BD ya existe)
                conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);

            } catch (SQLException exConn) {
                // 2) Si falla, pedimos datos de admin para crear la BD
                final boolean[] creado = { false };

                LoginAuxCreateDB.showDialog((port, adminUser, adminPass) -> {
                    try {
                        CreadorCompletoDB.crearTodo(port, adminUser, adminPass);

                        // Actualizamos URL con el puerto elegido
                        URL = "jdbc:postgresql://127.0.0.1:" + port
                                + "/bd_creditos_personales_cmhuancayo";

                        creado[0] = true;

                        JOptionPane.showMessageDialog(
                                null,
                                "Base de datos creada correctamente.",
                                "Información",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(
                                null,
                                "No se pudo crear la base de datos:\n" + e.getMessage(),
                                "Error al crear BD",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                });

                if (!creado[0]) {
                    throw new SQLException("No se pudo crear la base de datos.");
                }

                // 3) Nueva conexión ya con la BD creada
                conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
            }

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
                    "Error de conexión a PostgreSQL: " + ex.getMessage(),
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
