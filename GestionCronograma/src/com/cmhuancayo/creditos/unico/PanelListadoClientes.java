package com.cmhuancayo.creditos.unico;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel principal de la aplicación que muestra:
 *  - Filtros de búsqueda de clientes
 *  - Tabla con el 
 *  clientes
 *  - Botones para CRUD básico (nuevo, eliminar lógico, refrescar)
 *
 * Desde aquí se abre la pestaña de gestión de cada cliente en el DashboardPrincipal.
 */
public class PanelListadoClientes extends JPanel {

    /** Referencia a la ventana principal para poder abrir pestañas de gestión. */
    private final DashboardPrincipal dashboard;

    /** DAO para operaciones de búsqueda y eliminación lógica de clientes. */
    private final ClienteDAO clienteDAO;

    /** Tabla de clientes y su modelo. */
    private JTable tablaClientes;
    private ClienteTableModel tableModel;

    /** Controles de filtro de búsqueda. */
    private JTextField txtBusquedaNombre;
    private JTextField txtBusquedaDNI;
    private JComboBox<String> cmbEstado;

    public PanelListadoClientes(DashboardPrincipal dashboard) {
        this.dashboard = dashboard;
        this.clienteDAO = new ClienteDAO();
        
        setLayout(new BorderLayout(10, 10));
        // Margen exterior y fondo general
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 
        EstilosUI.aplicarFondoClaro(this);

        add(crearPanelFiltros(), BorderLayout.NORTH);
        add(crearPanelTabla(), BorderLayout.CENTER);
        add(crearPanelBotonesInferiores(), BorderLayout.SOUTH);
        
        // Carga inicial del listado
        cargarClientes();
    }

    /* ==========================================================
     * PANEL SUPERIOR: FILTROS DE BÚSQUEDA
     * ========================================================== */
    private JPanel crearPanelFiltros() {
        JPanel panelFiltros = new JPanel(new GridBagLayout());
        // Panel con borde titulado estilizado
        EstilosUI.estilizarTitledPanel(panelFiltros, "Filtros de Búsqueda"); 
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); // Espaciado entre controles
        gbc.anchor = GridBagConstraints.WEST;

        // Nombre / Apellido
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblNombre = new JLabel("Nombre/Apellido:");
        EstilosUI.estilizarLabel(lblNombre);
        panelFiltros.add(lblNombre, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtBusquedaNombre = new JTextField(20);
        EstilosUI.estilizarCampo(txtBusquedaNombre);
        panelFiltros.add(txtBusquedaNombre, gbc);

        // DNI
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblDni = new JLabel("DNI:");
        EstilosUI.estilizarLabel(lblDni);
        panelFiltros.add(lblDni, gbc);

        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        txtBusquedaDNI = new JTextField(10);
        EstilosUI.estilizarCampo(txtBusquedaDNI);
        panelFiltros.add(txtBusquedaDNI, gbc);

        // Estado (Al Día / Atrasado / Todos)
        gbc.gridx = 4;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblEstado = new JLabel("Estado:");
        EstilosUI.estilizarLabel(lblEstado);
        panelFiltros.add(lblEstado, gbc);

        gbc.gridx = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        cmbEstado = new JComboBox<>(new String[]{"Todos", "Al Día", "Atrasado"});
        EstilosUI.estilizarComboBox(cmbEstado);
        panelFiltros.add(cmbEstado, gbc);

        // Botón Buscar
        gbc.gridx = 6;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        JButton btnBuscar = new JButton("Buscar");
        EstilosUI.estilizarBotonPrincipal(btnBuscar);
        btnBuscar.addActionListener(e -> cargarClientes());
        panelFiltros.add(btnBuscar, gbc);

        return panelFiltros;
    }

