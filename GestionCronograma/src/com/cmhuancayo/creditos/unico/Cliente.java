package com.cmhuancayo.creditos.unico;

import java.math.BigDecimal;

/**
 * Representa a un cliente del sistema de créditos.
 *
 * Esta clase es un POJO (solo datos) utilizado en la capa de
 * presentación y en los DAO para transportar información del cliente.
 *
 * Campos principales:
 * <ul>
 *     <li>codigoCliente: identificador interno en la base de datos.</li>
 *     <li>apellidoPaterno, apellidoMaterno, nombres.</li>
 *     <li>numeroDocumento: DNI u otro documento de identidad.</li>
 *     <li>telefono: número de celular principal (opcional).</li>
 *     <li>estadoGeneral: estado crediticio calculado (por ejemplo: "Al Día", "Atrasado").</li>
 *     <li>scoring: puntaje de evaluación del cliente.</li>
 * </ul>
 */
public class Cliente {

    /** Clave primaria en la tabla cliente. */
    private int codigoCliente;

    private String apellidoPaterno;
    private String apellidoMaterno;
    private String nombres;
    private String numeroDocumento;

    /** Teléfono celular principal mostrado en el listado. */
    private String telefono;

    /** Estado calculado del cliente: "Al Día", "Atrasado", etc. */
    private String estadoGeneral;

    /** Puntaje de scoring crediticio. */
    private BigDecimal scoring;

    // ---------------------------------------------------------
    // Getters y Setters
    // ---------------------------------------------------------

    public int getCodigoCliente() {
        return codigoCliente;
    }

    public void setCodigoCliente(int codigoCliente) {
        this.codigoCliente = codigoCliente;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEstadoGeneral() {
        return estadoGeneral;
    }

    public void setEstadoGeneral(String estadoGeneral) {
        this.estadoGeneral = estadoGeneral;
    }

    public BigDecimal getScoring() {
        return scoring;
    }

    public void setScoring(BigDecimal scoring) {
        this.scoring = scoring;
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "codigoCliente=" + codigoCliente +
                ", apellidoPaterno='" + apellidoPaterno + '\'' +
                ", apellidoMaterno='" + apellidoMaterno + '\'' +
                ", nombres='" + nombres + '\'' +
                ", numeroDocumento='" + numeroDocumento + '\'' +
                '}';
    }
}
