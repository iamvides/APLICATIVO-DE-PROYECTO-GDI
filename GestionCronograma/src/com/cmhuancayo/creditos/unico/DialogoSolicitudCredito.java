package com.cmhuancayo.creditos.unico;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Diálogo para CREAR un NUEVO CRÉDITO para un cliente.
 *
 * Funciones:
 *  - Muestra un pequeño formulario con:
 *      • Monto solicitado
 *      • Número de cuotas
 *      • Tasa anual (%)
 *      • Fecha de desembolso
 *  - Valida los datos ingresados.
 *  - Llama a {@link CreditoDAO#crearCreditoParaCliente(int, BigDecimal, int, BigDecimal, Date)}
 *    que se encarga de:
 *      • Crear la solicitud, pre_aprobación, contrato y cronograma en PostgreSQL.
 *  - Si todo va bien:
 *      • Marca la bandera {@code guardado = true}
 *      • Guarda el código de contrato nuevo en {@code codigoContratoCreado}
 *      • Cierra el diálogo.
 *
 * El panel que llame a este diálogo (por ejemplo PanelGestionCliente)
 * puede luego refrescar la lista de créditos del cliente usando CreditoDAO.
 */
public class DialogoSolicitudCredito extends JDialog {

    /** Cliente al que se le generará el nuevo crédito. */
    private final Cliente cliente;
    /** DAO que encapsula la lógica de creación de créditos en BD. */
    private final CreditoDAO creditoDAO;

    // Campos de formulario
    private JTextField txtMonto;
    private JTextField txtCuotas;
    private JTextField txtTasa;
    private JTextField txtFechaDesembolso;

    // Resultado
    private boolean guardado = false;
    private int codigoContratoCreado = -1;

    /**
     * Construye el diálogo modal para registrar una nueva solicitud de crédito.
     *
     * @param owner      ventana padre para centrar el diálogo.
     * @param cliente    cliente seleccionado en la tabla principal.
     * @param creditoDAO DAO que creará el crédito en la base de datos.
     */
    public DialogoSolicitudCredito(Window owner, Cliente cliente, CreditoDAO creditoDAO) {
        super(owner, "Solicitar Préstamo para " + cliente.getNombres(), ModalityType.APPLICATION_MODAL);
        this.cliente = cliente;
        this.creditoDAO = creditoDAO;
        initUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    /**
     * Inicializa la interfaz:
     *  - Panel central con campos de monto, cuotas, tasa y fecha de desembolso.
     *  - Panel inferior con botones "Cancelar" y "Crear Crédito".
     */
    private void initUI() {
        EstilosUI.estilizarDialogo(this);
        setLayout(new BorderLayout(10, 10));

        // --------------------------------------------------------------
        // Panel central: datos del crédito a crear
        // --------------------------------------------------------------
        JPanel panelCampos = new JPanel(new GridLayout(0, 2, 10, 10));
        panelCampos.setOpaque(false);
        panelCampos.setBorder(EstilosUI.BORDE_PANEL_INTERNO);

        panelCampos.add(new JLabel("Monto (S/):"));
        txtMonto = new JTextField("5000.00");
        panelCampos.add(txtMonto);

        panelCampos.add(new JLabel("N° de cuotas:"));
        txtCuotas = new JTextField("12");
        panelCampos.add(txtCuotas);

        panelCampos.add(new JLabel("Tasa anual (%):"));
        txtTasa = new JTextField("15.00");
        panelCampos.add(txtTasa);

        panelCampos.add(new JLabel("Fecha desembolso (yyyy-MM-dd):"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        txtFechaDesembolso = new JTextField(sdf.format(new Date()));
        panelCampos.add(txtFechaDesembolso);

        // Estilos visuales (rojo/blanco)
        for (Component comp : panelCampos.getComponents()) {
            if (comp instanceof JTextField) {
                EstilosUI.estilizarCampo((JTextField) comp);
            }
            if (comp instanceof JLabel) {
                EstilosUI.estilizarLabel((JLabel) comp);
            }
        }

        add(panelCampos, BorderLayout.CENTER);

        // --------------------------------------------------------------
        // Panel inferior: botones de acción
        // --------------------------------------------------------------
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        EstilosUI.aplicarFondoClaro(panelBotones);
        panelBotones.setBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xDDDDDD))
        );

        JButton btnGuardar = new JButton("Crear Crédito");
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
     * Valida los datos ingresados y, si son correctos, llama a CreditoDAO
     * para crear el crédito completo en la base de datos.
     *
     * Validaciones:
     *  - Monto > 0
     *  - Cuotas > 0
     *  - Tasa numérica y NO negativa
     *  - Fecha con formato yyyy-MM-dd
     */
    private void guardar() {
        try {
            // 1. Parseo y validación numérica
            BigDecimal monto = new BigDecimal(txtMonto.getText().trim());
            int cuotas = Integer.parseInt(txtCuotas.getText().trim());
            BigDecimal tasa = new BigDecimal(txtTasa.getText().trim());

            if (monto.compareTo(BigDecimal.ZERO) <= 0 || cuotas <= 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "El monto y las cuotas deben ser mayores que cero.",
                        "Validación",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            // *** NUEVA VALIDACIÓN: la tasa no puede ser negativa ***
            if (tasa.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "La tasa anual no puede ser negativa.",
                        "Validación",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            // 2. Validación de fecha
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date fechaDesembolso;
            try {
                fechaDesembolso = sdf.parse(txtFechaDesembolso.getText().trim());
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Formato de fecha inválido. Use yyyy-MM-dd.",
                        "Validación",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            // 3. Llamada al DAO (SP sp_CrearCreditoClienteBasico en PostgreSQL)
            int nuevoContrato = creditoDAO.crearCreditoParaCliente(
                    cliente.getCodigoCliente(),
                    monto,
                    cuotas,
                    tasa,
                    fechaDesembolso
            );

            if (nuevoContrato > 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Crédito creado correctamente. Contrato N° " + nuevoContrato,
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE
                );
                guardado = true;
                codigoContratoCreado = nuevoContrato;
                dispose();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "No se pudo crear el crédito.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Monto, cuotas o tasa inválidos.",
                    "Validación",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------

    /** @return true si el crédito fue creado correctamente. */
    public boolean isGuardado() {
        return guardado;
    }

    /** @return código del contrato creado (o -1 si no se creó). */
    public int getCodigoContratoCreado() {
        return codigoContratoCreado;
    }
}
