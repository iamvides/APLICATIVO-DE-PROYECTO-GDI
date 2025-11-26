package com.cmhuancayo.creditos.unico;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Representa una cuota del cronograma de pagos de un contrato.
 *
 * Campos principales:
 * <ul>
 *     <li>nroCuota: número de cuota dentro del contrato (1..N).</li>
 *     <li>codigoContrato: contrato al que pertenece la cuota.</li>
 *     <li>estadoCuota: estado (PENDIENTE, PAGADA, VENCIDA, PARCIAL, etc.).</li>
 *     <li>fechaVencimiento: fecha límite de pago.</li>
 *     <li>capital: parte de amortización de capital.</li>
 *     <li>interes: parte de interés de la cuota.</li>
 *     <li>seguroDegravamen: seguro asociado a la cuota.</li>
 *     <li>segurosComisiones: otros seguros/comisiones.</li>
 *     <li>itf: impuesto a las transacciones financieras asociado.</li>
 *     <li>dias: número de días del período (para cálculo de interés).</li>
 *     <li>montoCuota: importe total a pagar en la cuota.</li>
 *     <li>saldoCapital: saldo de capital después de aplicar la cuota.</li>
 * </ul>
 *
 * Se usa como DTO/POJO entre la base de datos, la lógica de cálculo
 * (CalculadoraCreditoService) y la interfaz gráfica.
 */
public class Cuota {

    /** Número de cuota (1..N) dentro del contrato. */
    private int nroCuota;

    /** Código del contrato al que pertenece la cuota. */
    private int codigoContrato;

    /** Estado de la cuota: PENDIENTE, PAGADA, VENCIDA, PARCIAL, etc. */
    private String estadoCuota;

    /** Fecha de vencimiento de la cuota. */
    private Date fechaVencimiento;

    /** Parte de amortización de capital correspondiente a esta cuota. */
    private BigDecimal capital;

    /** Parte de interés correspondiente a esta cuota. */
    private BigDecimal interes;

    /** Monto de seguro de desgravamen. */
    private BigDecimal seguroDegravamen;

    /** Otros seguros y comisiones asociados a la cuota. */
    private BigDecimal segurosComisiones;

    /** ITF aplicado sobre la cuota. */
    private BigDecimal itf;

    /** Número de días del período (se usa en algunos cálculos de interés). */
    private int dias;

    /** Monto total de la cuota (capital + interés + seguros + ITF). */
    private BigDecimal montoCuota;

    /** Saldo de capital pendiente después de aplicar esta cuota. */
    private BigDecimal saldoCapital;

    // ---------------------------------------------------------
    // Getters y Setters
    // ---------------------------------------------------------

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

    @Override
    public String toString() {
        return "Cuota{" +
                "nroCuota=" + nroCuota +
                ", codigoContrato=" + codigoContrato +
                ", estadoCuota='" + estadoCuota + '\'' +
                ", fechaVencimiento=" + fechaVencimiento +
                ", capital=" + capital +
                ", interes=" + interes +
                ", montoCuota=" + montoCuota +
                ", saldoCapital=" + saldoCapital +
                '}';
    }
}
