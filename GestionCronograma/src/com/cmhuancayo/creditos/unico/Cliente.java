package com.cmhuancayo.creditos.unico;

import java.math.BigDecimal;
import java.util.Date;

public class Cliente {
    private int codigoCliente;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String nombres;
    private String numeroDocumento;
    private String telefono; // Lo añadimos para la tabla principal
    private String estadoGeneral; // Campo calculado (Al Día / Atrasado)
    private BigDecimal scoring;

    // --- Getters y Setters ---
    public int getCodigoCliente() { return codigoCliente; }
    public void setCodigoCliente(int codigoCliente) { this.codigoCliente = codigoCliente; }
    public String getApellidoPaterno() { return apellidoPaterno; }
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }
    public String getApellidoMaterno() { return apellidoMaterno; }
    public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEstadoGeneral() { return estadoGeneral; }
    public void setEstadoGeneral(String estadoGeneral) { this.estadoGeneral = estadoGeneral; }
    public BigDecimal getScoring() { return scoring; }
    public void setScoring(BigDecimal scoring) { this.scoring = scoring; }
}