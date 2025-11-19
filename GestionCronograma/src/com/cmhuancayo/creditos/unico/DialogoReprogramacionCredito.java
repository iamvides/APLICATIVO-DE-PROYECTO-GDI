package com.cmhuancayo.creditos.unico;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class DialogoReprogramacionCredito extends JDialog {

    private final Credito credito;
    private JTextField txtNuevasCuotas;
    private JTextField txtNuevaTasa;

    private boolean guardado = false;
    private int nuevasCuotas;
    private BigDecimal nuevaTasa;

    public DialogoReprogramacionCredito(Window owner, Credito credito) {
        super(owner, "Reprogramar Crédito " + credito.getCodigoContrato(), ModalityType.APPLICATION_MODAL);
        this.credito = credito;
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
        
        String tasaActualStr = credito.getTasaInteresCompensatorio() != null
                ? credito.getTasaInteresCompensatorio().setScale(2, BigDecimal.ROUND_HALF_UP).toString()
                : "0.00";
        
        panelInfo.add(new JLabel("Cuotas actuales:"));
        panelInfo.add(new JLabel(String.valueOf(credito.getNumeroCuotas())));

        panelInfo.add(new JLabel("Tasa anual actual (%):"));
        panelInfo.add(new JLabel(tasaActualStr));
        
        // Separador
        panelInfo.add(new JSeparator());
        panelInfo.add(new JSeparator());

        txtNuevasCuotas = new JTextField(String.valueOf(credito.getNumeroCuotas()));
        txtNuevaTasa = new JTextField(tasaActualStr);
        
        panelInfo.add(new JLabel("NUEVO número de cuotas:"));
        panelInfo.add(txtNuevasCuotas);

        panelInfo.add(new JLabel("NUEVA tasa anual (%):"));
        panelInfo.add(txtNuevaTasa);
        
        // Estilizar componentes
        for (Component comp : panelInfo.getComponents()) {
            if (comp instanceof JTextField) EstilosUI.estilizarCampo((JTextField) comp);
            if (comp instanceof JLabel) EstilosUI.estilizarLabel((JLabel) comp);
        }

        add(panelInfo, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        EstilosUI.aplicarFondoClaro(panelBotones);
        panelBotones.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xDDDDDD)));
        
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

    private void guardar() {
        // ... (Lógica de guardado sin cambios)
        try {
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

            this.nuevasCuotas = nCuotas;
            this.nuevaTasa = tasa;
            this.guardado = true;
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valores numéricos inválidos (cuotas o tasa).", "Validación", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isGuardado() { return guardado; }
    public int getNuevasCuotas() { return nuevasCuotas; }
    public BigDecimal getNuevaTasa() { return nuevaTasa; }
}