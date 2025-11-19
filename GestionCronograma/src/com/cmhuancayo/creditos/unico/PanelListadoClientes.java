package com.cmhuancayo.creditos.unico;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class PanelListadoClientes extends JPanel {

    private final DashboardPrincipal dashboard;
    private final ClienteDAO clienteDAO;

    private JTable tablaClientes;
    private ClienteTableModel tableModel;

    private JTextField txtBusquedaNombre;
    private JTextField txtBusquedaDNI;
    private JComboBox<String> cmbEstado;

    public PanelListadoClientes(DashboardPrincipal dashboard) {
        this.dashboard = dashboard;
        this.clienteDAO = new ClienteDAO();
        
        setLayout(new BorderLayout(10, 10));
        // Margen exterior y fondo
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 
        EstilosUI.aplicarFondoClaro(this);

        add(crearPanelFiltros(), BorderLayout.NORTH);
        add(crearPanelTabla(), BorderLayout.CENTER);
        add(crearPanelBotonesInferiores(), BorderLayout.SOUTH);
        
        // Carga inicial
        cargarClientes();
    }

    /* ==========================================================
     * PANEL SUPERIOR: FILTROS DE BÚSQUEDA
     * ========================================================== */
    private JPanel crearPanelFiltros() {
        JPanel panelFiltros = new JPanel(new GridBagLayout());
        // Usamos el TitledPanel estilizado
        EstilosUI.estilizarTitledPanel(panelFiltros, "Filtros de Búsqueda"); 
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); // Más espaciado
        gbc.anchor = GridBagConstraints.WEST;

        // Nombre / Apellido
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblNombre = new JLabel("Nombre/Apellido:");
        EstilosUI.estilizarLabel(lblNombre); // Estilo
        panelFiltros.add(lblNombre, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtBusquedaNombre = new JTextField(20);
        EstilosUI.estilizarCampo(txtBusquedaNombre); // Estilo
        panelFiltros.add(txtBusquedaNombre, gbc);

        // DNI
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblDni = new JLabel("DNI:");
        EstilosUI.estilizarLabel(lblDni); // Estilo
        panelFiltros.add(lblDni, gbc);

        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        txtBusquedaDNI = new JTextField(10);
        EstilosUI.estilizarCampo(txtBusquedaDNI); // Estilo
        panelFiltros.add(txtBusquedaDNI, gbc);

        // Estado
        gbc.gridx = 4;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblEstado = new JLabel("Estado:");
        EstilosUI.estilizarLabel(lblEstado); // Estilo
        panelFiltros.add(lblEstado, gbc);

        gbc.gridx = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        cmbEstado = new JComboBox<>(new String[]{"Todos", "Al Día", "Atrasado"});
        EstilosUI.estilizarComboBox(cmbEstado); // Estilo
        panelFiltros.add(cmbEstado, gbc);

        // Botón Buscar
        gbc.gridx = 6;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        JButton btnBuscar = new JButton("Buscar");
        EstilosUI.estilizarBotonPrincipal(btnBuscar); // Estilo
        btnBuscar.addActionListener(e -> cargarClientes());
        panelFiltros.add(btnBuscar, gbc);

        return panelFiltros;
    }

    /* ==========================================================
     * PANEL CENTRAL: TABLA
     * ========================================================== */
    private JScrollPane crearPanelTabla() {
        tableModel = new ClienteTableModel(new ArrayList<>());
        tablaClientes = new JTable(tableModel);
        
        EstilosUI.estilizarTabla(tablaClientes); // Aplicamos el nuevo estilo de tabla
        
        tablaClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaClientes.setAutoCreateRowSorter(true);

        // Doble clic para abrir gestión
        tablaClientes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tablaClientes.getSelectedRow() != -1) {
                    int fila = tablaClientes.getSelectedRow();
                    fila = tablaClientes.convertRowIndexToModel(fila);
                    Cliente c = tableModel.getClienteAt(fila);
                    dashboard.abrirPestanaGestion(c);
                }
            }
        });
        
        JScrollPane scroll = new JScrollPane(tablaClientes);
        EstilosUI.estilizarScrollPane(scroll); // Estilo
        return scroll;
    }

    /* ==========================================================
     * PANEL INFERIOR: BOTONES CRUD
     * ========================================================== */
    private JPanel crearPanelBotonesInferiores() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        EstilosUI.aplicarFondoClaro(panel); // Fondo
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xDDDDDD)));

        JButton btnNuevo = new JButton("Nuevo Cliente");
        JButton btnEliminar = new JButton("Eliminar Cliente");
        JButton btnRefrescar = new JButton("Refrescar Lista");

        // Botón principal
        EstilosUI.estilizarBotonPrincipal(btnNuevo); 
        // Botones secundarios
        EstilosUI.estilizarBotonSecundario(btnEliminar);
        EstilosUI.estilizarBotonSecundario(btnRefrescar); 

        btnNuevo.addActionListener(e -> nuevoCliente());
        btnEliminar.addActionListener(e -> eliminarClienteLogico());
        btnRefrescar.addActionListener(e -> cargarClientes());

        panel.add(btnRefrescar);
        panel.add(btnEliminar);
        panel.add(btnNuevo); // El botón principal al final (derecha)

        return panel;
    }

    /* ==========================================================
     * LÓGICA (Sin cambios)
     * ========================================================== */
    private void cargarClientes() {
        String nombre = txtBusquedaNombre.getText().trim();
        String dni = txtBusquedaDNI.getText().trim();
        String estado = (String) cmbEstado.getSelectedItem();

        List<Cliente> lista = clienteDAO.buscarClientes(nombre, dni, estado);
        tableModel.setClientes(lista);
    }

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

    private void eliminarClienteLogico() {
        int filaVista = tablaClientes.getSelectedRow();
        if (filaVista == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un cliente primero.", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int filaModelo = tablaClientes.convertRowIndexToModel(filaVista);
        Cliente c = tableModel.getClienteAt(filaModelo);
        
        int r = JOptionPane.showConfirmDialog(this,
                "¿Eliminar lógicamente al cliente " + c.getNombres() + " " + c.getApellidoPaterno() + "?\nEsta acción no se puede deshacer.",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (r == JOptionPane.YES_OPTION) {
            boolean ok = clienteDAO.eliminarClienteLogico(c.getCodigoCliente());
            if (ok) {
                cargarClientes();
            }
        }
    }
}