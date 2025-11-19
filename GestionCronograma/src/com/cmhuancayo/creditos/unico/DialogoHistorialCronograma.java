package com.cmhuancayo.creditos.unico;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Date; // Importar Date

public class DialogoHistorialCronograma extends JDialog {

    private final int codigoContrato;
    private final HistorialCronogramaDAO historialDAO;

    private JTable tabla;
    private HistorialTableModel tableModel;

    public DialogoHistorialCronograma(Window owner, int codigoContrato, HistorialCronogramaDAO dao) {
        super(owner, "Historial de Cambios - Contrato " + codigoContrato, ModalityType.APPLICATION_MODAL);
        this.codigoContrato = codigoContrato;
        this.historialDAO = dao;

        initUI();
        cargarDatos();

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        EstilosUI.estilizarDialogo(this);
        setLayout(new BorderLayout(10, 10));
        
        tableModel = new HistorialTableModel();
        tabla = new JTable(tableModel);
        
        EstilosUI.estilizarTabla(tabla); // Estilo de tabla
        
        // Ajustar anchos de columna
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(60);  // Código
        tabla.getColumnModel().getColumn(1).setPreferredWidth(160); // Fecha
        tabla.getColumnModel().getColumn(2).setPreferredWidth(150); // Tipo
        tabla.getColumnModel().getColumn(3).setPreferredWidth(300); // Descripción
        tabla.getColumnModel().getColumn(4).setPreferredWidth(100); // Usuario

        JScrollPane scroll = new JScrollPane(tabla);
        EstilosUI.estilizarScrollPane(scroll);
        
        add(scroll, BorderLayout.CENTER);
        
        JButton btnCerrar = new JButton("Cerrar");
        EstilosUI.estilizarBotonSecundario(btnCerrar);
        btnCerrar.addActionListener(e -> dispose());
        
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        EstilosUI.aplicarFondoClaro(panelBoton);
        panelBoton.add(btnCerrar);

        add(panelBoton, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(800, 350));
    }

    private void cargarDatos() {
        List<HistorialCambioCronograma> lista = historialDAO.listarPorContrato(codigoContrato);
        tableModel.setDatos(lista);
    }

    // ---------------- TABLE MODEL INTERNO (Sin cambios) ----------------
    private static class HistorialTableModel extends AbstractTableModel {
        private final String[] columnas = {"Código", "Fecha cambio", "Tipo cambio", "Descripción", "Usuario"};
        private List<HistorialCambioCronograma> datos;
        private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        public void setDatos(List<HistorialCambioCronograma> datos) {
            this.datos = datos;
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return datos == null ? 0 : datos.size(); }
        @Override public int getColumnCount() { return columnas.length; }
        @Override public String getColumnName(int column) { return columnas[column]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            HistorialCambioCronograma h = datos.get(rowIndex);
            switch (columnIndex) {
                case 0: return h.getCodigoHistorial();
                case 1: return h.getFechaCambio() != null ? sdf.format(h.getFechaCambio()) : "";
                case 2: return h.getTipoCambio();
                case 3: return h.getDescripcionCambio();
                case 4: return h.getUsuarioRegistro();
            }
            return "";
        }
    }
}