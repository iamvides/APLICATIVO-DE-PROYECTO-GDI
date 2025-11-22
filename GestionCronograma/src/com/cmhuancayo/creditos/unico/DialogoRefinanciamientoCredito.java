package com.cmhuancayo.creditos.unico;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Diálogo para capturar los datos de un nuevo crédito de REFINANCIAMIENTO.
 *
 * Flujo típico:
 *  - Se muestra el saldo vivo del crédito original.
 *  - El usuario define:
 *      * Monto del nuevo crédito (>= saldo vivo).
 *      * Nuevo número de cuotas.
 *      * Nueva tasa anual.
 *      * Fecha de desembolso del nuevo crédito.
 *  - Al pulsar "Crear Crédito Refinanciado" se validan los datos y,
 *    si todo está correcto, se marcan los valores en las propiedades
 *    de salida (montoNuevo, nuevasCuotas, nuevaTasa, fechaDesembolsoTexto)
 *    y se marca guardado = true.
 *
 * La clase NO realiza inserciones en BD, solo recoge los datos
 * que luego usará la capa DAO/servicio.
 */
public class DialogoRefinanciamientoCredito extends JDialog {

    /** Crédito original que se está refinanciando (solo referencia). */
    private final Credito credito;

    /** Saldo vivo del crédito original (suma de capital pendiente). */
    private final BigDecimal saldoVivo;

    // Campos de entrada de datos
    private JTextField txtMontoNuevo;
    private JTextField txtNuevasCuotas;
    private JTextField txtNuevaTasa;
    private JTextField txtFechaDesembolso;

    // Valores de salida
    private boolean guardado = false;
    private BigDecimal montoNuevo;
    private int nuevasCuotas;
    private BigDecimal nuevaTasa;
    private String fechaDesembolsoTexto;

