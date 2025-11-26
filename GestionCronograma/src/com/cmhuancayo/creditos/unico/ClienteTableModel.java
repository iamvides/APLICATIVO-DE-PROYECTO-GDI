package com.cmhuancayo.creditos.unico;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * TableModel para mostrar la lista de clientes en la tabla de la interfaz.
 *
 * Columnas:
 * <ol>
 *     <li>Código</li>
 *     <li>Nombres</li>
 *     <li>Ap. Paterno</li>
 *     <li>Nro. Documento</li>
 *     <li>Teléfono</li>
 *     <li>Estado (Al Día / Atrasado)</li>
 * </ol>
 */
public class ClienteTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    /** Lista de clientes mostrados en la tabla. */
    private List<Cliente> clientes;

    /** Nombres de columnas que se muestran en el JTable. */
    private static final String[] COLUMNAS = {
            "Código",
            "Nombres",
            "Ap. Paterno",
            "Nro. Documento",
            "Teléfono",
            "Estado"
    };

    /**
     * Construye el modelo con una lista inicial de clientes.
     * Si la lista es null, se inicializa vacía.
     */
    public ClienteTableModel(List<Cliente> clientes) {
        this.clientes = (clientes != null) ? clientes : new ArrayList<>();
    }

    /**
     * Reemplaza la lista de clientes y notifica a la tabla
     * que los datos han cambiado.
     */
    public void setClientes(List<Cliente> clientes) {
        this.clientes = (clientes != null) ? clientes : new ArrayList<>();
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return (clientes == null) ? 0 : clientes.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNAS.length;
    }

    @Override
    public String getColumnName(int col) {
        return COLUMNAS[col];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (clientes == null || rowIndex < 0 || rowIndex >= clientes.size()) {
            return null;
        }

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

    /**
     * Devuelve el cliente correspondiente a la fila indicada.
     */
    public Cliente getClienteAt(int rowIndex) {
        if (clientes == null || rowIndex < 0 || rowIndex >= clientes.size()) {
            return null;
        }
        return clientes.get(rowIndex);
    }
}
