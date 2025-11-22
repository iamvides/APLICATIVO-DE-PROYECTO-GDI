package com.cmhuancayo.creditos.unico;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal del sistema.
 *
 * Contiene:
 *  - Un JTabbedPane con la pestaña fija "Listado de Clientes".
 *  - Pestañas dinámicas de gestión por cliente (PanelGestionCliente),
 *    cada una con botón de cierre (ButtonTabComponent).
 *
 * Nota: Esta clase NO accede directamente a la base de datos.
 *       Toda la lógica de datos está en los DAO.
 */
public class DashboardPrincipal extends JFrame {

    /** Contenedor de pestañas principales del sistema. */
    private JTabbedPane tabbedPane;

    /** Panel principal con el listado/búsqueda de clientes. */
    private PanelListadoClientes panelClientes;

    /**
     * Constructor: inicializa la ventana principal, estilos y pestañas.
     */
    public DashboardPrincipal() {
        setTitle("Sistema de Gestión de Créditos - CM Huancayo");
        setSize(1366, 768); // Tamaño por defecto (puedes ajustarlo según tu pantalla)
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrar en pantalla

        // Icono de la aplicación (opcional; descomentarlo si tienes el recurso)
        /*
        try {
            Image icon = new ImageIcon(getClass().getResource("/icon.png")).getImage();
            setIconImage(icon);
        } catch (Exception e) {
            System.out.println("Icono no encontrado");
        }
        */

        // Look and Feel: puedes usar el del sistema o el cruzado.
        // Recomendado: aspecto nativo del sistema operativo.
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            // Si prefieres el look cruzado de Java, usa:
            // UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Fondo general del frame (gris claro definido en EstilosUI)
        getContentPane().setBackground(EstilosUI.GRIS_FONDO);

        // === Configuración del JTabbedPane principal ===
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(EstilosUI.FONT_SUBTITULO);
        tabbedPane.setBackground(EstilosUI.BLANCO);
        tabbedPane.setForeground(EstilosUI.GRIS_TEXTO);
        tabbedPane.setOpaque(true);

        // Borde superior rojo (línea que refuerza la identidad visual)
        tabbedPane.setBorder(
                BorderFactory.createMatteBorder(3, 0, 0, 0, EstilosUI.ROJO_PRINCIPAL)
        );

        // === Pestaña fija: Listado de Clientes ===
        panelClientes = new PanelListadoClientes(this);
        tabbedPane.addTab("  Listado de Clientes  ", panelClientes);

        // Reemplazamos el título de la pestaña por un JLabel estilizado (sin botón de cierre).
        JLabel lblTab = new JLabel("📋 Listado de Clientes");
        lblTab.setFont(EstilosUI.FONT_SUBTITULO);
        lblTab.setForeground(EstilosUI.ROJO_PRINCIPAL); // texto en rojo
        lblTab.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); // padding
        tabbedPane.setTabComponentAt(0, lblTab);

        // Agregamos el tabbed pane al frame
        add(tabbedPane);
    }

    /**
     * Abre una nueva pestaña de gestión de un cliente (PanelGestionCliente).
     * - Si el cliente ya tiene una pestaña abierta, simplemente se selecciona esa pestaña.
     * - Si no existe, se crea una nueva con un botón de cierre (ButtonTabComponent).
     *
     * @param cliente Cliente seleccionado en el listado.
     */
    public void abrirPestanaGestion(Cliente cliente) {
        String titulo = "Gestión: " + cliente.getNombres() + " " + cliente.getApellidoPaterno();

        // 1. Evitar pestañas duplicadas para el mismo cliente.
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            String tituloActual = "";

            // Si la pestaña tiene un ButtonTabComponent, extraemos su JLabel interno
            Component tabComp = tabbedPane.getTabComponentAt(i);
            if (tabComp instanceof ButtonTabComponent) {
                // Asumimos que el primer componente del ButtonTabComponent es el JLabel del título
                Component comp0 = ((ButtonTabComponent) tabComp).getComponent(0);
                if (comp0 instanceof JLabel) {
                    JLabel lbl = (JLabel) comp0;
                    tituloActual = lbl.getText();
                }
            } else if (i > 0) {
                // Para pestañas sin componente personalizado (o por seguridad),
                // usamos el título normal del tabbedPane. Ignoramos la pestaña 0
                // porque es la fija de "Listado de Clientes".
                tituloActual = tabbedPane.getTitleAt(i);
            }

            if (titulo.equals(tituloActual)) {
                // Ya hay una pestaña abierta para este cliente -> la seleccionamos
                tabbedPane.setSelectedIndex(i);
                return;
            }
        }

        // 2. Crear el panel de gestión para el cliente
        PanelGestionCliente panelGestion = new PanelGestionCliente(cliente);
        tabbedPane.addTab(titulo, panelGestion);
        int newIndex = tabbedPane.getTabCount() - 1;

        // 3. Asignar un componente de pestaña con botón de cierre
        tabbedPane.setTabComponentAt(newIndex, new ButtonTabComponent(tabbedPane));
        tabbedPane.setSelectedIndex(newIndex); // Seleccionar la nueva pestaña
    }

    /**
     * Punto de entrada de la aplicación.
     * Crea y muestra la ventana principal en el hilo de eventos de Swing.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DashboardPrincipal().setVisible(true);
        });
    }
}
