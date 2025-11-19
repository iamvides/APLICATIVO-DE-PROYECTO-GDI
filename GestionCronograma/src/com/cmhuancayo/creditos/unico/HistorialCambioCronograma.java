package com.cmhuancayo.creditos.unico;

import java.util.Date;

public class HistorialCambioCronograma {

    private int codigoHistorial;
    private int codigoContrato;
    private Date fechaCambio;
    private String tipoCambio;
    private String descripcionCambio;
    private String usuarioRegistro;

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
