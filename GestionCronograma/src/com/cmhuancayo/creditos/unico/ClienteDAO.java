package com.cmhuancayo.creditos.unico;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * DAO de clientes.
 *
 * Encargado de:
 * <ul>
 *     <li>Buscar clientes con filtros.</li>
 *     <li>Registrar un cliente completo usando el procedimiento almacenado.</li>
 *     <li>Actualizar datos básicos del cliente (nombres / teléfono).</li>
 *     <li>Eliminar lógicamente un cliente.</li>
 * </ul>
 *
 * Versión adaptada para PostgreSQL.
 */
public class ClienteDAO {

    /**
     * Busca clientes aplicando filtros por nombre, DNI y estado de sus créditos.
     */
    public List<Cliente> buscarClientes(String nombre, String dni, String estado) {
        List<Cliente> clientes = new ArrayList<>();
        Connection cnx = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        // Subconsulta que calcula el estado general del cliente.
        String estadoSubquery =
            " (SELECT CASE WHEN COUNT(*) > 0 THEN 'Atrasado' ELSE 'Al Día' END " +
            "    FROM cronograma_de_pago cp " +
            "    JOIN contrato co ON cp.codigo_contrato = co.codigo_contrato " +
            "    JOIN solicitud s ON co.codigo_solicitud = s.codigo_solicitud " +
            "   WHERE s.codigo_cliente = c.codigo_cliente " +
            "     AND cp.estado_cuota = 'Vencida' " +
            "     AND cp.fecha_vencimiento < CURRENT_DATE" +
            "  ) ";

        StringBuilder sql = new StringBuilder(
            "SELECT " +
            "    c.codigo_cliente, " +
            "    c.nombres, " +
            "    c.apellido_paterno, " +
            "    c.numero_documento, " +
            "    t.telefono_celular, " +
            estadoSubquery + " AS estado_general " +
            "FROM cliente c " +
            "LEFT JOIN telefono_cliente t ON c.codigo_cliente = t.codigo_cliente " +
            "WHERE 1 = 1 " +
            "  AND COALESCE(c.estado_registro, 1) = 1 "
        );

        // Filtro por nombre / apellidos
        if (nombre != null && !nombre.trim().isEmpty()) {
            sql.append(" AND (c.nombres ILIKE ? " +
                       "      OR c.apellido_paterno ILIKE ? " +
                       "      OR COALESCE(c.apellido_materno, '') ILIKE ?) ");
        }

        // Filtro por DNI
        if (dni != null && !dni.trim().isEmpty()) {
            sql.append(" AND c.numero_documento LIKE ? ");
        }

        // Filtro por estado ("Al Día" / "Atrasado")
        if (estado != null && !estado.equals("Todos")) {
            sql.append(" AND ").append(estadoSubquery).append(" = ? ");
        }

        sql.append(" ORDER BY c.codigo_cliente LIMIT 100");

        try {
            cnx = ConexionDB.getConnection();
            ps = cnx.prepareStatement(sql.toString());

            int idx = 1;

            if (nombre != null && !nombre.trim().isEmpty()) {
                String like = "%" + nombre.trim() + "%";
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
            }

            if (dni != null && !dni.trim().isEmpty()) {
                ps.setString(idx++, "%" + dni.trim() + "%");
            }

            if (estado != null && !estado.equals("Todos")) {
                ps.setString(idx++, estado);
            }

            rs = ps.executeQuery();
            while (rs.next()) {
                Cliente c = new Cliente();
                c.setCodigoCliente(rs.getInt("codigo_cliente"));
                c.setNombres(rs.getString("nombres"));
                c.setApellidoPaterno(rs.getString("apellido_paterno"));
                c.setNumeroDocumento(rs.getString("numero_documento"));
                c.setTelefono(rs.getString("telefono_celular"));
                c.setEstadoGeneral(rs.getString("estado_general"));
                clientes.add(c);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                null,
                "Error al buscar clientes: " + e.getMessage(),
                "Error SQL",
                JOptionPane.ERROR_MESSAGE
            );
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException ignore) {}
            ConexionDB.close(cnx);
        }
        return clientes;
    }

    /**
     * Registra un cliente completo usando el procedimiento
     * sp_registrarclientecompleto (PostgreSQL PROCEDURE).
     */
    public boolean registrarClienteCompleto(
            String apPaterno,
            String apMaterno,
            String nombres,
            String sexo,
            Date fechaNac,
            String estadoCivil,
            String gradoInstruccion,
            String dni,
            String direccion,
            String referencia,
            String condicionVivienda,
            String propietario,
            int anioResidencia,
            String codigoSuministro,
            String tipoActividad,
            String nombreNegocio,
            String direccionNegocio,
            String telefonoNegocio,
            Date fechaInicioAct,
            String sectorEconomico,
            BigDecimal ingresoMensual,
            String telefonoCelular,
            BigDecimal scoring,
            int numDependientes
    ) {
        Connection cnx = null;
        PreparedStatement ps = null;

        // IMPORTANTE: usar CALL directo para que PostgreSQL lo trate como PROCEDURE
        String sql = "CALL sp_registrarclientecompleto(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try {
            cnx = ConexionDB.getConnection();
            ps = cnx.prepareStatement(sql);

            if (fechaNac == null) {
                fechaNac = new Date();
            }
            if (fechaInicioAct == null) {
                fechaInicioAct = new Date();
            }
            if (ingresoMensual == null) {
                ingresoMensual = BigDecimal.ZERO;
            }
            if (scoring == null) {
                scoring = new BigDecimal("80.00");
            }

            ps.setString(1, apPaterno);
            ps.setString(2, apMaterno);
            ps.setString(3, nombres);
            ps.setString(4, sexo);
            ps.setDate(5, new java.sql.Date(fechaNac.getTime()));
            ps.setString(6, estadoCivil);
            ps.setString(7, gradoInstruccion);
            ps.setString(8, dni);

            ps.setString(9,  direccion);
            ps.setString(10, referencia);
            ps.setString(11, condicionVivienda);
            ps.setString(12, propietario);
            ps.setInt(13, anioResidencia);
            ps.setString(14, codigoSuministro);

            ps.setString(15, tipoActividad);
            ps.setString(16, nombreNegocio);
            ps.setString(17, direccionNegocio);
            ps.setString(18, telefonoNegocio);
            ps.setDate(19, new java.sql.Date(fechaInicioAct.getTime()));
            ps.setString(20, sectorEconomico);
            ps.setBigDecimal(21, ingresoMensual);

            ps.setString(22, telefonoCelular);
            ps.setBigDecimal(23, scoring);
            ps.setInt(24, numDependientes);

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                null,
                "Error SQL al registrar cliente: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException ignore) {}
            ConexionDB.close(cnx);
        }
    }

    /**
     * Actualiza nombres/apellido del cliente y su teléfono principal.
     */
    public boolean actualizarCliente(Cliente cliente) {
        Connection cnx = null;
        PreparedStatement psCliente = null;
        PreparedStatement psTelefono = null;

        String sqlCliente =
            "UPDATE cliente " +
            "SET nombres = ?, apellido_paterno = ? " +
            "WHERE numero_documento = ?";

        String sqlTelefonoUpdate =
            "UPDATE telefono_cliente " +
            "SET telefono_celular = ? " +
            "WHERE codigo_cliente = ?";

        String sqlTelefonoInsert =
            "INSERT INTO telefono_cliente (codigo_cliente, telefono_celular) " +
            "VALUES (?, ?)";

        try {
            cnx = ConexionDB.getConnection();
            cnx.setAutoCommit(false);

            psCliente = cnx.prepareStatement(sqlCliente);
            psCliente.setString(1, cliente.getNombres());
            psCliente.setString(2, cliente.getApellidoPaterno());
            psCliente.setString(3, cliente.getNumeroDocumento());
            int filasCliente = psCliente.executeUpdate();

            if (cliente.getTelefono() != null && !cliente.getTelefono().isEmpty()) {

                psTelefono = cnx.prepareStatement(sqlTelefonoUpdate);
                psTelefono.setString(1, cliente.getTelefono());
                psTelefono.setInt(2, cliente.getCodigoCliente());
                int filasTel = psTelefono.executeUpdate();
                psTelefono.close();

                if (filasTel == 0) {
                    psTelefono = cnx.prepareStatement(sqlTelefonoInsert);
                    psTelefono.setInt(1, cliente.getCodigoCliente());
                    psTelefono.setString(2, cliente.getTelefono());
                    psTelefono.executeUpdate();
                }
            }

            cnx.commit();
            return filasCliente > 0;

        } catch (SQLException e) {
            try {
                if (cnx != null) cnx.rollback();
            } catch (SQLException ignore) {}

            JOptionPane.showMessageDialog(
                null,
                "Error SQL al actualizar cliente: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;

        } finally {
            try {
                if (psCliente != null) psCliente.close();
                if (psTelefono != null) psTelefono.close();
                if (cnx != null) cnx.setAutoCommit(true);
            } catch (SQLException ignore) {}
            ConexionDB.close(cnx);
        }
    }

    /**
     * Elimina lógicamente un cliente llamando al procedure sp_eliminarclientelogico.
     */
    public boolean eliminarClienteLogico(int codigoCliente) {
        Connection cnx = null;
        PreparedStatement ps = null;
        String sql = "CALL sp_eliminarclientelogico(?)";

        try {
            cnx = ConexionDB.getConnection();
            ps = cnx.prepareStatement(sql);
            ps.setInt(1, codigoCliente);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                null,
                "Error SQL al eliminar lógicamente cliente: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;

        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException ignore) {}
            ConexionDB.close(cnx);
        }
    }

    public boolean recalcularEstadosCreditos() {
        return true;
    }
}
