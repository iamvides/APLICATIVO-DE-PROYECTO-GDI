package com.cmhuancayo.creditos.unico;

import javax.swing.*;
import java.awt.*;

public class LoginAuxCreateDB extends JDialog {

    public interface CreateDBCallback {
        void onCreate(String port, String user, String password);
    }

    public static void showDialog(CreateDBCallback callback) {
        LoginAuxCreateDB dialog = new LoginAuxCreateDB(callback);
        dialog.setLocationRelativeTo(null);
        dialog.setModal(true);
        dialog.setVisible(true);
    }

    private final JTextField txtPort = new JTextField("5432", 10);
    private final JTextField txtUser = new JTextField("postgres", 15);
    private final JPasswordField txtPass = new JPasswordField(15);
    private final JLabel lblError = new JLabel(" ");

    private LoginAuxCreateDB(CreateDBCallback callback) {
        setTitle("Configuración de PostgreSQL (solo primera vez)");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        int row = 0;

        // Puerto
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Puerto:"), gbc);
        gbc.gridx = 1;
        panel.add(txtPort, gbc);

        // Usuario admin
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Usuario admin (ej. postgres):"), gbc);
        gbc.gridx = 1;
        panel.add(txtUser, gbc);

        // Password admin
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Contraseña admin:"), gbc);
        gbc.gridx = 1;
        panel.add(txtPass, gbc);

        // Mensaje de error
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        lblError.setForeground(Color.RED);
        panel.add(lblError, gbc);

        // Botones
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAceptar = new JButton("Crear base");
        JButton btnCancelar = new JButton("Cancelar");
        botones.add(btnAceptar);
        botones.add(btnCancelar);

        btnAceptar.addActionListener(e -> {
            String port = txtPort.getText().trim();
            String user = txtUser.getText().trim();
            String pass = new String(txtPass.getPassword());

            if (port.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                lblError.setText("Todos los campos son obligatorios.");
                return;
            }

            // Probamos conexión al servidor (BD postgres)
            try {
                Class.forName("org.postgresql.Driver");
                String urlTest = "jdbc:postgresql://127.0.0.1:" + port + "/postgres";
                java.sql.Connection c = java.sql.DriverManager.getConnection(urlTest, user, pass);
                c.close();
            } catch (Exception ex) {
                lblError.setText("No se pudo conectar con esos datos. Verifícalos.");
                return;
            }

            lblError.setText(" ");
            if (callback != null) {
                callback.onCreate(port, user, pass);
            }
            dispose();
        });

        btnCancelar.addActionListener(e -> dispose());

        getContentPane().setLayout(new BorderLayout(10, 10));
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(botones, BorderLayout.SOUTH);
        pack();
    }
}
