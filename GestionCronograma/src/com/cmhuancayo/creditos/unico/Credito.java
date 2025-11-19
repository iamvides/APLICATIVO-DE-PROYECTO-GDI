package com.cmhuancayo.creditos.unico;

import java.math.BigDecimal;
import java.util.Date;

public class Credito {
    private int codigoContrato;
    private int codigoSolicitud;
    private int codigoCliente;
    private BigDecimal montoDesembolso;
    private BigDecimal tasaInteresCompensatorio; // TEA
    private int numeroCuotas;
    private Date fechaDesembolso;
    private BigDecimal saldoCapital; // Saldo actual

    // --- Getters y Setters ---
    public int getCodigoContrato() { return codigoContrato; }
    public void setCodigoContrato(int codigoContrato) { this.codigoContrato = codigoContrato; }
    public int getCodigoSolicitud() { return codigoSolicitud; }
    public void setCodigoSolicitud(int codigoSolicitud) { this.codigoSolicitud = codigoSolicitud; }
    public int getCodigoCliente() { return codigoCliente; }
    public void setCodigoCliente(int codigoCliente) { this.codigoCliente = codigoCliente; }
    public BigDecimal getMontoDesembolso() { return montoDesembolso; }
    public void setMontoDesembolso(BigDecimal montoDesembolso) { this.montoDesembolso = montoDesembolso; }
    public BigDecimal getTasaInteresCompensatorio() { return tasaInteresCompensatorio; }
    public void setTasaInteresCompensatorio(BigDecimal tasaInteresCompensatorio) { this.tasaInteresCompensatorio = tasaInteresCompensatorio; }
    public int getNumeroCuotas() { return numeroCuotas; }
    public void setNumeroCuotas(int numeroCuotas) { this.numeroCuotas = numeroCuotas; }
    public Date getFechaDesembolso() { return fechaDesembolso; }
    public void setFechaDesembolso(Date fechaDesembolso) { this.fechaDesembolso = fechaDesembolso; }
    public BigDecimal getSaldoCapital() { return saldoCapital; }
    public void setSaldoCapital(BigDecimal saldoCapital) { this.saldoCapital = saldoCapital; }
    private String estadoContrato;

    public String getEstadoContrato() {
        return estadoContrato;
    }

    public void setEstadoContrato(String estadoContrato) {
        this.estadoContrato = estadoContrato;
    }

}