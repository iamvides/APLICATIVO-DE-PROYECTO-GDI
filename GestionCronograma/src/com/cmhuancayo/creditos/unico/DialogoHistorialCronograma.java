package com.cmhuancayo.creditos.unico;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Diálogo modal que muestra el historial de cambios del cronograma
 * de un contrato específico.
 *
 * Se apoya en:
 * - HistorialCronogramaDAO  -> para obtener los registros desde BD
 * - HistorialCambioCronograma -> modelo de cada fila del historial
 * - EstilosUI               -> para la parte visual (colores/fuentes)
 */
public class DialogoHistorialCronograma extends JDialog {

    /** Código de contrato al que pertenece el historial mostrado. */
    private final int codigoContrato;

    /** DAO que consulta el historial en la base de datos. */
    private final HistorialCronogramaDAO historialDAO;

    /** Tabla donde se renderiza el historial. */
    private JTable tabla;

    /** Modelo de tabla que adapta la lista de cambios a la JTable. */
    private HistorialTableModel tableModel;

    /**
     * Construye el diálogo de historial para un contrato concreto.
     *
     * @param owner          Ventana padre (para modalidad y centrado).
     * @param codigoContrato Código del contrato cuyo historial se va a mostrar.
     * @param dao            DAO para consultar el historial en BD.
     */
    public DialogoHistorialCronograma(Window owner,
                                      int codigoContrato,
                                      HistorialCronogramaDAO dao) {
        super(owner,
                "Historial de Cambios - Contrato " + codigoContrato,
                ModalityType.APPLICATION_MODAL);

        this.codigoContrato = codigoContrato;
        this.historialDAO = dao;

        initUI();
        cargarDatos();

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    /**
     * Inicializa componentes gráficos y aplica estilos.
     */
    private void initUI() {
        // Aplica colores/fuentes por defecto definidos en EstilosUI
        EstilosUI.estilizarDialogo(this);
        setLayout(new BorderLayout(10, 10));

        // Modelo vacío inicial (se llenará en cargarDatos)
        tableModel = new HistorialTableModel();
        tabla = new JTable(tableModel);

        // Estilo general de la tabla (cabeceras, filas, selección, etc.)
        EstilosUI.estilizarTabla(tabla);

        // Definimos ancho fijo por columna para evitar recortes
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(60);   // Código
        tabla.getColumnModel().getColumn(1).setPreferredWidth(160);  // Fecha
        tabla.getColumnModel().getColumn(2).setPreferredWidth(150);  // Tipo
        tabla.getColumnModel().getColumn(3).setPreferredWidth(300);  // Descripción
        tabla.getColumnModel().getColumn(4).setPreferredWidth(100);  // Usuario

        // Scroll con estilo unificado
        JScrollPane scroll = new JScrollPane(tabla);
        EstilosUI.estilizarScrollPane(scroll);

        add(scroll, BorderLayout.CENTER);

        // Botón Cerrar
        JButton btnCerrar = new JButton("Cerrar");
        EstilosUI.estilizarBotonSecundario(btnCerrar);
        btnCerrar.addActionListener(e -> dispose());

        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        EstilosUI.aplicarFondoClaro(panelBoton);
        panelBoton.add(btnCerrar);

        add(panelBoton, BorderLayout.SOUTH);

        // Tamaño sugerido del diálogo
        setPreferredSize(new Dimension(800, 350));
    }

    /**
     * Consulta el historial en BD y actualiza el modelo de la tabla.
     */
    private void cargarDatos() {
        List<HistorialCambioCronograma> lista =
                historialDAO.listarPorContrato(codigoContrato);
        tableModel.setDatos(lista);
    }

    /**
     * TableModel interno para mostrar:
     *   - Código de historial
     *   - Fecha y hora del cambio
     *   - Tipo de cambio
     *   - Descripción del cambio
     *   - Usuario que realizó el registro
     */
    private static class HistorialTableModel extends AbstractTableModel {

        private final String[] columnas = {
                "Código",
                "Fecha cambio",
                "Tipo cambio",
                "Descripción",
                "Usuario"
        };

        /** Lista de registros a mostrar. */
        private List<HistorialCambioCronograma> datos;

        /** Formato de fecha y hora para la columna de fecha. */
        private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        /**
         * Actualiza la lista de registros mostrados en la tabla.
         */
        public void setDatos(List<HistorialCambioCronograma> datos) {
            this.datos = datos;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return (datos == null) ? 0 : datos.size();
        }

        @Override
        public int getColumnCount() {
            return columnas.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnas[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (datos == null || rowIndex < 0 || rowIndex >= datos.size()) {
                return "";
            }

            HistorialCambioCronograma h = datos.get(rowIndex);

            switch (columnIndex) {
                case 0: // Código
                    return h.getCodigoHistorial();
                case 1: // Fecha cambio
                    return (h.getFechaCambio() != null)
                            ? sdf.format(h.getFechaCambio())
                            : "";
                case 2: // Tipo cambio
                    return h.getTipoCambio();
                case 3: // Descripción
                    return h.getDescripcionCambio();
                case 4: // Usuario
                    return h.getUsuarioRegistro();
                default:
                    return "";
            }
        }
    }
}
