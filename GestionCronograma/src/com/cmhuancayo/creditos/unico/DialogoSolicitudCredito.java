package com.cmhuancayo.creditos.unico;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DialogoSolicitudCredito extends JDialog {

    private final Cliente cliente;
    private final CreditoDAO creditoDAO;

    private JTextField txtMonto;
    private JTextField txtCuotas;
    private JTextField txtTasa;
    private JTextField txtFechaDesembolso;

    private boolean guardado = false;
    private int codigoContratoCreado = -1;

    public DialogoSolicitudCredito(Window owner, Cliente cliente, CreditoDAO creditoDAO) {
        super(owner, "Solicitar Préstamo para " + cliente.getNombres(), ModalityType.APPLICATION_MODAL);
        this.cliente = cliente;
        this.creditoDAO = creditoDAO;
        initUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        EstilosUI.estilizarDialogo(this);
        setLayout(new BorderLayout(10, 10));

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

        // Estilizar todos los componentes
        for (Component comp : panelCampos.getComponents()) {
            if (comp instanceof JTextField) EstilosUI.estilizarCampo((JTextField) comp);
            if (comp instanceof JLabel) EstilosUI.estilizarLabel((JLabel) comp);
        }

        add(panelCampos, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        EstilosUI.aplicarFondoClaro(panelBotones);
        panelBotones.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xDDDDDD)));
        
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

    private void guardar() {
        // ... (Lógica de guardado sin cambios)
        try {
            BigDecimal monto = new BigDecimal(txtMonto.getText().trim());
            int cuotas = Integer.parseInt(txtCuotas.getText().trim());
            BigDecimal tasa = new BigDecimal(txtTasa.getText().trim());

            if (monto.compareTo(BigDecimal.ZERO) <= 0 || cuotas <= 0) {
                JOptionPane.showMessageDialog(this, "El monto y las cuotas deben ser mayores que cero.", "Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date fechaDesembolso;
            try {
                fechaDesembolso = sdf.parse(txtFechaDesembolso.getText().trim());
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(this, "Formato de fecha inválido. Use yyyy-MM-dd.", "Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int nuevoContrato = creditoDAO.crearCreditoParaCliente(
                    cliente.getCodigoCliente(),
                    monto,
                    cuotas,
                    tasa,
                    fechaDesembolso
            );
            
            if (nuevoContrato > 0) {
                JOptionPane.showMessageDialog(this, "Crédito creado correctamente. Contrato N° " + nuevoContrato, "Éxito", JOptionPane.INFORMATION_MESSAGE);
                guardado = true;
                codigoContratoCreado = nuevoContrato;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo crear el crédito.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Monto, cuotas o tasa inválidos.", "Validación", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isGuardado() {
        return guardado;
    }

    public int getCodigoContratoCreado() {
        return codigoContratoCreado;
    }
}