package com.cmhuancayo.creditos.unico;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Representa un crédito (contrato) de un cliente.
 *
 * Esta clase se usa como DTO/POJO para transportar la información
 * del contrato entre la capa DAO y la interfaz gráfica.
 *
 * Campos principales:
 * <ul>
 *     <li>codigoContrato: identificador del contrato en BD.</li>
 *     <li>codigoSolicitud: solicitud que originó el contrato.</li>
 *     <li>codigoCliente: titular del crédito.</li>
 *     <li>montoDesembolso: monto de capital desembolsado.</li>
 *     <li>tasaInteresCompensatorio: TEA del crédito.</li>
 *     <li>numeroCuotas: cantidad de cuotas del cronograma.</li>
 *     <li>fechaDesembolso: fecha de inicio/vigencia del crédito.</li>
 *     <li>saldoCapital: saldo de capital pendiente (calculado).</li>
 *     <li>estadoContrato: estado del contrato (VIGENTE, CANCELADO, REFINANCIADO, etc.).</li>
 * </ul>
 */
public class Credito {

    /** Clave primaria de la tabla contrato. */
    private int codigoContrato;

    /** Relación con la tabla solicitud. */
    private int codigoSolicitud;

    /** Relación con la tabla cliente. */
    private int codigoCliente;

    /** Monto de capital desembolsado. */
    private BigDecimal montoDesembolso;

    /** Tasa de interés compensatorio anual (TEA). */
    private BigDecimal tasaInteresCompensatorio;

    /** Número total de cuotas del crédito. */
    private int numeroCuotas;

    /** Fecha de desembolso / vigencia del contrato. */
    private Date fechaDesembolso;

    /** Saldo de capital pendiente (se usa en cálculos y pantallas). */
    private BigDecimal saldoCapital;

    /** Estado del contrato: VIGENTE, CANCELADO, REFINANCIADO, etc. */
    private String estadoContrato;

    // ---------------------------------------------------------
    // Getters y Setters
    // ---------------------------------------------------------

    public int getCodigoContrato() {
        return codigoContrato;
    }

    public void setCodigoContrato(int codigoContrato) {
        this.codigoContrato = codigoContrato;
    }

    public int getCodigoSolicitud() {
        return codigoSolicitud;
    }

    public void setCodigoSolicitud(int codigoSolicitud) {
        this.codigoSolicitud = codigoSolicitud;
    }

    public int getCodigoCliente() {
        return codigoCliente;
    }

    public void setCodigoCliente(int codigoCliente) {
        this.codigoCliente = codigoCliente;
    }

    public BigDecimal getMontoDesembolso() {
        return montoDesembolso;
    }

    public void setMontoDesembolso(BigDecimal montoDesembolso) {
        this.montoDesembolso = montoDesembolso;
    }

    public BigDecimal getTasaInteresCompensatorio() {
        return tasaInteresCompensatorio;
    }

    public void setTasaInteresCompensatorio(BigDecimal tasaInteresCompensatorio) {
        this.tasaInteresCompensatorio = tasaInteresCompensatorio;
    }

    public int getNumeroCuotas() {
        return numeroCuotas;
    }

    public void setNumeroCuotas(int numeroCuotas) {
        this.numeroCuotas = numeroCuotas;
    }

    public Date getFechaDesembolso() {
        return fechaDesembolso;
    }

    public void setFechaDesembolso(Date fechaDesembolso) {
        this.fechaDesembolso = fechaDesembolso;
    }

    public BigDecimal getSaldoCapital() {
        return saldoCapital;
    }

    public void setSaldoCapital(BigDecimal saldoCapital) {
        this.saldoCapital = saldoCapital;
    }

    public String getEstadoContrato() {
        return estadoContrato;
    }

    public void setEstadoContrato(String estadoContrato) {
        this.estadoContrato = estadoContrato;
    }

    @Override
    public String toString() {
        return "Credito{" +
                "codigoContrato=" + codigoContrato +
                ", codigoSolicitud=" + codigoSolicitud +
                ", codigoCliente=" + codigoCliente +
                ", montoDesembolso=" + montoDesembolso +
                ", numeroCuotas=" + numeroCuotas +
                ", estadoContrato='" + estadoContrato + '\'' +
                '}';
    }
}
