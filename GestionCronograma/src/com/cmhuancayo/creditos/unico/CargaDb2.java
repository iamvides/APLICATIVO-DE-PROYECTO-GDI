package com.cmhuancayo.creditos.unico;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class CargaDb2 {
    public static void ejecutar(Connection conn) {
        Statement st = null;
        try {
            st = conn.createStatement();

            // Verificar si ya existe la BD
            java.sql.ResultSet rs = st.executeQuery(
                "SELECT 1 FROM pg_database WHERE datname = 'bd_creditos_personales_cmhuancayo'"
            );
            boolean existe = rs.next();
            rs.close();

            if (!existe) {
                // Crear la base de datos con owner administrador
                st.executeUpdate(
                    "CREATE DATABASE bd_creditos_personales_cmhuancayo " +
                    "WITH OWNER = administrador " +
                    "ENCODING = 'UTF8' " +
                    "TABLESPACE = pg_default " +
                    "CONNECTION LIMIT = -1;"
                );
            }

            // Otorgar privilegios (por si acaso)
            st.executeUpdate(
                "GRANT ALL PRIVILEGES ON DATABASE bd_creditos_personales_cmhuancayo TO administrador;"
            );

        } catch (SQLException ex) {
            // No mostrar nada, puede fallar si ya existe, etc.
        } finally {
            try { if (st != null) st.close(); } catch (Exception ignore) {}
        }
    }
}
