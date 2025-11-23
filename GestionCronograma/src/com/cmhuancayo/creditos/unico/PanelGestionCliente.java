package com.cmhuancayo.creditos.unico;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;              
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.ListSelectionModel;

/**
 * Panel de gestión individual de un cliente.
 *
 * Desde aquí se puede:
 *  - Ver y editar datos básicos del cliente (nombre, apellido, teléfono).
 *  - Ver la lista de créditos (contratos) del cliente.
 *  - Visualizar el cronograma de pagos del crédito seleccionado.
 *  - Registrar pagos de cuota (totales y parciales).
 *  - Aplicar amortizaciones extraordinarias.
 *  - Reprogramar créditos (cambio de plazo / tasa).
 *  - Generar un nuevo crédito por refinanciamiento.
 *  - Ver el historial de cambios del cronograma.
 */
public class PanelGestionCliente extends JPanel {

    /** Cliente actual que se está gestionando. */
    private Cliente cliente;

    /** Crédito actualmente seleccionado en el combo. */
    private Credito creditoSeleccionado;

    /** Tabla que muestra el cronograma del crédito seleccionado. */
    private JTable tablaCronograma;
    private CuotaTableModel cuotaTableModel;

    /** Combo con la lista de créditos del cliente. */
    private JComboBox<Credito> cmbCreditos;

    // Capas de servicio y acceso a datos
    private CalculadoraCreditoService calculadoraService;
    private ClienteDAO clienteDAO;
    private CreditoDAO creditoDAO;
    private CuotaDAO cuotaDAO;
    private HistorialCronogramaDAO historialDAO;

    // Controles de datos básicos del cliente
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtDNI;
    private JTextField txtTelefono;
    private JButton btnGuardarCliente;

    // Etiquetas de resumen del crédito
    private JLabel lblResumenDesembolso;
    private JLabel lblResumenCuotas;
    private JLabel lblResumenTasa;
    private JLabel lblResumenCliente;
    private JLabel lblResumenDocumento;
    private JLabel lblResumenContrato;
    private JLabel lblResumenFechaVigencia;

    public PanelGestionCliente(Cliente cliente) {
        this.cliente = cliente;

        this.calculadoraService = new CalculadoraCreditoService();
        this.clienteDAO = new ClienteDAO();
        this.creditoDAO = new CreditoDAO();
        this.cuotaDAO = new CuotaDAO();
        this.historialDAO = new HistorialCronogramaDAO();

        setLayout(new BorderLayout(10, 10));
        setBorder(EstilosUI.BORDE_PANEL_INTERNO);
        EstilosUI.aplicarFondoClaro(this);

        add(crearPanelIzquierdo(), BorderLayout.WEST);
        add(crearPanelCentralCronograma(), BorderLayout.CENTER);

        cargarDatosClienteEnUI();
        cargarListaDeCreditos();
    }

    /* =========================================================
     *  PANEL IZQUIERDO: DATOS DEL CLIENTE + CRÉDITOS
     * ========================================================= */

