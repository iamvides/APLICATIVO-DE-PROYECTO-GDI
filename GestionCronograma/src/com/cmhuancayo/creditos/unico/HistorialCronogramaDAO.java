package com.cmhuancayo.creditos.unico;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistorialCronogramaDAO {

    /**
     * Registra un cambio de cronograma en la tabla historial_cambios_cronograma.
     */
    public boolean registrarCambio(int codigoContrato,
                                   String tipoCambio,
                                   String descripcion,
                                   String usuario) {

        Connection cnx = null;
        PreparedStatement ps = null;

        String sql = "INSERT INTO historial_cambios_cronograma " +
                     "  (codigo_contrato, fecha_cambio, tipo_cambio, " +
                     "   descripcion_cambio, usuario_registro) " +
                     "VALUES (?, NOW(), ?, ?, ?)";

        try {
            cnx = ConexionDB.getConnection();
            ps = cnx.prepareStatement(sql);
            ps.setInt(1, codigoContrato);
            ps.setString(2, tipoCambio);
            ps.setString(3, descripcion);
            ps.setString(4, usuario);

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Error al registrar historial de cronograma: " + e.getMessage(),
                    "Error SQL",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;

        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException ignored) {}
            ConexionDB.close(cnx);
        }
    }

    /**
     * Lista los cambios del cronograma para un contrato.
     */
    public List<HistorialCambioCronograma> listarPorContrato(int codigoContrato) {
        List<HistorialCambioCronograma> lista = new ArrayList<>();

        Connection cnx = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sql = "SELECT codigo_historial, codigo_contrato, fecha_cambio, " +
                     "       tipo_cambio, descripcion_cambio, usuario_registro " +
                     "FROM historial_cambios_cronograma " +
                     "WHERE codigo_contrato = ? " +
                     "ORDER BY fecha_cambio DESC, codigo_historial DESC";

        try {
            cnx = ConexionDB.getConnection();
            ps = cnx.prepareStatement(sql);
            ps.setInt(1, codigoContrato);
            rs = ps.executeQuery();

            while (rs.next()) {
                HistorialCambioCronograma h = new HistorialCambioCronograma();
                h.setCodigoHistorial(rs.getInt("codigo_historial"));
                h.setCodigoContrato(rs.getInt("codigo_contrato"));
                h.setFechaCambio(rs.getTimestamp("fecha_cambio"));
                h.setTipoCambio(rs.getString("tipo_cambio"));
                h.setDescripcionCambio(rs.getString("descripcion_cambio"));
                h.setUsuarioRegistro(rs.getString("usuario_registro"));
                lista.add(h);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Error al listar historial de cronograma: " + e.getMessage(),
                    "Error SQL",
                    JOptionPane.ERROR_MESSAGE
            );
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException ignored) {}
            ConexionDB.close(cnx);
        }

        return lista;
    }
}
