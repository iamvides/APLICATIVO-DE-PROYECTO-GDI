package com.cmhuancayo.creditos.unico;

import java.math.BigDecimal;
import java.util.Date;

public class Movimiento {
    private int codigoMovimiento;
    private int codigoContrato;
    private Integer nroCuotaAfectada; // Usamos Integer para que pueda ser NULL
    private Date fechaMovimiento;
    private String tipoMovimiento; // ENUM: PAGO_CUOTA, AMORTIZACION_CAPITAL, CONDONACION, etc.
    private BigDecimal monto;
    private String usuarioRegistro;

    // --- Getters y Setters ---

    public int getCodigoMovimiento() { return codigoMovimiento; }
    public void setCodigoMovimiento(int codigoMovimiento) { this.codigoMovimiento = codigoMovimiento; }
    
    public int getCodigoContrato() { return codigoContrato; }
    public void setCodigoContrato(int codigoContrato) { this.codigoContrato = codigoContrato; }
    
    public Integer getNroCuotaAfectada() { return nroCuotaAfectada; }
    public void setNroCuotaAfectada(Integer nroCuotaAfectada) { this.nroCuotaAfectada = nroCuotaAfectada; }
    
    public Date getFechaMovimiento() { return fechaMovimiento; }
    public void setFechaMovimiento(Date fechaMovimiento) { this.fechaMovimiento = fechaMovimiento; }
    
    public String getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(String tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }
    
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    
    public String getUsuarioRegistro() { return usuarioRegistro; }
    public void setUsuarioRegistro(String usuarioRegistro) { this.usuarioRegistro = usuarioRegistro; }
}