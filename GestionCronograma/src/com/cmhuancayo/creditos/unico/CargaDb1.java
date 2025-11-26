package com.cmhuancayo.creditos.unico;

import java.sql.Connection;
import java.sql.Statement;

public class CargaDb1 {
    public static void ejecutar(Connection conn) {
        try (Statement st = conn.createStatement()) {
            // Crear rol administrador si no existe
            st.executeUpdate(
                "DO $$\n" +
                "BEGIN\n" +
                "    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'administrador') THEN\n" +
                "        CREATE ROLE administrador\n" +
                "            LOGIN\n" +
                "            SUPERUSER\n" +
                "            CREATEDB\n" +
                "            CREATEROLE\n" +
                "            INHERIT\n" +
                "            PASSWORD 'admin123';\n" +
                "    END IF;\n" +
                "END;\n" +
                "$$;"
            );
        } catch (Exception ex) {
            // No mostramos error al usuario aquí
        }
    }
}
