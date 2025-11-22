package com.cmhuancayo.creditos.unico;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * TableModel para mostrar el cronograma de pagos (lista de Cuota)
 * en la tabla de la interfaz.
 *
 * Columnas:
 *  0 - N° de cuota (formato 001, 002, ...)
 *  1 - Fecha de vencimiento
 *  2 - Capital
 *  3 - Interés
 *  4 - Seguro de desgravamen
 *  5 - Monto total de la cuota
 *  6 - Saldo de capital después de la cuota
 *  7 - Estado de la cuota (Pendiente, Pagada, Vencida, Parcial, etc.)
 */
public class CuotaTableModel extends AbstractTableModel {

    private List<Cuota> cuotas;

    // Encabezados de columnas
    private static final String[] COLUMNAS = {
            "N°",
            "Fecha venc.",
            "Capital",
            "Interés",
            "Seg. degrav.",
            "Monto cuota",
            "Saldo capital",
            "Estado"
    };

    public CuotaTableModel(List<Cuota> cuotas) {
        this.cuotas = cuotas;
    }

    /**
     * Reemplaza la lista de cuotas y refresca la tabla.
     */
    public void setCuotas(List<Cuota> cuotas) {
        this.cuotas = cuotas;
        fireTableDataChanged();
    }

    /**
     * Devuelve la lista interna de cuotas (por si se necesita externamente).
     */
    public List<Cuota> getCuotas() {
        return cuotas;
    }

    @Override
    public int getRowCount() {
        return (cuotas == null) ? 0 : cuotas.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNAS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNAS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (cuotas == null || rowIndex < 0 || rowIndex >= cuotas.size()) {
            return null;
        }
        Cuota c = cuotas.get(rowIndex);

        switch (columnIndex) {
            case 0: // N°
                return String.format("%03d", c.getNroCuota());
            case 1: // Fecha venc.
                return formatFecha(c.getFechaVencimiento());
            case 2: // Capital
                return formatMoneda(c.getCapital());
            case 3: // Interés
                return formatMoneda(c.getInteres());
            case 4: // Seguro de desgravamen
                return formatMoneda(c.getSeguroDegravamen());
            case 5: // Monto de la cuota
                return formatMoneda(c.getMontoCuota());
            case 6: // Saldo capital
                return formatMoneda(c.getSaldoCapital());
            case 7: // Estado
                return c.getEstadoCuota();
            default:
                return null;
        }
    }

    /**
     * Da formato de moneda en soles, con 2 decimales y separador de miles.
     * Ejemplo: S/ 1,234.56
     */
    private String formatMoneda(BigDecimal valor) {
        if (valor == null) return "S/ 0.00";
        return String.format("S/ %,.2f", valor);
    }

    /**
     * Da formato a la fecha en yyyy-MM-dd.
     */
    private String formatFecha(Date fecha) {
        if (fecha == null) return "-";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(fecha);
    }

    /**
     * Devuelve la cuota correspondiente a la fila seleccionada.
     */
    public Cuota getCuotaAt(int rowIndex) {
        if (cuotas == null || rowIndex < 0 || rowIndex >= cuotas.size()) {
            return null;
        }
        return cuotas.get(rowIndex);
    }
}
