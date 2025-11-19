package com.cmhuancayo.creditos.unico;

import javax.swing.table.AbstractTableModel;

import java.util.List;

public class ClienteTableModel extends AbstractTableModel {
    
    private List<Cliente> clientes;
    private final String[] COLUMNAS = {"Código", "Nombres", "Ap. Paterno", "Nro. Documento", "Teléfono", "Estado"};

    public ClienteTableModel(List<Cliente> clientes) {
        this.clientes = clientes;
    }
    
    public void setClientes(List<Cliente> clientes) {
        this.clientes = clientes;
        fireTableDataChanged();
    }

    @Override public int getRowCount() { return clientes.size(); }
    @Override public int getColumnCount() { return COLUMNAS.length; }
    @Override public String getColumnName(int col) { return COLUMNAS[col]; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Cliente c = clientes.get(rowIndex);
        switch (columnIndex) {
            case 0: return c.getCodigoCliente();
            case 1: return c.getNombres();
            case 2: return c.getApellidoPaterno();
            case 3: return c.getNumeroDocumento();
            case 4: return c.getTelefono();
            case 5: return c.getEstadoGeneral();
            default: return null;
        }
    }
    
    public Cliente getClienteAt(int rowIndex) {
        return clientes.get(rowIndex);
    }
}