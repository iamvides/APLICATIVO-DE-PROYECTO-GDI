package com.cmhuancayo.creditos.unico;

import java.util.Date;

/**
 * Representa un registro de cambio realizado sobre el cronograma de pago
 * de un contrato.
 *
 * Esta clase se corresponde con la tabla:
 *   historial_cambios_cronograma
 * en la base de datos PostgreSQL.
 *
 * Cada registro indica:
 *  - Qué contrato se modificó.
 *  - Cuándo se realizó el cambio.
 *  - Qué tipo de cambio fue (reprogramación, amortización, refinanciación, etc.).
 *  - Una descripción libre del cambio.
 *  - El usuario que realizó o registró el cambio.
 */
public class HistorialCambioCronograma {

    /** Clave primaria del historial (codigo_historial). */
    private int codigoHistorial;

    /** Contrato al que pertenece este cambio (codigo_contrato). */
    private int codigoContrato;

    /** Fecha y hora en que se realizó el cambio (fecha_cambio). */
    private Date fechaCambio;

    /**
     * Tipo de cambio realizado sobre el cronograma.
     * Ejemplos: "REPROGRAMACION_PLAZO", "AMORTIZACION", "CAMBIO_TASA", etc.
     */
    private String tipoCambio;

    /** Descripción detallada del cambio aplicado al cronograma. */
    private String descripcionCambio;

    /** Usuario que registró el cambio (usuario_registro). */
    private String usuarioRegistro;

    // ================== Getters y Setters ==================

    public int getCodigoHistorial() {
        return codigoHistorial;
    }

    public void setCodigoHistorial(int codigoHistorial) {
        this.codigoHistorial = codigoHistorial;
    }

    public int getCodigoContrato() {
        return codigoContrato;
    }

    public void setCodigoContrato(int codigoContrato) {
        this.codigoContrato = codigoContrato;
    }

    public Date getFechaCambio() {
        return fechaCambio;
    }

    public void setFechaCambio(Date fechaCambio) {
        this.fechaCambio = fechaCambio;
    }

    public String getTipoCambio() {
        return tipoCambio;
    }

    public void setTipoCambio(String tipoCambio) {
        this.tipoCambio = tipoCambio;
    }

    public String getDescripcionCambio() {
        return descripcionCambio;
    }

    public void setDescripcionCambio(String descripcionCambio) {
        this.descripcionCambio = descripcionCambio;
    }

    public String getUsuarioRegistro() {
        return usuarioRegistro;
    }

    public void setUsuarioRegistro(String usuarioRegistro) {
        this.usuarioRegistro = usuarioRegistro;
    }
}
