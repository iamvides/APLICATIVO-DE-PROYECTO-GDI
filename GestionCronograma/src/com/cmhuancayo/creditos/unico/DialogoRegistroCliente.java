package com.cmhuancayo.creditos.unico;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Diálogo para el registro COMPLETO de un nuevo cliente.
 *
 * Flujo de uso:
 *  - El usuario llena los 4 pasos (pestañas):
 *      1) Datos personales
 *      2) Domicilio
 *      3) Actividad e ingresos
 *      4) Otros (teléfono, scoring y dependientes)
 *  - Al pulsar "Guardar Cliente" se valida la información básica
 *    (nombres, DNI, fechas, números) y se invoca a
 *    {@link ClienteDAO#registrarClienteCompleto(...)}.
 *  - Si todo es correcto, se muestra un mensaje de éxito,
 *    se marca la bandera guardado = true y se cierra el diálogo.
 *
 * Este formulario solo se encarga de la captura y validación de datos;
 * la lógica de persistencia está en {@link ClienteDAO}.
 */
public class DialogoRegistroCliente extends JDialog {

    /** DAO responsable de registrar el cliente completo en la base de datos. */
    private final ClienteDAO clienteDAO;

    // ---------------------------------------------------------------------
    //  Componentes de entrada (campos de formulario)
    // ---------------------------------------------------------------------

    // Datos personales
    private JTextField txtApellidoPaterno;
    private JTextField txtApellidoMaterno;
    private JTextField txtNombres;
    private JTextField txtDni;
    private JComboBox<String> cmbSexo;
    private JTextField txtFechaNacimiento;
    private JComboBox<String> cmbEstadoCivil;
    private JComboBox<String> cmbGradoInstruccion;

    // Domicilio
    private JTextField txtDireccion;
    private JTextField txtReferencia;
    private JTextField txtPropietario;
    private JTextField txtAnioResidencia;
    private JTextField txtCodigoSuministro;
    private JComboBox<String> cmbCondicionVivienda;

    // Actividad / ingresos
    private JTextField txtTipoActividad;
    private JTextField txtNombreNegocio;
    private JTextField txtDireccionNegocio;
    private JTextField txtTelefonoNegocio;
    private JTextField txtFechaInicioActividad;
    private JTextField txtSectorEconomico;
    private JTextField txtIngresoMensual;

    // Otros
    private JTextField txtTelefonoCelular;
    private JTextField txtScoring;
    private JTextField txtNumDependientes;

    /** Indica si el registro se realizó correctamente (para el llamador). */
    private boolean guardado = false;

    /**
     * Construye el diálogo modal para registrar un nuevo cliente.
     *
     * @param owner      ventana padre (para centrar el diálogo).
     * @param clienteDAO DAO que ejecutará el procedimiento almacenado de inserción.
     */
    public DialogoRegistroCliente(Window owner, ClienteDAO clienteDAO) {
        super(owner, "Registrar Nuevo Cliente", ModalityType.APPLICATION_MODAL);
        this.clienteDAO = clienteDAO;
        initUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    /**
     * Configura la estructura visual general del diálogo:
     *  - Pestañas para agrupar campos.
     *  - Panel inferior de botones (Guardar / Cancelar).
     */
    private void initUI() {
        EstilosUI.estilizarDialogo(this); // Estilo base del diálogo
        setLayout(new BorderLayout(10, 10));

        // Pestañas por secciones
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(EstilosUI.FONT_SUBTITULO);
        tabs.addTab(" 1. Datos Personales ", crearPanelDatosPersonales());
        tabs.addTab(" 2. Domicilio ", crearPanelDomicilio());
        tabs.addTab(" 3. Actividad e Ingresos ", crearPanelActividad());
        tabs.addTab(" 4. Otros ", crearPanelOtros());

        add(tabs, BorderLayout.CENTER);

        // Panel inferior de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        EstilosUI.aplicarFondoClaro(panelBotones);
        panelBotones.setBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xDDDDDD))
        );

        JButton btnGuardar = new JButton("Guardar Cliente");
        JButton btnCancelar = new JButton("Cancelar");
        EstilosUI.estilizarBotonPrincipal(btnGuardar);
        EstilosUI.estilizarBotonSecundario(btnCancelar);

        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> dispose());

        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);

        add(panelBotones, BorderLayout.SOUTH);
    }

    /**
     * Crea un panel base con estilo homogéneo para cada pestaña:
     *  - Fondo blanco
     *  - Borde interno suave
     *  - GridLayout de 2 columnas
     */
    private JPanel crearPanelConEstilo() {
        JPanel p = new JPanel(new GridLayout(0, 2, 10, 10)); // Más espacio
        p.setBackground(EstilosUI.BLANCO);
        p.setBorder(EstilosUI.BORDE_PANEL_INTERNO);
        return p;
    }

    /**
     * Recorre los componentes del panel y aplica los estilos
     * definidos en {@link EstilosUI} según el tipo:
     *  - JTextField -> estilizarCampo
     *  - JComboBox  -> estilizarComboBox
     *  - JLabel     -> estilizarLabel
     */
    private void estilizarCamposPanel(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JTextField) {
                EstilosUI.estilizarCampo((JTextField) comp);
            } else if (comp instanceof JComboBox) {
                EstilosUI.estilizarComboBox((JComboBox<?>) comp);
            } else if (comp instanceof JLabel) {
                EstilosUI.estilizarLabel((JLabel) comp);
            }
        }
    }

    // ---------------------------------------------------------------------
    //  Pestaña 1: Datos personales
    // ---------------------------------------------------------------------
    private JPanel crearPanelDatosPersonales() {
        JPanel p = crearPanelConEstilo();

        txtApellidoPaterno = new JTextField();
        txtApellidoMaterno = new JTextField();
        txtNombres = new JTextField();
        txtDni = new JTextField();
        cmbSexo = new JComboBox<>(new String[]{"M", "F"});
        txtFechaNacimiento = new JTextField("1990-01-01");
        cmbEstadoCivil = new JComboBox<>(new String[]{"SOLTERO", "CASADO", "DIVORCIADO", "VIUDO"});
        cmbGradoInstruccion = new JComboBox<>(new String[]{"ANALFABETO","PRIMARIA","SECUNDARIA","TECNICO","SUPERIOR"});

        p.add(new JLabel("Apellido Paterno:"));       p.add(txtApellidoPaterno);
        p.add(new JLabel("Apellido Materno:"));       p.add(txtApellidoMaterno);
        p.add(new JLabel("Nombres:"));                p.add(txtNombres);
        p.add(new JLabel("DNI:"));                    p.add(txtDni);
        p.add(new JLabel("Sexo:"));                   p.add(cmbSexo);
        p.add(new JLabel("Fecha Nac. (yyyy-MM-dd):"));p.add(txtFechaNacimiento);
        p.add(new JLabel("Estado civil:"));           p.add(cmbEstadoCivil);
        p.add(new JLabel("Grado instrucción:"));      p.add(cmbGradoInstruccion);

        estilizarCamposPanel(p);
        return p;
    }

    // ---------------------------------------------------------------------
    //  Pestaña 2: Domicilio
    // ---------------------------------------------------------------------
    private JPanel crearPanelDomicilio() {
        JPanel p = crearPanelConEstilo();

        txtDireccion = new JTextField();
        txtReferencia = new JTextField();
        cmbCondicionVivienda = new JComboBox<>(new String[]{"PROPIA","ALQUILADA","FAMILIAR","OTRO"});
        txtPropietario = new JTextField();
        txtAnioResidencia = new JTextField("3");
        txtCodigoSuministro = new JTextField();

        p.add(new JLabel("Dirección:"));          p.add(txtDireccion);
        p.add(new JLabel("Referencia:"));         p.add(txtReferencia);
        p.add(new JLabel("Condición vivienda:")); p.add(cmbCondicionVivienda);
        p.add(new JLabel("Propietario:"));        p.add(txtPropietario);
        p.add(new JLabel("Años residencia:"));    p.add(txtAnioResidencia);
        p.add(new JLabel("Código suministro:"));  p.add(txtCodigoSuministro);

        estilizarCamposPanel(p);
        return p;
    }

    // ---------------------------------------------------------------------
    //  Pestaña 3: Actividad e ingresos
    // ---------------------------------------------------------------------
    private JPanel crearPanelActividad() {
        JPanel p = crearPanelConEstilo();

        txtTipoActividad = new JTextField("DEPENDIENTE");
        txtNombreNegocio = new JTextField("Sin negocio");
        txtDireccionNegocio = new JTextField();
        txtTelefonoNegocio = new JTextField();
        txtFechaInicioActividad = new JTextField("2015-01-01");
        txtSectorEconomico = new JTextField("OTROS");
        txtIngresoMensual = new JTextField("1500.00");

        p.add(new JLabel("Tipo actividad:"));            p.add(txtTipoActividad);
        p.add(new JLabel("Nombre negocio:"));            p.add(txtNombreNegocio);
        p.add(new JLabel("Dirección trabajo/negocio:")); p.add(txtDireccionNegocio);
        p.add(new JLabel("Teléfono trabajo:"));          p.add(txtTelefonoNegocio);
        p.add(new JLabel("Fecha inicio act. (yyyy-MM-dd):")); p.add(txtFechaInicioActividad);
        p.add(new JLabel("Sector económico:"));          p.add(txtSectorEconomico);
        p.add(new JLabel("Ingreso mensual (S/):"));      p.add(txtIngresoMensual);

        estilizarCamposPanel(p);
        return p;
    }

    // ---------------------------------------------------------------------
    //  Pestaña 4: Otros (Teléfono, scoring, dependientes)
    // ---------------------------------------------------------------------
    private JPanel crearPanelOtros() {
        JPanel p = crearPanelConEstilo();

        txtTelefonoCelular = new JTextField();
        txtScoring = new JTextField("80.00");
        txtNumDependientes = new JTextField("0");

        p.add(new JLabel("Teléfono celular:"));    p.add(txtTelefonoCelular);
        p.add(new JLabel("Scoring:"));             p.add(txtScoring);
        p.add(new JLabel("N° dependientes:"));     p.add(txtNumDependientes);

        estilizarCamposPanel(p);
        return p;
    }

    // ---------------------------------------------------------------------
    //  Lógica de guardado y validación
    // ---------------------------------------------------------------------

    /**
     * Valida los datos mínimos, convierte los campos de texto a tipos
     * adecuados (Date, BigDecimal, int) e invoca al DAO para registrar
     * el cliente completo en la base de datos.
     *
     * Si todo es correcto:
     *   - guardado = true
     *   - se cierra el diálogo.
     *
     * Ante cualquier error de validación o SQL se muestra un JOptionPane.
     */
    private void guardar() {
        try {
            // --------------------------------------------------------------
            // 1) Validación básica de campos obligatorios
            // --------------------------------------------------------------
            String apP = txtApellidoPaterno.getText().trim();
            String apM = txtApellidoMaterno.getText().trim();
            String nom = txtNombres.getText().trim();
            String dni = txtDni.getText().trim();

            if (apP.isEmpty() || nom.isEmpty() || dni.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Apellido paterno, nombres y DNI son obligatorios.",
                        "Validación",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            if (dni.length() != 8) {
                JOptionPane.showMessageDialog(
                        this,
                        "El DNI debe tener 8 dígitos.",
                        "Validación",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            // --------------------------------------------------------------
            // 2) Conversión de fechas (nacimiento y actividad)
            // --------------------------------------------------------------
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date fechaNac;
            Date fechaInicioAct;

            try {
                fechaNac = sdf.parse(txtFechaNacimiento.getText().trim());
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Fecha de nacimiento inválida. Formato: yyyy-MM-dd",
                        "Validación",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            try {
                fechaInicioAct = sdf.parse(txtFechaInicioActividad.getText().trim());
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Fecha de inicio de actividad inválida. Formato: yyyy-MM-dd",
                        "Validación",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            // --------------------------------------------------------------
            // 3) Lectura de combos y campos de texto "simples"
            // --------------------------------------------------------------
            String sexo = (String) cmbSexo.getSelectedItem();
            String estadoCivil = (String) cmbEstadoCivil.getSelectedItem();
            String gradoInstr = (String) cmbGradoInstruccion.getSelectedItem();

            String direccion = txtDireccion.getText().trim();
            String referencia = txtReferencia.getText().trim();
            String condicionVivienda = (String) cmbCondicionVivienda.getSelectedItem();
            String propietario = txtPropietario.getText().trim();
            int anioResidencia = Integer.parseInt(txtAnioResidencia.getText().trim());
            String codigoSuministro = txtCodigoSuministro.getText().trim();

            String tipoActividad = txtTipoActividad.getText().trim();
            String nombreNegocio = txtNombreNegocio.getText().trim();
            String direccionNegocio = txtDireccionNegocio.getText().trim();
            String telefonoNegocio = txtTelefonoNegocio.getText().trim();
            String sectorEco = txtSectorEconomico.getText().trim();
            BigDecimal ingreso = new BigDecimal(txtIngresoMensual.getText().trim());

            String telCel = txtTelefonoCelular.getText().trim();
            BigDecimal scoring = new BigDecimal(txtScoring.getText().trim());
            int numDep = Integer.parseInt(txtNumDependientes.getText().trim());

            // --------------------------------------------------------------
            // 4) Llamada al DAO para insertar en BD vía SP
            // --------------------------------------------------------------
            boolean ok = clienteDAO.registrarClienteCompleto(
                    apP, apM, nom, sexo, fechaNac,
                    estadoCivil, gradoInstr, dni,
                    direccion, referencia, condicionVivienda, propietario,
                    anioResidencia, codigoSuministro,
                    tipoActividad, nombreNegocio, direccionNegocio, telefonoNegocio,
                    fechaInicioAct, sectorEco, ingreso,
                    telCel, scoring, numDep
            );

            if (ok) {
                JOptionPane.showMessageDialog(
                        this,
                        "Cliente registrado correctamente.",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE
                );
                guardado = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "No se pudo registrar el cliente.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (NumberFormatException ex) {
            // Cualquier problema convirtiendo números (años, ingreso, scoring, dependientes)
            JOptionPane.showMessageDialog(
                    this,
                    "Datos numéricos inválidos (años, ingreso, scoring o dependientes).",
                    "Validación",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * @return true si el cliente fue registrado exitosamente
     *         (es decir, se pulsó Guardar y la operación en BD fue OK).
     */
    public boolean isGuardado() {
        return guardado;
    }
}
