package com.cmhuancayo.creditos.unico;

import java.math.BigDecimal;
import java.util.Date;

public class Cuota {
    private int nroCuota;
    private int codigoContrato;
    private String estadoCuota; // PENDIENTE, PAGADA, VENCIDA
    private Date fechaVencimiento;
    private BigDecimal capital;            // Amortización
    private BigDecimal interes;
    private BigDecimal seguroDegravamen;
    private BigDecimal segurosComisiones;  // NUEVO
    private BigDecimal itf;                // NUEVO
    private int dias;                      // NUEVO
    private BigDecimal montoCuota;
    private BigDecimal saldoCapital;       // Saldo después de la cuota

    // --- Getters y Setters ---

    public int getNroCuota() {
        return nroCuota;
    }

    public void setNroCuota(int nroCuota) {
        this.nroCuota = nroCuota;
    }

    public int getCodigoContrato() {
        return codigoContrato;
    }

    public void setCodigoContrato(int codigoContrato) {
        this.codigoContrato = codigoContrato;
    }

    public String getEstadoCuota() {
        return estadoCuota;
    }

    public void setEstadoCuota(String estadoCuota) {
        this.estadoCuota = estadoCuota;
    }

    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public BigDecimal getCapital() {
        return capital;
    }

    public void setCapital(BigDecimal capital) {
        this.capital = capital;
    }

    public BigDecimal getInteres() {
        return interes;
    }

    public void setInteres(BigDecimal interes) {
        this.interes = interes;
    }

    public BigDecimal getSeguroDegravamen() {
        return seguroDegravamen;
    }

    public void setSeguroDegravamen(BigDecimal seguroDegravamen) {
        this.seguroDegravamen = seguroDegravamen;
    }

    public BigDecimal getSegurosComisiones() {
        return segurosComisiones;
    }

    public void setSegurosComisiones(BigDecimal segurosComisiones) {
        this.segurosComisiones = segurosComisiones;
    }

    public BigDecimal getItf() {
        return itf;
    }

    public void setItf(BigDecimal itf) {
        this.itf = itf;
    }

    public int getDias() {
        return dias;
    }

    public void setDias(int dias) {
        this.dias = dias;
    }

    public BigDecimal getMontoCuota() {
        return montoCuota;
    }

    public void setMontoCuota(BigDecimal montoCuota) {
        this.montoCuota = montoCuota;
    }

    public BigDecimal getSaldoCapital() {
        return saldoCapital;
    }

    public void setSaldoCapital(BigDecimal saldoCapital) {
        this.saldoCapital = saldoCapital;
    }
}
