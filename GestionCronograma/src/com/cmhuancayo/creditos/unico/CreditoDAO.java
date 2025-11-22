package com.cmhuancayo.creditos.unico;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * DAO para la gestión de créditos (contratos).
 *
 * Responsabilidades:
 *  - Listar créditos por cliente.
 *  - Crear un nuevo crédito completo mediante procedimiento almacenado.
 *  - Actualizar contrato luego de reprogramación.
 *  - Marcar contrato como refinanciado.
 *
 * Versión adaptada para PostgreSQL.
 */
public class CreditoDAO {

    /**
     * Lista los contratos (créditos) de un cliente.
     *
     * Relación usada:
     *   cliente -> solicitud -> pre_aprobacion -> contrato
     */
    public List<Credito> listarPorCliente(int codigoCliente) {
        List<Credito> creditos = new ArrayList<>();
        Connection cnx = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sql =
            "SELECT c.codigo_contrato, " +
            "       s.codigo_solicitud, " +
            "       s.codigo_cliente, " +
            "       c.monto_desembolso, " +
            "       c.tasa_interes_compensatorio, " +
            "       c.numero_cuotas, " +
            "       c.fecha_vigencia, " +
            "       c.estado_contrato " +
            "FROM contrato c " +
            "JOIN pre_aprobacion p ON c.codigo_solicitud = p.codigo_solicitud " +
            "JOIN solicitud s ON p.codigo_solicitud = s.codigo_solicitud " +
            "WHERE s.codigo_cliente = ? " +
            "  AND c.estado_contrato = 'VIGENTE' " +
            "ORDER BY c.codigo_contrato";

        try {
            cnx = ConexionDB.getConnection();
            ps = cnx.prepareStatement(sql);
            ps.setInt(1, codigoCliente);
            rs = ps.executeQuery();

            while (rs.next()) {
                Credito c = new Credito();
                c.setCodigoContrato(rs.getInt("codigo_contrato"));
                c.setCodigoSolicitud(rs.getInt("codigo_solicitud"));
                c.setCodigoCliente(rs.getInt("codigo_cliente"));
                c.setMontoDesembolso(rs.getBigDecimal("monto_desembolso"));
                c.setTasaInteresCompensatorio(rs.getBigDecimal("tasa_interes_compensatorio"));
                c.setNumeroCuotas(rs.getInt("numero_cuotas"));
                c.setFechaDesembolso(rs.getDate("fecha_vigencia"));
                // De inicio asumimos saldo = monto desembolsado
                c.setSaldoCapital(rs.getBigDecimal("monto_desembolso"));
                c.setEstadoContrato(rs.getString("estado_contrato"));
                creditos.add(c);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                null,
                "Error al listar créditos: " + e.getMessage(),
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

        return creditos;
    }

    /**
     * Crea un crédito completo para un cliente llamando al SP
     * sp_crearcreditoclientebasico en PostgreSQL.
     *
     * @return código del contrato creado o -1 si hubo error.
     */
    public int crearCreditoParaCliente(int codigoCliente,
                                       BigDecimal monto,
                                       int nroCuotas,
                                       BigDecimal tasaAnual,
                                       Date fechaDesembolso) {

        Connection cnx = null;
        CallableStatement cs = null;

        // IMPORTANTE: 6 placeholders (5 IN + 1 INOUT)
        String sql = "CALL sp_crearcreditoclientebasico(?, ?, ?, ?, ?, ?)";

        try {
            cnx = ConexionDB.getConnection();
            cs = cnx.prepareCall(sql);

            if (fechaDesembolso == null) {
                fechaDesembolso = new Date();
            }

            cs.setInt(1, codigoCliente);
            cs.setBigDecimal(2, monto);
            cs.setInt(3, nroCuotas);
            cs.setBigDecimal(4, tasaAnual);
            cs.setDate(5, new java.sql.Date(fechaDesembolso.getTime()));
            // p_codigo_contrato  (INOUT)
            cs.registerOutParameter(6, Types.INTEGER);

            cs.execute();

            int codigoContrato = cs.getInt(6);
            return codigoContrato;

        } catch (SQLException e) {
            String sqlState = e.getSQLState();
            // Tu SP usa: USING ERRCODE = '45000';
            if ("45000".equals(sqlState)) {
                JOptionPane.showMessageDialog(
                    null,
                    e.getMessage(),
                    "Límite de créditos",
                    JOptionPane.WARNING_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                    null,
                    "Error SQL al crear crédito: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
            return -1;

        } finally {
            try {
                if (cs != null) cs.close();
            } catch (SQLException ignored) {}
            ConexionDB.close(cnx);
        }
    }

    /**
     * Actualiza datos del contrato después de una reprogramación.
     */
    public boolean actualizarContratoReprogramado(int codigoContrato,
                                                  BigDecimal nuevaTasaAnual,
                                                  int nuevoNumeroCuotas) {
        Connection cnx = null;
        PreparedStatement ps = null;

        String sql =
            "UPDATE contrato " +
            "SET tasa_interes_compensatorio = ?, " +
            "    numero_cuotas = ?, " +
            "    plazo_credito = ? " +
            "WHERE codigo_contrato = ?";

        try {
            cnx = ConexionDB.getConnection();
            ps = cnx.prepareStatement(sql);

            ps.setBigDecimal(1, (nuevaTasaAnual == null) ? BigDecimal.ZERO : nuevaTasaAnual);
            ps.setInt(2, nuevoNumeroCuotas);
            ps.setInt(3, nuevoNumeroCuotas * 30); // plazo aprox. en días
            ps.setInt(4, codigoContrato);

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                null,
                "Error SQL al actualizar contrato reprogramado: " + e.getMessage(),
                "Error",
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
     * Marca un contrato como REFINANCIADO.
     */
    public boolean marcarContratoComoRefinanciado(int codigoContrato) {
        Connection cnx = null;
        PreparedStatement ps = null;

        String sql =
            "UPDATE contrato " +
            "SET estado_contrato = 'REFINANCIADO' " +
            "WHERE codigo_contrato = ?";

        try {
            cnx = ConexionDB.getConnection();
            ps = cnx.prepareStatement(sql);
            ps.setInt(1, codigoContrato);

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                null,
                "Error SQL al marcar contrato refinanciado: " + e.getMessage(),
                "Error",
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
}