    /* ==========================================================
     * PANEL CENTRAL: TABLA DE CLIENTES
     * ========================================================== */
    private JScrollPane crearPanelTabla() {
        tableModel = new ClienteTableModel(new ArrayList<>());
        tablaClientes = new JTable(tableModel);
        
        // Estilo unificado de tabla (cabecera roja, filas en cebra)
        EstilosUI.estilizarTabla(tablaClientes);
        
        tablaClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaClientes.setAutoCreateRowSorter(true); // Permite ordenar por columnas

        // Doble clic sobre una fila para abrir la gestión del cliente
        tablaClientes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tablaClientes.getSelectedRow() != -1) {
                    int fila = tablaClientes.getSelectedRow();
                    // Convertir índice de vista a índice de modelo (por el sorter)
                    fila = tablaClientes.convertRowIndexToModel(fila);
                    Cliente c = tableModel.getClienteAt(fila);
                    dashboard.abrirPestanaGestion(c);
                }
            }
        });
        
        JScrollPane scroll = new JScrollPane(tablaClientes);
        EstilosUI.estilizarScrollPane(scroll);
        return scroll;
    }

    /* ==========================================================
     * PANEL INFERIOR: BOTONES CRUD
     * ========================================================== */
    private JPanel crearPanelBotonesInferiores() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        EstilosUI.aplicarFondoClaro(panel);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xDDDDDD)));

        JButton btnNuevo = new JButton("Nuevo Cliente");
        JButton btnEliminar = new JButton("Eliminar Cliente");
        JButton btnRefrescar = new JButton("Refrescar Lista");

        // Botón principal (acción más importante)
        EstilosUI.estilizarBotonPrincipal(btnNuevo);
        // Botones secundarios
        EstilosUI.estilizarBotonSecundario(btnEliminar);
        EstilosUI.estilizarBotonSecundario(btnRefrescar);

        btnNuevo.addActionListener(e -> nuevoCliente());
        btnEliminar.addActionListener(e -> eliminarClienteLogico());
        btnRefrescar.addActionListener(e -> cargarClientes());

        // Orden: refrescar, eliminar, nuevo (nuevo al extremo derecho)
        panel.add(btnRefrescar);
        panel.add(btnEliminar);
        panel.add(btnNuevo);

        return panel;
    }

    /* ==========================================================
     * LÓGICA DE NEGOCIO / EVENTOS
     * ========================================================== */

    /**
     * Carga la lista de clientes en la tabla según los filtros ingresados.
     * Usa ClienteDAO.buscarClientes (ya adaptado a PostgreSQL).
     */
    private void cargarClientes() {
        String nombre = txtBusquedaNombre.getText().trim();
        String dni = txtBusquedaDNI.getText().trim();
        String estado = (String) cmbEstado.getSelectedItem();

        List<Cliente> lista = clienteDAO.buscarClientes(nombre, dni, estado);
        tableModel.setClientes(lista);
    }

    /**
     * Abre el diálogo de registro de cliente y, si se guarda,
     * refresca automáticamente el listado.
     */
    private void nuevoCliente() {
        DialogoRegistroCliente dlg = new DialogoRegistroCliente(
                SwingUtilities.getWindowAncestor(this),
                new ClienteDAO()
        );
        dlg.setVisible(true);
        if (dlg.isGuardado()) {
            cargarClientes();
        }
    }

    /**
     * Eliminación lógica de cliente:
     * llama ClienteDAO.eliminarClienteLogico(codigoCliente)
     * y actualiza la tabla si la operación fue exitosa.
     */
    private void eliminarClienteLogico() {
        int filaVista = tablaClientes.getSelectedRow();
        if (filaVista == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Seleccione un cliente primero.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int filaModelo = tablaClientes.convertRowIndexToModel(filaVista);
        Cliente c = tableModel.getClienteAt(filaModelo);
        
        int r = JOptionPane.showConfirmDialog(
                this,
                "¿Eliminar lógicamente al cliente " + c.getNombres() + " " + c.getApellidoPaterno() + "?\nEsta acción no se puede deshacer.",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        
        if (r == JOptionPane.YES_OPTION) {
            boolean ok = clienteDAO.eliminarClienteLogico(c.getCodigoCliente());
            if (ok) {
                cargarClientes();
            }
        }
    }
}
