package com.cmhuancayo.creditos.unico;

import javax.swing.*;
import java.awt.*;

public class DashboardPrincipal extends JFrame {

    private JTabbedPane tabbedPane;
    private PanelListadoClientes panelClientes;

    public DashboardPrincipal() {
        setTitle("Sistema de Gestión de Créditos - CM Huancayo");
        setSize(1366, 768); // Tamaño más grande por defecto
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Icono de la aplicación (opcional, pero recomendado)
        // try {
        //     Image icon = new ImageIcon(getClass().getResource("/icon.png")).getImage();
        //     setIconImage(icon);
        // } catch (Exception e) {
        //     System.out.println("Icono no encontrado");
        // }

        // Aplicamos Look and Feel nativo para una mejor apariencia
        try {
           UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) { e.printStackTrace(); }

        // Fondo del frame
        getContentPane().setBackground(EstilosUI.GRIS_FONDO); 

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(EstilosUI.FONT_SUBTITULO);
        tabbedPane.setBackground(EstilosUI.BLANCO);
        
        // Estilo de pestañas
        tabbedPane.setForeground(EstilosUI.GRIS_TEXTO);
        tabbedPane.setOpaque(true);
        // Borde superior rojo
        tabbedPane.setBorder(
                BorderFactory.createMatteBorder(3, 0, 0, 0, EstilosUI.ROJO_PRINCIPAL)
        );
        
        panelClientes = new PanelListadoClientes(this);
        tabbedPane.addTab("  Listado de Clientes  ", panelClientes);

        // Pestaña fija (sin botón de cierre) con título estilizado
        JLabel lblTab = new JLabel("📋 Listado de Clientes");
        lblTab.setFont(EstilosUI.FONT_SUBTITULO);
        lblTab.setForeground(EstilosUI.ROJO_PRINCIPAL); // Título de pestaña en rojo
        lblTab.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); // Más padding
        tabbedPane.setTabComponentAt(0, lblTab);

        add(tabbedPane);
    }

    /**
     * Abre una pestaña de gestión y le añade un botón de cierre.
     */
    public void abrirPestanaGestion(Cliente cliente) {
        String titulo = "Gestión: " + cliente.getNombres() + " " + cliente.getApellidoPaterno();
        
        // 1. Evitar duplicados
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            // Comprobación robusta del título
            String tituloActual = "";
            Component tabComp = tabbedPane.getTabComponentAt(i);
            if (tabComp instanceof ButtonTabComponent) {
                // Asumiendo que el primer componente del ButtonTabComponent es el JLabel
                JLabel lbl = (JLabel) ((ButtonTabComponent) tabComp).getComponent(0);
                tituloActual = lbl.getText();
            } else if (i > 0) { // Ignorar la primera pestaña fija
                 tituloActual = tabbedPane.getTitleAt(i);
            }

            if (titulo.equals(tituloActual)) {
                tabbedPane.setSelectedIndex(i);
                return;
            }
        }

        // 2. Crear el panel de gestión
        PanelGestionCliente panelGestion = new PanelGestionCliente(cliente);
        tabbedPane.addTab(titulo, panelGestion);
        int newIndex = tabbedPane.getTabCount() - 1;

        // 3. Añadir el componente de pestaña con botón de cierre
        tabbedPane.setTabComponentAt(newIndex, new ButtonTabComponent(tabbedPane));
        tabbedPane.setSelectedIndex(newIndex); // Seleccionar la nueva pestaña
    }

    public static void main(String[] args) {
        // Asegurar que Swing se ejecute en el hilo de despacho de eventos
        SwingUtilities.invokeLater(() -> {
            new DashboardPrincipal().setVisible(true);
        });
    }
}