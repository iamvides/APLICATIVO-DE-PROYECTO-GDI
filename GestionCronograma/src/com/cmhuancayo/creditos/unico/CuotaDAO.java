package com.cmhuancayo.creditos.unico;

import javax.swing.JOptionPane;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/**
 * DAO de cuotas / cronograma de pago.
 * Versión adaptada para PostgreSQL.
 */
public class CuotaDAO {

    /**
     * Lista todas las cuotas (cronograma) de un contrato.
     */
    public List<Cuota> listarPorContrato(int codigoContrato) {
        List<Cuota> cronograma = new ArrayList<>();
        Connection cnx = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sql =
                "SELECT nro_cuota, codigo_contrato, estado_cuota, fecha_vencimiento, " +
                "       capital, interes, seguro_degravamen, seguros_comisiones, itf, " +
                "       monto_cuota, dias, saldo_capital " +
                "FROM cronograma_de_pago " +
                "WHERE codigo_contrato = ? " +
                "ORDER BY nro_cuota";

        try {
            cnx = ConexionDB.getConnection();
            ps = cnx.prepareStatement(sql);
            ps.setInt(1, codigoContrato);
            rs = ps.executeQuery();

            while (rs.next()) {
                Cuota c = new Cuota();
                c.setNroCuota(rs.getInt("nro_cuota"));
                c.setCodigoContrato(rs.getInt("codigo_contrato"));
                c.setEstadoCuota(rs.getString("estado_cuota"));
                c.setFechaVencimiento(rs.getDate("fecha_vencimiento"));
                c.setCapital(rs.getBigDecimal("capital"));
                c.setInteres(rs.getBigDecimal("interes"));
                c.setSeguroDegravamen(rs.getBigDecimal("seguro_degravamen"));
                c.setSegurosComisiones(rs.getBigDecimal("seguros_comisiones"));
                c.setItf(rs.getBigDecimal("itf"));
                c.setMontoCuota(rs.getBigDecimal("monto_cuota"));
                c.setDias(rs.getInt("dias"));
                try {
                    c.setSaldoCapital(rs.getBigDecimal("saldo_capital"));
                } catch (SQLException ex) {
                    c.setSaldoCapital(BigDecimal.ZERO);
                }
                cronograma.add(c);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Error al listar cronograma: " + e.getMessage(),
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

        return cronograma;
    }

    /**
     * Llama al procedimiento sp_registrar_pago_cuota en PostgreSQL.
     */
    public boolean registrarPagoCuota(int codigoContrato, int nroCuota, BigDecimal monto, String usuario) {
        Connection cnx = null;
        CallableStatement cs = null;

        // IMPORTANTE: sin llaves {} para que se ejecute como CALL a un PROCEDURE
        String sql = "CALL sp_registrar_pago_cuota(?, ?, ?, ?)";

        try {
            cnx = ConexionDB.getConnection();
            cs = cnx.prepareCall(sql);
            cs.setInt(1, codigoContrato);
            cs.setInt(2, nroCuota);
            cs.setBigDecimal(3, monto);
            cs.setString(4, usuario);

            cs.execute();
            return true;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Error al registrar pago de cuota: " + e.getMessage(),
                    "Error SQL",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        } finally {
            try {
                if (cs != null) cs.close();
            } catch (SQLException ignored) {}
            ConexionDB.close(cnx);
        }
    }

    /**
     * Actualiza el estado de una cuota en cronograma_de_pago.
     */
    public boolean actualizarEstadoCuota(int codigoContrato, int nroCuota, String nuevoEstado) {
        Connection cnx = null;
        PreparedStatement ps = null;

        String sql = "UPDATE cronograma_de_pago " +
                     "SET estado_cuota = ? " +
                     "WHERE codigo_contrato = ? AND nro_cuota = ?";

        try {
            cnx = ConexionDB.getConnection();
            ps = cnx.prepareStatement(sql);
            ps.setString(1, nuevoEstado);
            ps.setInt(2, codigoContrato);
            ps.setInt(3, nroCuota);

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Error al actualizar estado de cuota: " + e.getMessage(),
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
     * Pago parcial: llama al procedimiento sp_registrarpagoparcial en PostgreSQL.
     */
    public boolean registrarPagoParcialCuota(int codigoContrato,
                                             int nroCuotaInicio,
                                             BigDecimal montoPago,
                                             String usuario) {
        Connection cnx = null;
        CallableStatement cs = null;

        // También sin llaves {}, es un PROCEDURE
        String sql = "CALL sp_registrarpagoparcial(?, ?, ?, ?)";

        try {
            cnx = ConexionDB.getConnection();
            cs = cnx.prepareCall(sql);
            cs.setInt(1, codigoContrato);
            cs.setInt(2, nroCuotaInicio);
            cs.setBigDecimal(3, montoPago);
            cs.setString(4, usuario);

            cs.execute();
            return true;

        } catch (SQLException e) {
            // En el script usamos ERRCODE '45000' para validaciones de negocio
            if ("45000".equals(e.getSQLState()) || "P0001".equals(e.getSQLState())) {
                JOptionPane.showMessageDialog(
                        null,
                        e.getMessage(),
                        "Validación",
                        JOptionPane.WARNING_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Error SQL al registrar pago parcial: " + e.getMessage(),
                        "Error SQL",
                        JOptionPane.ERROR_MESSAGE
                );
            }
            return false;
        } finally {
            try {
                if (cs != null) cs.close();
            } catch (SQLException ignored) {}
            ConexionDB.close(cnx);
        }
    }

    /**
     * Reemplaza por completo el cronograma de un contrato.
     */
    public boolean actualizarCronogramaCompleto(int codigoContrato, List<Cuota> nuevoCronograma) {
        Connection cnx = null;
        PreparedStatement psDelete = null;
        PreparedStatement psInsert = null;

        String sqlDelete = "DELETE FROM cronograma_de_pago WHERE codigo_contrato = ?";
        String sqlInsert = "INSERT INTO cronograma_de_pago " +
                "(nro_cuota, codigo_contrato, estado_cuota, fecha_vencimiento, " +
                " capital, interes, seguro_degravamen, seguros_comisiones, itf, " +
                " monto_cuota, dias, saldo_capital) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            cnx = ConexionDB.getConnection();
            cnx.setAutoCommit(false);

            psDelete = cnx.prepareStatement(sqlDelete);
            psDelete.setInt(1, codigoContrato);
            psDelete.executeUpdate();

            psInsert = cnx.prepareStatement(sqlInsert);
            for (Cuota c : nuevoCronograma) {
                psInsert.setInt(1, c.getNroCuota());
                psInsert.setInt(2, codigoContrato);
                psInsert.setString(3, c.getEstadoCuota());
                psInsert.setDate(4, new java.sql.Date(c.getFechaVencimiento().getTime()));
                psInsert.setBigDecimal(5, c.getCapital());
                psInsert.setBigDecimal(6, c.getInteres());
                psInsert.setBigDecimal(7, c.getSeguroDegravamen() != null ? c.getSeguroDegravamen() : BigDecimal.ZERO);
                psInsert.setBigDecimal(8, c.getSegurosComisiones() != null ? c.getSegurosComisiones() : BigDecimal.ZERO);
                psInsert.setBigDecimal(9, c.getItf() != null ? c.getItf() : BigDecimal.ZERO);
                psInsert.setBigDecimal(10, c.getMontoCuota());
                psInsert.setInt(11, c.getDias());
                psInsert.setBigDecimal(12, c.getSaldoCapital() != null ? c.getSaldoCapital() : BigDecimal.ZERO);
                psInsert.addBatch();
            }

            psInsert.executeBatch();
            cnx.commit();
            return true;

        } catch (SQLException e) {
            try {
                if (cnx != null) cnx.rollback();
            } catch (SQLException ignored) {}
            JOptionPane.showMessageDialog(
                    null,
                    "Error al actualizar cronograma: " + e.getMessage(),
                    "Error SQL",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        } finally {
            try {
                if (psDelete != null) psDelete.close();
                if (psInsert != null) psInsert.close();
                if (cnx != null) cnx.setAutoCommit(true);
            } catch (SQLException ignored) {}
            ConexionDB.close(cnx);
        }
    }

    /**
     * Archiva el cronograma actual antes de amortización / reprogramación / refinanciación.
     */
    public boolean archivarCronogramaActual(int codigoContrato,
                                            String tipoCambio,
                                            String descripcion,
                                            String usuario) {
        Connection cnx = null;
        CallableStatement cs = null;

        // También es PROCEDURE en PostgreSQL
        String sql = "CALL sp_archivarcronogramaactual(?, ?, ?, ?)";

        try {
            cnx = ConexionDB.getConnection();
            cs = cnx.prepareCall(sql);
            cs.setInt(1, codigoContrato);
            cs.setString(2, tipoCambio);
            cs.setString(3, descripcion);
            cs.setString(4, usuario);

            cs.execute();
            return true;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Error al archivar cronograma: " + e.getMessage(),
                    "Error SQL",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        } finally {
            try {
                if (cs != null) cs.close();
            } catch (SQLException ignored) {}
            ConexionDB.close(cnx);
        }
    }
}