    private JPanel crearPanelIzquierdo() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(350, 0));
        panel.setOpaque(false);

        // 1. Panel de Datos del Cliente
        JPanel panelDatos = new JPanel(new GridBagLayout());
        EstilosUI.estilizarTitledPanel(panelDatos, "Datos del cliente");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtNombre = new JTextField(cliente.getNombres(), 20);
        txtApellido = new JTextField(cliente.getApellidoPaterno(), 20);
        txtDNI = new JTextField(cliente.getNumeroDocumento(), 20);
        txtDNI.setEditable(false); // El DNI no se debe modificar
        txtDNI.setBackground(EstilosUI.GRIS_FONDO);
        txtTelefono = new JTextField(cliente.getTelefono(), 20);

        EstilosUI.estilizarCampo(txtNombre);
        EstilosUI.estilizarCampo(txtApellido);
        EstilosUI.estilizarCampo(txtDNI);
        EstilosUI.estilizarCampo(txtTelefono);

        // Campos y etiquetas
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panelDatos.add(new JLabel("Nombres:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1;
        panelDatos.add(txtNombre, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panelDatos.add(new JLabel("Ap. paterno:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1;
        panelDatos.add(txtApellido, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        panelDatos.add(new JLabel("DNI:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1;
        panelDatos.add(txtDNI, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        panelDatos.add(new JLabel("Teléfono:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1;
        panelDatos.add(txtTelefono, gbc);

        // Botón Guardar cambios del cliente
        btnGuardarCliente = new JButton("Guardar cambios");
        EstilosUI.estilizarBotonPrincipal(btnGuardarCliente);
        btnGuardarCliente.addActionListener(this::guardarDatosCliente);

        gbc.gridx = 0;
        gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(15, 8, 8, 8);
        panelDatos.add(btnGuardarCliente, gbc);

        panel.add(panelDatos);
        panel.add(Box.createVerticalStrut(15));

        // 2. Panel de créditos asociados al cliente
        JPanel panelCreditos = new JPanel(new GridBagLayout());
        EstilosUI.estilizarTitledPanel(panelCreditos, "Créditos del cliente");

        gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panelCreditos.add(new JLabel("Seleccione contrato:"), gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        cmbCreditos = new JComboBox<>();
        EstilosUI.estilizarComboBox(cmbCreditos);
        panelCreditos.add(cmbCreditos, gbc);

        // Render del ComboBox
        cmbCreditos.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Credito) {
                    Credito c = (Credito) value;
                    String texto = String.format(
                            "Contrato %d - S/ %.2f (%d cuotas, %s)",
                            c.getCodigoContrato(),
                            (c.getMontoDesembolso() != null ? c.getMontoDesembolso() : BigDecimal.ZERO),
                            c.getNumeroCuotas(),
                            c.getEstadoContrato()
                    );
                    setText(texto);
                    setFont(EstilosUI.FONT_TEXTO);
                }
                setBorder(new EmptyBorder(5, 5, 5, 5));
                return this;
            }
        });

        // Botón para cargar cronograma
        JButton btnVerCronograma = new JButton("Ver Cronograma");
        EstilosUI.estilizarBotonSecundario(btnVerCronograma);
        btnVerCronograma.addActionListener(e -> cargarCronogramaSeleccionado());

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        panelCreditos.add(btnVerCronograma, gbc);

        // Botón para solicitar préstamo
        JButton btnSolicitar = new JButton("Solicitar Préstamo");
        EstilosUI.estilizarBotonPrincipal(btnSolicitar);
        btnSolicitar.addActionListener(e -> solicitarNuevoCredito());

        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        panelCreditos.add(btnSolicitar, gbc);

        panel.add(panelCreditos);
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }

    /* =========================================================
     *  PANEL CENTRAL: RESUMEN + CRONOGRAMA + ACCIONES
     * ========================================================= */

    private JPanel crearPanelCentralCronograma() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        // Panel superior: título + resumen
        JPanel panelSuperior = new JPanel(new BorderLayout(0, 10));
        panelSuperior.setOpaque(false);

        JLabel lblTitulo = new JLabel("CRONOGRAMA DE PAGOS", SwingConstants.LEFT);
        lblTitulo.setFont(EstilosUI.FONT_TITULO);
        lblTitulo.setForeground(EstilosUI.ROJO_PRINCIPAL);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        panelSuperior.add(lblTitulo, BorderLayout.NORTH);
        
        JPanel panelResumen = new JPanel(new GridLayout(2, 4, 20, 10));
        panelResumen.setBackground(EstilosUI.BLANCO);
        panelResumen.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder("Resumen del Crédito"),
                new EmptyBorder(10, 10, 10, 10)
        ));
        
        lblResumenDesembolso = new JLabel("DESEMBOLSO: -");
        lblResumenCuotas = new JLabel("CUOTAS: -");
        lblResumenTasa = new JLabel("TASA INTERÉS COMP. ANUAL: -");
        lblResumenCliente = new JLabel("CLIENTE: -");
        lblResumenDocumento = new JLabel("DOC.: -");
        lblResumenContrato = new JLabel("CONTRATO: -");
        lblResumenFechaVigencia = new JLabel("VIGENCIA: -");
        
        JLabel[] arr = {
                lblResumenDesembolso, lblResumenCuotas, lblResumenTasa,
                lblResumenCliente, lblResumenDocumento, lblResumenContrato,
                lblResumenFechaVigencia
        };

        for (JLabel l : arr) {
            EstilosUI.estilizarLabel(l);
        }

        panelResumen.add(lblResumenCliente);
        panelResumen.add(lblResumenDocumento);
        panelResumen.add(lblResumenContrato);
        panelResumen.add(lblResumenFechaVigencia);
        
        panelResumen.add(lblResumenDesembolso);
        panelResumen.add(lblResumenCuotas);
        panelResumen.add(lblResumenTasa);
        panelResumen.add(new JLabel()); // celda vacía
        
        panelSuperior.add(panelResumen, BorderLayout.CENTER);
        panel.add(panelSuperior, BorderLayout.NORTH);

        // Tabla de cronograma
        cuotaTableModel = new CuotaTableModel(new ArrayList<>());
        tablaCronograma = new JTable(cuotaTableModel);
        
        EstilosUI.estilizarTabla(tablaCronograma);
        
        tablaCronograma.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaCronograma.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Renderizadores
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        
        // Ajuste de columnas
        for (int i = 0; i < cuotaTableModel.getColumnCount(); i++) {
            int width;
            switch (i) {
                case 0: width = 50;  tablaCronograma.getColumnModel().getColumn(i).setCellRenderer(centerRenderer); break; // N°
                case 1: width = 120; break; // Fecha
                case 2: width = 110; tablaCronograma.getColumnModel().getColumn(i).setCellRenderer(rightRenderer); break; // Capital
                case 3: width = 110; tablaCronograma.getColumnModel().getColumn(i).setCellRenderer(rightRenderer); break; // Interés
                case 4: width = 110; tablaCronograma.getColumnModel().getColumn(i).setCellRenderer(rightRenderer); break; // Seg. degrav.
                case 5: width = 120; tablaCronograma.getColumnModel().getColumn(i).setCellRenderer(rightRenderer); break; // Monto cuota
                case 6: width = 120; tablaCronograma.getColumnModel().getColumn(i).setCellRenderer(rightRenderer); break; // Saldo capital
                case 7: width = 100; break; // Estado
                default: width = 80;
            }
            tablaCronograma.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        
        JScrollPane scroll = new JScrollPane(tablaCronograma);
        EstilosUI.estilizarScrollPane(scroll);
        panel.add(scroll, BorderLayout.CENTER);
        
        // Barra inferior de acciones
        JToolBar toolBarAcciones = new JToolBar();
        toolBarAcciones.setFloatable(false);
        toolBarAcciones.setBackground(EstilosUI.GRIS_FONDO);
        toolBarAcciones.setBorder(new EmptyBorder(10, 0, 0, 0));
        toolBarAcciones.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        JButton btnRegistrarPago = new JButton("Registrar Pago de Cuota");
        JButton btnPagoParcial = new JButton("Pago Parcial");
        JButton btnAmortizacion = new JButton("Amortización");
        JButton btnReprogramar = new JButton("Reprogramar");
        JButton btnRefinanciar = new JButton("Refinanciar");
        JButton btnHistorial = new JButton("Ver Historial");
        JButton btnDescargar = new JButton("Descargar cronograma"); // <-- NUEVO

        EstilosUI.estilizarBotonPrincipal(btnRegistrarPago);
        EstilosUI.estilizarBotonSecundario(btnPagoParcial);
        EstilosUI.estilizarBotonSecundario(btnAmortizacion);
        EstilosUI.estilizarBotonSecundario(btnReprogramar);
        EstilosUI.estilizarBotonSecundario(btnRefinanciar);
        EstilosUI.estilizarBotonSecundario(btnHistorial);
        EstilosUI.estilizarBotonSecundario(btnDescargar); // <-- NUEVO
        
        btnRegistrarPago.addActionListener(this::registrarPagoCuota);
        btnPagoParcial.addActionListener(this::pagoParcial);
        btnAmortizacion.addActionListener(this::amortizacionCapital);
        btnReprogramar.addActionListener(this::reprogramarCredito);
        btnRefinanciar.addActionListener(this::refinanciarCredito);
        btnHistorial.addActionListener(this::verHistorialCronograma);
        btnDescargar.addActionListener(this::descargarCronograma); // <-- NUEVO

        // Orden de botones
        toolBarAcciones.add(btnHistorial);
        toolBarAcciones.add(btnDescargar);             // <-- NUEVO
        toolBarAcciones.add(Box.createHorizontalStrut(20));
        toolBarAcciones.add(btnAmortizacion);
        toolBarAcciones.add(btnReprogramar);
        toolBarAcciones.add(btnRefinanciar);
        toolBarAcciones.add(Box.createHorizontalStrut(20));
        toolBarAcciones.add(btnPagoParcial);
        toolBarAcciones.add(btnRegistrarPago);

        panel.add(toolBarAcciones, BorderLayout.SOUTH);

        return panel;
    }

    /* =========================================================
     *  CARGA DE DATOS Y RESUMEN
     * ========================================================= */

    private void cargarDatosClienteEnUI() {
        if (cliente == null) return;
        txtNombre.setText(cliente.getNombres() != null ? cliente.getNombres() : "");
        txtApellido.setText(cliente.getApellidoPaterno() != null ? cliente.getApellidoPaterno() : "");
        txtDNI.setText(cliente.getNumeroDocumento() != null ? cliente.getNumeroDocumento() : "");
        txtTelefono.setText(cliente.getTelefono() != null ? cliente.getTelefono() : "");
    }

    private void cargarListaDeCreditos() {
        List<Credito> creditos = creditoDAO.listarPorCliente(cliente.getCodigoCliente());
        cmbCreditos.removeAllItems();
        for (Credito c : creditos) {
            cmbCreditos.addItem(c); 
        }

        if (cmbCreditos.getItemCount() > 0) {
            cmbCreditos.setSelectedIndex(0);
            cargarCronogramaSeleccionado();
        } else {
            creditoSeleccionado = null;
            cuotaTableModel.setCuotas(new ArrayList<>());
            actualizarResumen(null);
        }
    }

    private void cargarCronogramaSeleccionado() {
        Credito seleccionado = (Credito) cmbCreditos.getSelectedItem();
        if (seleccionado == null) {
            creditoSeleccionado = null;
            cuotaTableModel.setCuotas(List.of());
            actualizarResumen(null);
            return;
        }
        this.creditoSeleccionado = seleccionado;

        List<Cuota> cuotas = cuotaDAO.listarPorContrato(seleccionado.getCodigoContrato());
        
        if (cuotas.isEmpty()
                && seleccionado.getMontoDesembolso() != null
                && seleccionado.getMontoDesembolso().compareTo(BigDecimal.ZERO) > 0) {
            cuotas = calculadoraService.generarCronograma(seleccionado);
        }
        
        cuotaTableModel.setCuotas(cuotas);
        actualizarResumen(seleccionado);
    }

    private void actualizarResumen(Credito c) {
        if (c == null) {
            lblResumenDesembolso.setText("DESEMBOLSO: -");
            lblResumenCuotas.setText("CUOTAS: -");
            lblResumenTasa.setText("TASA INTERÉS COMP. ANUAL: -");
            lblResumenCliente.setText("CLIENTE: -");
            lblResumenDocumento.setText("DOC.: -");
            lblResumenContrato.setText("CONTRATO: -");
            lblResumenFechaVigencia.setText("VIGENCIA: -");
            return;
        }

        String desembolso = formatearMoneda(c.getMontoDesembolso());
        String numCuotas = String.valueOf(c.getNumeroCuotas());
        String tasa = formatearPorcentaje(c.getTasaInteresCompensatorio());
        
        String nombreCompleto =
                (cliente.getApellidoPaterno() != null ? cliente.getApellidoPaterno() + " " : "") +
                (cliente.getApellidoMaterno() != null ? cliente.getApellidoMaterno() + " " : "") +
                (cliente.getNombres() != null ? cliente.getNombres() : "");
        String nombreFinal = nombreCompleto.trim();
        
        String documento = cliente.getNumeroDocumento() != null ? cliente.getNumeroDocumento() : "-";
        String contrato = String.valueOf(c.getCodigoContrato());
        
        Date vig = c.getFechaDesembolso();
        String vigencia = vig != null ? formatearFecha(vig) : "-";

        lblResumenDesembolso.setText("DESEMBOLSO:  " + desembolso);
        lblResumenCuotas.setText("CUOTAS:  " + numCuotas);
        lblResumenTasa.setText("TASA ANUAL:  " + tasa);
        lblResumenCliente.setText("CLIENTE:  " + nombreFinal);
        lblResumenDocumento.setText("DOC.:  " + documento);
        lblResumenContrato.setText("CONTRATO:  " + contrato);
        lblResumenFechaVigencia.setText("VIGENCIA:  " + vigencia);
    }
    
    private void actualizarResumen() {
        actualizarResumen(this.creditoSeleccionado);
    }

    /* =========================================================
     *  ACCIONES SOBRE CLIENTE Y CRÉDITO
     * ========================================================= */

    private void guardarDatosCliente(ActionEvent e) {
        cliente.setNombres(txtNombre.getText().trim());
        cliente.setApellidoPaterno(txtApellido.getText().trim());
        cliente.setTelefono(txtTelefono.getText().trim());

        boolean ok = clienteDAO.actualizarCliente(cliente);
        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Datos del cliente actualizados correctamente.",
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se pudo actualizar el cliente.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        actualizarResumen(); 
    }

    private void solicitarNuevoCredito() {
        DialogoSolicitudCredito dialogo = new DialogoSolicitudCredito(
                SwingUtilities.getWindowAncestor(this),
                this.cliente,
                this.creditoDAO
        );
        dialogo.setVisible(true);

        if (dialogo.isGuardado()) {
            int nuevoContrato = dialogo.getCodigoContratoCreado();
            cargarListaDeCreditos();
            if (nuevoContrato > 0) {
                seleccionarContratoEnCombo(nuevoContrato);
            }
        }
    }

    private void seleccionarContratoEnCombo(int codigoContrato) {
        ComboBoxModel<Credito> model = cmbCreditos.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            Credito c = model.getElementAt(i);
            if (c.getCodigoContrato() == codigoContrato) {
                cmbCreditos.setSelectedIndex(i);
                cargarCronogramaSeleccionado();
                break;
            }
        }
    }

    private Cuota obtenerCuotaSeleccionada() {
        int filaVista = tablaCronograma.getSelectedRow();
        if (filaVista == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Seleccione una cuota de la tabla.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
            return null;
        }
        int filaModelo = tablaCronograma.convertRowIndexToModel(filaVista);
        return cuotaTableModel.getCuotaAt(filaModelo);
    }

    private void registrarPagoCuota(ActionEvent e) {
        if (creditoSeleccionado == null) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione primero un crédito.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Cuota cuota = obtenerCuotaSeleccionada();
        if (cuota == null) return;
        
        if ("Pagada".equalsIgnoreCase(cuota.getEstadoCuota())) {
            JOptionPane.showMessageDialog(this,
                    "Esta cuota ya está pagada.",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Confirmar pago total de " + formatearMoneda(cuota.getMontoCuota()) +
                        " para la cuota N° " + cuota.getNroCuota() + "?",
                "Confirmar pago",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = cuotaDAO.registrarPagoCuota(
                cuota.getCodigoContrato(),
                cuota.getNroCuota(),
                cuota.getMontoCuota(),
                "sistema"
        );
        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Pago registrado correctamente.",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            cargarCronogramaSeleccionado();
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se pudo registrar el pago.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pagoParcial(ActionEvent e) {
        if (creditoSeleccionado == null) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione primero un crédito.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Cuota cuota = obtenerCuotaSeleccionada();
        if (cuota == null) return;
        
        if ("Pagada".equalsIgnoreCase(cuota.getEstadoCuota())) {
            JOptionPane.showMessageDialog(this,
                    "Esta cuota ya está pagada.",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String montoStr = JOptionPane.showInputDialog(this,
                "Ingrese el monto a pagar (pago parcial) para la cuota N° "
                        + cuota.getNroCuota() + ":\n"
                        + "Monto de la cuota: " + formatearMoneda(cuota.getMontoCuota()),
                "Pago parcial de cuota",
                JOptionPane.QUESTION_MESSAGE);
        
        if (montoStr == null) return;

        BigDecimal montoPago;
        try {
            montoPago = new BigDecimal(montoStr.replace(",", "."));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Monto inválido.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (montoPago.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this,
                    "El monto debe ser mayor que cero.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirmar = JOptionPane.showConfirmDialog(this,
                "¿Confirmar pago parcial de " + formatearMoneda(montoPago)
                        + " empezando en la cuota N° " + cuota.getNroCuota() + "?\n"
                        + "El excedente se aplicará a las siguientes cuotas.",
                "Confirmar pago parcial",
                JOptionPane.YES_NO_OPTION);
        
        if (confirmar != JOptionPane.YES_OPTION) return;

        boolean ok = cuotaDAO.registrarPagoParcialCuota(
                creditoSeleccionado.getCodigoContrato(),
                cuota.getNroCuota(),
                montoPago,
                "sistema"
        );
        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Pago parcial registrado correctamente.",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            cargarCronogramaSeleccionado();
        } else {
             JOptionPane.showMessageDialog(this,
                     "No se pudo registrar el pago parcial.",
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
        }
    }

    private void amortizacionCapital(ActionEvent e) {
        if (creditoSeleccionado == null) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione primero un crédito.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String montoStr = JOptionPane.showInputDialog(this,
                "Ingrese el monto a amortizar (S/):",
                "Amortización de capital",
                JOptionPane.QUESTION_MESSAGE);
        if (montoStr == null) return;

        BigDecimal montoAmortizacion;
        try {
            montoAmortizacion = new BigDecimal(montoStr.replace(",", "."));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Monto inválido.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (montoAmortizacion.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this,
                    "El monto debe ser mayor que cero.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] opciones = {"Reducir plazo", "Reducir monto de cuota"};
        int opcion = JOptionPane.showOptionDialog(this,
                "Seleccione el tipo de amortización:",
                "Amortización de capital",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]);
        if (opcion == -1) return;

        String tipoReduccion = (opcion == 0) ? "REDUCIR_PLAZO" : "REDUCIR_CUOTA";

        List<Cuota> cronogramaActual = cuotaTableModel.getCuotas();
        List<Cuota> nuevoCronograma = calculadoraService.recalcularPorAmortizacion(
                creditoSeleccionado,
                cronogramaActual,
                montoAmortizacion,
                tipoReduccion,
                0
        );
        if (nuevoCronograma == null || nuevoCronograma.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "La amortización no pudo aplicarse (monto demasiado alto o datos insuficientes).",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean archivado = cuotaDAO.archivarCronogramaActual(
                creditoSeleccionado.getCodigoContrato(),
                "AMORTIZACION",
                "Amortización extraordinaria de capital por S/ " + montoAmortizacion,
                "sistema"
        );
        if (!archivado) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo archivar el cronograma anterior. Operación cancelada.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean okCronograma = cuotaDAO.actualizarCronogramaCompleto(
                creditoSeleccionado.getCodigoContrato(), nuevoCronograma);
        if (!okCronograma) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo actualizar el cronograma. Operación cancelada.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        historialDAO.registrarCambio(
                creditoSeleccionado.getCodigoContrato(),
                "AMORTIZACION",
                "Amortización extraordinaria de capital por S/ " + montoAmortizacion,
                "sistema"
        );

        cuotaTableModel.setCuotas(nuevoCronograma);

        JOptionPane.showMessageDialog(this,
                "Amortización aplicada y cronograma recalculado.",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void reprogramarCredito(ActionEvent e) {
        if (creditoSeleccionado == null) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione primero un crédito.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        DialogoReprogramacionCredito dlg = new DialogoReprogramacionCredito(
                SwingUtilities.getWindowAncestor(this),
                creditoSeleccionado
        );
        dlg.setVisible(true);

        if (!dlg.isGuardado()) return;

        int nuevasCuotas = dlg.getNuevasCuotas();
        BigDecimal nuevaTasa = dlg.getNuevaTasa();

        List<Cuota> cronogramaActual = cuotaTableModel.getCuotas();
        List<Cuota> nuevoCronograma = calculadoraService.recalcularPorReprogramacion(
                creditoSeleccionado,
                cronogramaActual,
                nuevasCuotas,
                nuevaTasa
        );
        if (nuevoCronograma == null || nuevoCronograma.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "La reprogramación no pudo aplicarse (datos insuficientes).",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean archivado = cuotaDAO.archivarCronogramaActual(
                creditoSeleccionado.getCodigoContrato(),
                "REPROGRAMACION_PLAZO",
                "Reprogramación: nuevas cuotas = " + nuevasCuotas +
                        ", nueva tasa anual = " + nuevaTasa,
                "sistema"
        );
        if (!archivado) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo archivar el cronograma anterior. Operación cancelada.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean okCronograma = cuotaDAO.actualizarCronogramaCompleto(
                creditoSeleccionado.getCodigoContrato(), nuevoCronograma);
        if (!okCronograma) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo actualizar el cronograma. Operación cancelada.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean contratoOk = creditoDAO.actualizarContratoReprogramado(
                creditoSeleccionado.getCodigoContrato(),
                nuevaTasa,
                nuevasCuotas
        );
        if (contratoOk) {
            creditoSeleccionado.setNumeroCuotas(nuevasCuotas);
            creditoSeleccionado.setTasaInteresCompensatorio(nuevaTasa);
            actualizarResumen(creditoSeleccionado);
        }

        historialDAO.registrarCambio(
                creditoSeleccionado.getCodigoContrato(),
                "REPROGRAMACION_PLAZO",
                "Reprogramación: nuevas cuotas = " + nuevasCuotas +
                        ", nueva tasa anual = " + nuevaTasa,
                "sistema"
        );
        cuotaTableModel.setCuotas(nuevoCronograma);

        JOptionPane.showMessageDialog(this,
                "Reprogramación aplicada y cronograma recalculado.",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void refinanciarCredito(ActionEvent e) {
        if (creditoSeleccionado == null) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione primero un crédito.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal saldoVivo = BigDecimal.ZERO;
        for (Cuota c : cuotaTableModel.getCuotas()) {
            if (c.getEstadoCuota() == null ||
                    !c.getEstadoCuota().equalsIgnoreCase("PAGADA")) {
                if (c.getCapital() != null) {
                    saldoVivo = saldoVivo.add(c.getCapital());
                }
            }
        }

        if (saldoVivo.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Este crédito no tiene saldo pendiente para refinanciar.",
                    "Aviso",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        DialogoRefinanciamientoCredito dlg = new DialogoRefinanciamientoCredito(
                SwingUtilities.getWindowAncestor(this),
                creditoSeleccionado,
                saldoVivo
        );
        dlg.setVisible(true);

        if (!dlg.isGuardado()) return;

        BigDecimal montoNuevo = dlg.getMontoNuevo();
        int nuevasCuotas = dlg.getNuevasCuotas();
        BigDecimal nuevaTasa = dlg.getNuevaTasa();
        String fechaTxt = dlg.getFechaDesembolsoTexto();
        
        Date fechaDesembolso;
        try {
            fechaDesembolso = new SimpleDateFormat("yyyy-MM-dd").parse(fechaTxt);
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Fecha de desembolso inválida (use formato yyyy-MM-dd).",
                    "Validación",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int nuevoContrato = creditoDAO.crearCreditoParaCliente(
                cliente.getCodigoCliente(),
                montoNuevo,
                nuevasCuotas,
                nuevaTasa,
                fechaDesembolso
        );

        if (nuevoContrato <= 0) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo crear el nuevo crédito para refinanciamiento.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean archivado = cuotaDAO.archivarCronogramaActual(
                creditoSeleccionado.getCodigoContrato(),
                "REFINANCIACION",
                "Refinanciamiento al contrato " + nuevoContrato +
                        " por monto S/ " + montoNuevo,
                "sistema"
        );
        if (!archivado) {
            JOptionPane.showMessageDialog(this,
                    "Se creó el nuevo crédito pero NO se pudo archivar el cronograma anterior.\n" +
                    "Revise el historial manualmente.",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }

        boolean marcado = creditoDAO.marcarContratoComoRefinanciado(
                creditoSeleccionado.getCodigoContrato()
        );
        if (!marcado) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo marcar el contrato original como REFINANCIADO.\n" +
                    "Revise el estado del contrato en la base de datos.",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }

        historialDAO.registrarCambio(
                creditoSeleccionado.getCodigoContrato(),
                "REFINANCIACION",
                "Refinanciamiento al contrato " + nuevoContrato +
                        " por monto S/ " + montoNuevo,
                "sistema"
        );
        
        cargarListaDeCreditos();
        seleccionarContratoEnCombo(nuevoContrato);

        JOptionPane.showMessageDialog(this,
                "Refinanciamiento realizado. Se creó el contrato " + nuevoContrato + ".",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void verHistorialCronograma(ActionEvent e) {
        if (creditoSeleccionado == null) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione primero un crédito.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        DialogoHistorialCronograma dlg = new DialogoHistorialCronograma(
                SwingUtilities.getWindowAncestor(this),
                creditoSeleccionado.getCodigoContrato(),
                historialDAO
        );
        dlg.setVisible(true);
    }

    private void verHistorialCronograma() {
        verHistorialCronograma(null);
    }

    /**
     * Genera un PDF del cronograma del crédito seleccionado
     * (sin la columna de estado) usando CronogramaPdfExporter.
     */
    private void descargarCronograma(ActionEvent e) {
        if (creditoSeleccionado == null) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione primero un crédito.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (cuotaTableModel == null || cuotaTableModel.getCuotas() == null
                || cuotaTableModel.getCuotas().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay cronograma para exportar.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar cronograma como PDF");
        chooser.setSelectedFile(new File(
                "cronograma_contrato_" + creditoSeleccionado.getCodigoContrato() + ".pdf"
        ));

        int op = chooser.showSaveDialog(this);
        if (op != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File destino = chooser.getSelectedFile();
        try {
            CronogramaPdfExporter.exportar(
                    creditoSeleccionado,
                    cliente,
                    cuotaTableModel.getCuotas(),
                    destino
            );
            JOptionPane.showMessageDialog(this,
                    "Cronograma guardado en:\n" + destino.getAbsolutePath(),
                    "PDF generado",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al generar el PDF: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /* =========================================================
     *  MÉTODOS AUXILIARES DE FORMATO
     * ========================================================= */

    private String formatearMoneda(BigDecimal valor) {
        if (valor == null) return "S/ 0.00";
        return String.format("S/ %,.2f", valor);
    }

    private String formatearPorcentaje(BigDecimal tasa) {
        if (tasa == null) return "-";
        return String.format("%,.2f %%", tasa);
    }

    private String formatearFecha(Date fecha) {
        if (fecha == null) return "-";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(fecha);
    }
}

