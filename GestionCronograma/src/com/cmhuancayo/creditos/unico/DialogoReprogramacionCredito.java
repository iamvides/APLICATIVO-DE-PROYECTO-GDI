package com.cmhuancayo.creditos.unico;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Diálogo para REPROGRAMAR un crédito existente (mismo contrato).
 *
 * Este formulario NO hace operaciones en BD directamente.
 * Su objetivo es:
 *   - Mostrar la tasa y número de cuotas actuales del crédito.
 *   - Permitir que el usuario ingrese:
 *       • Nuevo número de cuotas.
 *       • Nueva tasa anual.
 *   - Validar esos datos.
 *   - Guardarlos en atributos internos (nuevasCuotas, nuevaTasa)
 *     y marcar la bandera {@code guardado = true}.
 *
 * Después, el llamador (PanelGestionCliente u otro) puede:
 *   1) Leer los valores mediante getNuevasCuotas() y getNuevaTasa().
 *   2) Recalcular el cronograma con {@link CalculadoraCreditoService}.
 *   3) Actualizar la tabla cronograma_de_pago y el contrato
 *      usando {@link CuotaDAO} y {@link CreditoDAO}.
 */
public class DialogoReprogramacionCredito extends JDialog {

    /** Crédito original a reprogramar (solo lectura aquí). */
    private final Credito credito;

    // Campos de entrada
    private JTextField txtNuevasCuotas;
    private JTextField txtNuevaTasa;

    // Resultado
    private boolean guardado = false;
    private int nuevasCuotas;
    private BigDecimal nuevaTasa;

    /**
     * Construye el diálogo modal para reprogramar un crédito.
     *
     * @param owner   ventana padre para centrar el diálogo.
     * @param credito crédito seleccionado que se desea reprogramar.
     */
    public DialogoReprogramacionCredito(Window owner, Credito credito) {
        super(owner, "Reprogramar Crédito " + credito.getCodigoContrato(), ModalityType.APPLICATION_MODAL);
        this.credito = credito;
        initUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    /**
     * Configura la interfaz:
     *  - Panel central con datos actuales y nuevos parámetros.
     *  - Panel inferior con botones de acción (Aplicar / Cancelar).
     */
    private void initUI() {
        EstilosUI.estilizarDialogo(this);
        setLayout(new BorderLayout(10, 10));

        // -----------------------------------------------------------------
        // Panel central: muestra datos actuales y captura los nuevos
        // -----------------------------------------------------------------
        JPanel panelInfo = new JPanel(new GridLayout(0, 2, 10, 10));
        panelInfo.setOpaque(false);
        panelInfo.setBorder(EstilosUI.BORDE_PANEL_INTERNO);

        String tasaActualStr = credito.getTasaInteresCompensatorio() != null
                ? credito.getTasaInteresCompensatorio().setScale(2, BigDecimal.ROUND_HALF_UP).toString()
                : "0.00";

        // Datos actuales
        panelInfo.add(new JLabel("Cuotas actuales:"));
        panelInfo.add(new JLabel(String.valueOf(credito.getNumeroCuotas())));

        panelInfo.add(new JLabel("Tasa anual actual (%):"));
        panelInfo.add(new JLabel(tasaActualStr));

        // Separador visual
        panelInfo.add(new JSeparator());
        panelInfo.add(new JSeparator());

        // Entradas para nuevos valores
        txtNuevasCuotas = new JTextField(String.valueOf(credito.getNumeroCuotas()));
        txtNuevaTasa = new JTextField(tasaActualStr);

        panelInfo.add(new JLabel("NUEVO número de cuotas:"));
        panelInfo.add(txtNuevasCuotas);

        panelInfo.add(new JLabel("NUEVA tasa anual (%):"));
        panelInfo.add(txtNuevaTasa);

        // Estilizar campos y etiquetas
        for (Component comp : panelInfo.getComponents()) {
            if (comp instanceof JTextField) {
                EstilosUI.estilizarCampo((JTextField) comp);
            }
            if (comp instanceof JLabel) {
                EstilosUI.estilizarLabel((JLabel) comp);
            }
        }

        add(panelInfo, BorderLayout.CENTER);

        // -----------------------------------------------------------------
        // Panel inferior con botones
        // -----------------------------------------------------------------
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        EstilosUI.aplicarFondoClaro(panelBotones);
        panelBotones.setBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xDDDDDD))
        );

        JButton btnGuardar = new JButton("Aplicar Reprogramación");
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
     * Valida los nuevos parámetros de reprogramación:
     *   - Número de cuotas > 0
     *   - Tasa anual >= 0
     *
     * Si todo es correcto:
     *   - Guarda los valores en nuevasCuotas / nuevaTasa
     *   - Marca guardado = true
     *   - Cierra el diálogo.
     */
    private void guardar() {
        try {
            // Número de cuotas
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

            // Tasa anual
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

            // Asignamos resultados y marcamos OK
            this.nuevasCuotas = nCuotas;
            this.nuevaTasa = tasa;
            this.guardado = true;
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Valores numéricos inválidos (cuotas o tasa).",
                    "Validación",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // -----------------------------------------------------------------
    // Getters para que el llamador recupere el resultado del diálogo
    // -----------------------------------------------------------------

    /** @return true si el usuario aplicó la reprogramación correctamente. */
    public boolean isGuardado() {
        return guardado;
    }

    /** @return nuevo número de cuotas definido por el usuario. */
    public int getNuevasCuotas() {
        return nuevasCuotas;
    }

    /** @return nueva tasa anual (%) definida por el usuario. */
    public BigDecimal getNuevaTasa() {
        return nuevaTasa;
    }
}
