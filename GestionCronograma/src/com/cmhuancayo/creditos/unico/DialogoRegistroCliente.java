package com.cmhuancayo.creditos.unico;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DialogoRegistroCliente extends JDialog {

    private final ClienteDAO clienteDAO;

    // ... (Todos los JTextField, JComboBox, etc.)
    private JTextField txtApellidoPaterno, txtApellidoMaterno, txtNombres, txtDni;
    private JComboBox<String> cmbSexo;
    private JTextField txtFechaNacimiento;
    private JComboBox<String> cmbEstadoCivil, cmbGradoInstruccion;
    private JTextField txtDireccion, txtReferencia, txtPropietario, txtAnioResidencia, txtCodigoSuministro;
    private JComboBox<String> cmbCondicionVivienda;
    private JTextField txtTipoActividad, txtNombreNegocio, txtDireccionNegocio, txtTelefonoNegocio;
    private JTextField txtFechaInicioActividad, txtSectorEconomico, txtIngresoMensual;
    private JTextField txtTelefonoCelular, txtScoring, txtNumDependientes;

    private boolean guardado = false;

    public DialogoRegistroCliente(Window owner, ClienteDAO clienteDAO) {
        super(owner, "Registrar Nuevo Cliente", ModalityType.APPLICATION_MODAL);
        this.clienteDAO = clienteDAO;
        initUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        EstilosUI.estilizarDialogo(this); // Estilo base del diálogo
        setLayout(new BorderLayout(10, 10));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(EstilosUI.FONT_SUBTITULO);
        tabs.addTab(" 1. Datos Personales ", crearPanelDatosPersonales());
        tabs.addTab(" 2. Domicilio ", crearPanelDomicilio());
        tabs.addTab(" 3. Actividad e Ingresos ", crearPanelActividad());
        tabs.addTab(" 4. Otros ", crearPanelOtros());
        
        add(tabs, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        EstilosUI.aplicarFondoClaro(panelBotones);
        panelBotones.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xDDDDDD)));

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

    private JPanel crearPanelConEstilo() {
        JPanel p = new JPanel(new GridLayout(0, 2, 10, 10)); // Más espacio
        p.setBackground(EstilosUI.BLANCO);
        p.setBorder(EstilosUI.BORDE_PANEL_INTERNO);
        return p;
    }

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

        p.add(new JLabel("Apellido Paterno:"));  p.add(txtApellidoPaterno);
        p.add(new JLabel("Apellido Materno:"));  p.add(txtApellidoMaterno);
        p.add(new JLabel("Nombres:"));           p.add(txtNombres);
        p.add(new JLabel("DNI:"));               p.add(txtDni);
        p.add(new JLabel("Sexo:"));              p.add(cmbSexo);
        p.add(new JLabel("Fecha Nac. (yyyy-MM-dd):")); p.add(txtFechaNacimiento);
        p.add(new JLabel("Estado civil:"));      p.add(cmbEstadoCivil);
        p.add(new JLabel("Grado instrucción:")); p.add(cmbGradoInstruccion);
        
        estilizarCamposPanel(p);
        return p;
    }

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

    private JPanel crearPanelActividad() {
        JPanel p = crearPanelConEstilo();

        txtTipoActividad = new JTextField("DEPENDIENTE");
        txtNombreNegocio = new JTextField("Sin negocio");
        txtDireccionNegocio = new JTextField();
        txtTelefonoNegocio = new JTextField();
        txtFechaInicioActividad = new JTextField("2015-01-01");
        txtSectorEconomico = new JTextField("OTROS");
        txtIngresoMensual = new JTextField("1500.00");

        p.add(new JLabel("Tipo actividad:"));           p.add(txtTipoActividad);
        p.add(new JLabel("Nombre negocio:"));           p.add(txtNombreNegocio);
        p.add(new JLabel("Dirección trabajo/negocio:")); p.add(txtDireccionNegocio);
        p.add(new JLabel("Teléfono trabajo:"));         p.add(txtTelefonoNegocio);
        p.add(new JLabel("Fecha inicio act. (yyyy-MM-dd):")); p.add(txtFechaInicioActividad);
        p.add(new JLabel("Sector económico:"));         p.add(txtSectorEconomico);
        p.add(new JLabel("Ingreso mensual (S/):"));     p.add(txtIngresoMensual);

        estilizarCamposPanel(p);
        return p;
    }

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

    private void guardar() {
        // ... (La lógica de guardado no cambia)
        try {
            String apP = txtApellidoPaterno.getText().trim();
            String apM = txtApellidoMaterno.getText().trim();
            String nom = txtNombres.getText().trim();
            String dni = txtDni.getText().trim();
            if (apP.isEmpty() || nom.isEmpty() || dni.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Apellido paterno, nombres y DNI son obligatorios.",
                        "Validación",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (dni.length() != 8) {
                JOptionPane.showMessageDialog(this,
                        "El DNI debe tener 8 dígitos.",
                        "Validación",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date fechaNac;
            Date fechaInicioAct;
            try {
                fechaNac = sdf.parse(txtFechaNacimiento.getText().trim());
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(this, "Fecha de nacimiento inválida. Formato: yyyy-MM-dd", "Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                fechaInicioAct = sdf.parse(txtFechaInicioActividad.getText().trim());
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(this, "Fecha de inicio de actividad inválida. Formato: yyyy-MM-dd", "Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

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
                JOptionPane.showMessageDialog(this, "Cliente registrado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                guardado = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo registrar el cliente.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Datos numéricos inválidos (años, ingreso, scoring o dependientes).",
                    "Validación",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isGuardado() {
        return guardado;
    }
}