    /**
     * Constructor.
     *
     * @param owner       Ventana padre (para modalidad y centrado).
     * @param credito     Crédito original que se está refinanciando.
     * @param saldoVivo   Saldo vivo actual del crédito (capital pendiente).
     */
    public DialogoRefinanciamientoCredito(Window owner,
                                          Credito credito,
                                          BigDecimal saldoVivo) {
        super(owner,
                "Refinanciar Crédito " + credito.getCodigoContrato(),
                ModalityType.APPLICATION_MODAL);

        this.credito = credito;
        this.saldoVivo = saldoVivo;

        initUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    /**
     * Inicializa componentes gráficos y aplica estilos.
     */
    private void initUI() {
        EstilosUI.estilizarDialogo(this);
        setLayout(new BorderLayout(10, 10));

        // Panel principal de datos
        JPanel panelInfo = new JPanel(new GridLayout(0, 2, 10, 10));
        panelInfo.setOpaque(false);
        panelInfo.setBorder(EstilosUI.BORDE_PANEL_INTERNO);

        // Texto amigable para saldo vivo y tasa actual
        String saldoStr = (saldoVivo != null)
                ? saldoVivo.setScale(2, RoundingMode.HALF_UP).toString()
                : "0.00";

        String tasaActualStr = (credito.getTasaInteresCompensatorio() != null)
                ? credito.getTasaInteresCompensatorio().setScale(2, RoundingMode.HALF_UP).toString()
                : "0.00";

        // Fila: saldo vivo actual
        panelInfo.add(new JLabel("Saldo vivo actual (S/):"));
        JLabel lblSaldoVivo = new JLabel(saldoStr);
        lblSaldoVivo.setFont(EstilosUI.FONT_SUBTITULO);
        panelInfo.add(lblSaldoVivo);

        // Pequeño separador visual
        panelInfo.add(new JSeparator());
        panelInfo.add(new JSeparator());

        // Fila: nuevo monto de crédito
        panelInfo.add(new JLabel("Monto NUEVO del crédito (S/):"));
        txtMontoNuevo = new JTextField(saldoStr); // por defecto: igual al saldo vivo
        panelInfo.add(txtMontoNuevo);

        // Fila: nuevo número de cuotas
        panelInfo.add(new JLabel("NUEVO número de cuotas:"));
        txtNuevasCuotas = new JTextField(String.valueOf(credito.getNumeroCuotas()));
        panelInfo.add(txtNuevasCuotas);

        // Fila: nueva tasa anual
        panelInfo.add(new JLabel("NUEVA tasa anual (%):"));
        txtNuevaTasa = new JTextField(tasaActualStr);
        panelInfo.add(txtNuevaTasa);

        // Fila: fecha de desembolso del nuevo crédito
        panelInfo.add(new JLabel("Fecha desembolso (yyyy-MM-dd):"));
        txtFechaDesembolso = new JTextField(java.time.LocalDate.now().toString());
        panelInfo.add(txtFechaDesembolso);

        // Aplicar estilo uniforme a labels y campos de texto
        for (Component comp : panelInfo.getComponents()) {
            if (comp instanceof JTextField) {
                EstilosUI.estilizarCampo((JTextField) comp);
            }
            if (comp instanceof JLabel) {
                EstilosUI.estilizarLabel((JLabel) comp);
            }
        }

        add(panelInfo, BorderLayout.CENTER);

        // Panel inferior con botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        EstilosUI.aplicarFondoClaro(panelBotones);
        panelBotones.setBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xDDDDDD))
        );

        JButton btnGuardar = new JButton("Crear Crédito Refinanciado");
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
     * Valida los datos ingresados y, si son correctos, llena los atributos
     * de salida (montoNuevo, nuevasCuotas, nuevaTasa, fechaDesembolsoTexto)
     * y marca guardado = true.
     *
     * Si hay errores de validación, muestra mensajes con JOptionPane
     * y NO cierra el diálogo.
     */
    private void guardar() {
        try {
            // --- 1) Monto del nuevo crédito ---
            String montoTxt = txtMontoNuevo.getText().trim().replace(",", ".");
            BigDecimal monto = new BigDecimal(montoTxt);

            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "El monto del nuevo crédito debe ser mayor que cero.",
                        "Validación",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            // No permitir refinanciar por un monto menor al saldo vivo.
            if (saldoVivo != null && monto.compareTo(saldoVivo) < 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "El monto del nuevo crédito no puede ser menor al saldo vivo del contrato.",
                        "Validación",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            // --- 2) Número de cuotas ---
            int nCuotas = Integer.parseInt(txtNuevasCuotas.getText().trim());
            if (nCuotas <= 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "El número de cuotas debe ser mayor que cero.",
                        "Validación",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            // --- 3) Tasa anual ---
            String tasaTxt = txtNuevaTasa.getText().trim().replace(",", ".");
            BigDecimal tasa = new BigDecimal(tasaTxt);
            if (tasa.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "La tasa no puede ser negativa.",
                        "Validación",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            // --- 4) Fecha desembolso (solo validamos que no esté vacía,
            //         el parseo real se suele hacer en la capa que llama) ---
            String fechaTxt = txtFechaDesembolso.getText().trim();
            if (fechaTxt.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Debe indicar la fecha de desembolso.",
                        "Validación",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            // Si llegó hasta aquí, todos los datos son válidos:
            this.montoNuevo = monto;
            this.nuevasCuotas = nCuotas;
            this.nuevaTasa = tasa;
            this.fechaDesembolsoTexto = fechaTxt;
            this.guardado = true;

            // Cerrar el diálogo
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Valores numéricos inválidos (monto, cuotas o tasa).",
                    "Validación",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================================================
    // Getters de salida para que el llamador lea los datos
    // después de que guardado == true
    // =========================================================

    /**
     * @return true si el usuario pulsó "Crear Crédito Refinanciado"
     *         y las validaciones fueron correctas.
     */
    public boolean isGuardado() {
        return guardado;
    }

    /** @return Monto del nuevo crédito refinanciado. */
    public BigDecimal getMontoNuevo() {
        return montoNuevo;
    }

    /** @return Número de cuotas del nuevo crédito. */
    public int getNuevasCuotas() {
        return nuevasCuotas;
    }

    /** @return Nueva tasa anual ingresada. */
    public BigDecimal getNuevaTasa() {
        return nuevaTasa;
    }

    /**
     * @return Fecha de desembolso en texto (yyyy-MM-dd) tal como
     *         la escribió el usuario.
     */
    public String getFechaDesembolsoTexto() {
        return fechaDesembolsoTexto;
    }
}
