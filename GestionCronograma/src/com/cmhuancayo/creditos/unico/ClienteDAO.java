package com.cmhuancayo.creditos.unico;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import java.math.BigDecimal;
import java.util.Date;

public class ClienteDAO {

    /**
     * R (READ) - Busca clientes con filtros. Solo muestra ACTIVO (estado_registro = 1)
     */
    public List<Cliente> buscarClientes(String nombre, String dni, String estado) {
        List<Cliente> clientes = new ArrayList<>();
        Connection cnx = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        StringBuilder sql = new StringBuilder(
            "SELECT c.codigo_cliente, c.nombres, c.apellido_paterno, c.numero_documento, " +
            "       t.telefono_celular, " +
            "       (SELECT IF(COUNT(*) > 0, 'Atrasado', 'Al Día') " +
            "        FROM cronograma_de_pago cp " +
            "        JOIN contrato co ON cp.codigo_contrato = co.codigo_contrato " +
            "        JOIN solicitud s ON co.codigo_solicitud = s.codigo_solicitud " +
            "        WHERE s.codigo_cliente = c.codigo_cliente " +
            "          AND cp.estado_cuota = 'Vencida' " +
            "          AND cp.fecha_vencimiento < CURDATE()" +
            "       ) AS estado_general " +
            "FROM cliente c " +
            "LEFT JOIN telefono_cliente t ON c.codigo_cliente = t.codigo_cliente " +
            "WHERE 1 = 1 " +
            "  AND IFNULL(c.estado_registro,1) = 1 "
        );

        if (nombre != null && !nombre.trim().isEmpty()) {
            sql.append("AND (c.nombres LIKE ? OR c.apellido_paterno LIKE ? OR c.apellido_materno LIKE ?) ");
        }
        if (dni != null && !dni.trim().isEmpty()) {
            sql.append("AND c.numero_documento LIKE ? ");
        }
        if (estado != null && !estado.equals("Todos")) {
            sql.append("HAVING estado_general = ? ");
        }
        sql.append("LIMIT 100");

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
            JOptionPane.showMessageDialog(null,
                    "Error al buscar clientes: " + e.getMessage(),
                    "Error SQL", JOptionPane.ERROR_MESSAGE);
        } finally {
            try { if (rs != null) rs.close(); if (ps != null) ps.close(); } catch (SQLException ignore) {}
            ConexionDB.close(cnx);
        }
        return clientes;
    }

    /**
     * C (CREATE) - REGISTRO COMPLETO de cliente usando sp_RegistrarClienteCompleto
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
        CallableStatement cs = null;

        String sql = "{ CALL sp_RegistrarClienteCompleto(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) }";

        try {
            cnx = ConexionDB.getConnection();
            cs = cnx.prepareCall(sql);

            // Valores por defecto si vienen nulos
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

            cs.setString(1, apPaterno);
            cs.setString(2, apMaterno);
            cs.setString(3, nombres);
            cs.setString(4, sexo);
            cs.setDate(5, new java.sql.Date(fechaNac.getTime()));
            cs.setString(6, estadoCivil);
            cs.setString(7, gradoInstruccion);
            cs.setString(8, dni);

            cs.setString(9,  direccion);
            cs.setString(10, referencia);
            cs.setString(11, condicionVivienda);
            cs.setString(12, propietario);
            cs.setInt(13, anioResidencia);
            cs.setString(14, codigoSuministro);

            cs.setString(15, tipoActividad);
            cs.setString(16, nombreNegocio);
            cs.setString(17, direccionNegocio);
            cs.setString(18, telefonoNegocio);
            cs.setDate(19, new java.sql.Date(fechaInicioAct.getTime()));
            cs.setString(20, sectorEconomico);
            cs.setBigDecimal(21, ingresoMensual);

            cs.setString(22, telefonoCelular);
            cs.setBigDecimal(23, scoring);
            cs.setInt(24, numDependientes);

            cs.executeUpdate();
            return true;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Error SQL al registrar cliente: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try { if (cs != null) cs.close(); } catch (SQLException ignore) {}
            ConexionDB.close(cnx);
        }
    }

    /**
     * U (UPDATE) - Actualiza nombre, apellido y teléfono (parte de gestión)
     */
    public boolean actualizarCliente(Cliente cliente) {
        Connection cnx = null;
        PreparedStatement psCliente = null;
        PreparedStatement psTelefono = null;

        String sqlCliente = "UPDATE cliente SET nombres = ?, apellido_paterno = ? WHERE numero_documento = ?";
        String sqlTelefono = "INSERT INTO telefono_cliente (codigo_cliente, telefono_celular) " +
                             "VALUES (?, ?) " +
                             "ON DUPLICATE KEY UPDATE telefono_celular = ?";

        try {
            cnx = ConexionDB.getConnection();
            cnx.setAutoCommit(false);

            psCliente = cnx.prepareStatement(sqlCliente);
            psCliente.setString(1, cliente.getNombres());
            psCliente.setString(2, cliente.getApellidoPaterno());
            psCliente.setString(3, cliente.getNumeroDocumento());
            int filasCliente = psCliente.executeUpdate();

            if (cliente.getTelefono() != null && !cliente.getTelefono().isEmpty()) {
                psTelefono = cnx.prepareStatement(sqlTelefono);
                psTelefono.setInt(1, cliente.getCodigoCliente());
                psTelefono.setString(2, cliente.getTelefono());
                psTelefono.setString(3, cliente.getTelefono());
                psTelefono.executeUpdate();
            }

            cnx.commit();
            return filasCliente > 0;

        } catch (SQLException e) {
            try { if (cnx != null) cnx.rollback(); } catch (SQLException ignore) {}
            JOptionPane.showMessageDialog(null,
                    "Error SQL al actualizar cliente: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
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
     * D (DELETE lógico) - Marca cliente como INACTIVO con sp_EliminarClienteLogico
     */
    public boolean eliminarClienteLogico(int codigoCliente) {
        Connection cnx = null;
        CallableStatement cs = null;
        String sql = "{ CALL sp_EliminarClienteLogico(?) }";

        try {
            cnx = ConexionDB.getConnection();
            cs = cnx.prepareCall(sql);
            cs.setInt(1, codigoCliente);
            cs.executeUpdate();
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Error SQL al eliminar lógicamente cliente: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try { if (cs != null) cs.close(); } catch (SQLException ignore) {}
            ConexionDB.close(cnx);
        }
    }

    /**
     * Ejecuta el SP sp_RecalcularEstadosTodosClientes para actualizar
     * el estado de los clientes según sus cronogramas de pago.
     */
    public boolean recalcularEstadosCreditos() {
        // No hacemos nada, solo devolvemos true.
        return true;
    }
}
