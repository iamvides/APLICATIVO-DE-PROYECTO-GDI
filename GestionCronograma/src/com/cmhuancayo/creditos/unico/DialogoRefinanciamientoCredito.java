package com.cmhuancayo.creditos.unico;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class DialogoRefinanciamientoCredito extends JDialog {

    private final Credito credito;
    private final BigDecimal saldoVivo;

    private JTextField txtMontoNuevo;
    private JTextField txtNuevasCuotas;
    private JTextField txtNuevaTasa;
    private JTextField txtFechaDesembolso;

    private boolean guardado = false;
    private BigDecimal montoNuevo;
    private int nuevasCuotas;
    private BigDecimal nuevaTasa;
    private String fechaDesembolsoTexto;

    public DialogoRefinanciamientoCredito(Window owner, Credito credito, BigDecimal saldoVivo) {
        super(owner, "Refinanciar Crédito " + credito.getCodigoContrato(), ModalityType.APPLICATION_MODAL);
        this.credito = credito;
        this.saldoVivo = saldoVivo;
        initUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        EstilosUI.estilizarDialogo(this);
        setLayout(new BorderLayout(10, 10));

        JPanel panelInfo = new JPanel(new GridLayout(0, 2, 10, 10));
        panelInfo.setOpaque(false);
        panelInfo.setBorder(EstilosUI.BORDE_PANEL_INTERNO);

        String saldoStr = saldoVivo != null ? saldoVivo.setScale(2, BigDecimal.ROUND_HALF_UP).toString() : "0.00";
        String tasaActualStr = credito.getTasaInteresCompensatorio() != null
                ? credito.getTasaInteresCompensatorio().setScale(2, BigDecimal.ROUND_HALF_UP).toString()
                : "0.00";
        
        panelInfo.add(new JLabel("Saldo vivo actual (S/):"));
        JLabel lblSaldoVivo = new JLabel(saldoStr);
        lblSaldoVivo.setFont(EstilosUI.FONT_SUBTITULO);
        panelInfo.add(lblSaldoVivo);
        
        // Separador
        panelInfo.add(new JSeparator());
        panelInfo.add(new JSeparator());

        panelInfo.add(new JLabel("Monto NUEVO del crédito (S/):"));
        txtMontoNuevo = new JTextField(saldoStr);
        panelInfo.add(txtMontoNuevo);
        
        panelInfo.add(new JLabel("NUEVO número de cuotas:"));
        txtNuevasCuotas = new JTextField(String.valueOf(credito.getNumeroCuotas()));
        panelInfo.add(txtNuevasCuotas);

        panelInfo.add(new JLabel("NUEVA tasa anual (%):"));
        txtNuevaTasa = new JTextField(tasaActualStr);
        panelInfo.add(txtNuevaTasa);
        
        panelInfo.add(new JLabel("Fecha desembolso (yyyy-MM-dd):"));
        txtFechaDesembolso = new JTextField(java.time.LocalDate.now().toString());
        panelInfo.add(txtFechaDesembolso);

        // Estilizar componentes
        for (Component comp : panelInfo.getComponents()) {
            if (comp instanceof JTextField) EstilosUI.estilizarCampo((JTextField) comp);
            if (comp instanceof JLabel) EstilosUI.estilizarLabel((JLabel) comp);
        }

        add(panelInfo, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        EstilosUI.aplicarFondoClaro(panelBotones);
        panelBotones.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xDDDDDD)));
        
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

    private void guardar() {
        // ... (Lógica de guardado sin cambios)
        try {
            String montoTxt = txtMontoNuevo.getText().trim().replace(",", ".");
            BigDecimal monto = new BigDecimal(montoTxt);
            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "El monto del nuevo crédito debe ser mayor que cero.", "Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (saldoVivo != null && monto.compareTo(saldoVivo) < 0) {
                JOptionPane.showMessageDialog(this, "El monto del nuevo crédito no puede ser menor al saldo vivo del contrato.", "Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int nCuotas = Integer.parseInt(txtNuevasCuotas.getText().trim());
            if (nCuotas <= 0) {
                JOptionPane.showMessageDialog(this, "El número de cuotas debe ser mayor que cero.", "Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String tasaTxt = txtNuevaTasa.getText().trim().replace(",", ".");
            BigDecimal tasa = new BigDecimal(tasaTxt);
            if (tasa.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, "La tasa no puede ser negativa.", "Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String fechaTxt = txtFechaDesembolso.getText().trim();
            if (fechaTxt.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe indicar la fecha de desembolso.", "Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            this.montoNuevo = monto;
            this.nuevasCuotas = nCuotas;
            this.nuevaTasa = tasa;
            this.fechaDesembolsoTexto = fechaTxt;
            this.guardado = true;
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valores numéricos inválidos (monto, cuotas o tasa).", "Validación", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ... (Getters sin cambios)
    public boolean isGuardado() { return guardado; }
    public BigDecimal getMontoNuevo() { return montoNuevo; }
    public int getNuevasCuotas() { return nuevasCuotas; }
    public BigDecimal getNuevaTasa() { return nuevaTasa; }
    public String getFechaDesembolsoTexto() { return fechaDesembolsoTexto; }
}