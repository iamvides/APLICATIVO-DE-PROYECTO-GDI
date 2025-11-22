package com.cmhuancayo.creditos.unico;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Entidad de dominio que representa un movimiento asociado a un contrato de crédito.
 *
 * Ejemplos de movimientos:
 *  - PAGO_CUOTA: pago normal de una cuota
 *  - AMORTIZACION_CAPITAL: pago extraordinario a capital
 *  - CONDONACION: condonación de intereses, comisiones, etc.
 *
 * Se corresponde con la tabla movimiento_contrato (o la que uses para registrar movimientos).
 */
public class Movimiento {

    /** Clave primaria del movimiento. */
    private int codigoMovimiento;

    /** Contrato al que pertenece el movimiento. */
    private int codigoContrato;

    /**
     * Número de la cuota afectada, si aplica.
     * Puede ser null en movimientos que no se aplican a una cuota específica
     * (por ejemplo, ciertos ajustes generales al contrato).
     */
    private Integer nroCuotaAfectada;

    /** Fecha y hora en que se registró el movimiento. */
    private Date fechaMovimiento;

    /**
     * Tipo de movimiento (PAGO_CUOTA, AMORTIZACION_CAPITAL, CONDONACION, etc.).
     * En BD normalmente se maneja como VARCHAR o ENUM.
     */
    private String tipoMovimiento;

    /** Monto del movimiento (importe total aplicado). */
    private BigDecimal monto;

    /** Usuario del sistema que registró el movimiento. */
    private String usuarioRegistro;

    // --- Getters y Setters ---

    public int getCodigoMovimiento() {
        return codigoMovimiento;
    }

    public void setCodigoMovimiento(int codigoMovimiento) {
        this.codigoMovimiento = codigoMovimiento;
    }

    public int getCodigoContrato() {
        return codigoContrato;
    }

    public void setCodigoContrato(int codigoContrato) {
        this.codigoContrato = codigoContrato;
    }

    public Integer getNroCuotaAfectada() {
        return nroCuotaAfectada;
    }

    public void setNroCuotaAfectada(Integer nroCuotaAfectada) {
        this.nroCuotaAfectada = nroCuotaAfectada;
    }

    public Date getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(Date fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getUsuarioRegistro() {
        return usuarioRegistro;
    }

    public void setUsuarioRegistro(String usuarioRegistro) {
        this.usuarioRegistro = usuarioRegistro;
    }
}
