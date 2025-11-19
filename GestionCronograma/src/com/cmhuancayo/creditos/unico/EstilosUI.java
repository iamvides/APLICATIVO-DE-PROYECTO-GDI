package com.cmhuancayo.creditos.unico;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Clase centralizada para todos los estilos de la UI.
 * Define la paleta de colores (Rojo/Blanco) y fuentes.
 */
public class EstilosUI {

    // --- PALETA DE COLORES (Rojo y Blanco) ---
    public static final Color ROJO_PRINCIPAL = new Color(0xC62828); // Rojo oscuro (material design)
    public static final Color ROJO_OSCURO_HOVER = new Color(0xAB2222); // Para hover
    public static final Color ROJO_SUAVE_BORDE = new Color(0xE57373); // Para bordes suaves
    public static final Color BLANCO = Color.WHITE;
    public static final Color GRIS_FONDO = new Color(0xF4F4F4); // Fondo general
    public static final Color GRIS_TEXTO = new Color(0x333333); // Texto principal
    public static final Color GRIS_TABLA_ZEBRA = new Color(0xFAFAFA); // Trama de tabla

    // --- FUENTES ---
    public static final Font FONT_TITULO = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITULO = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_TEXTO = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BOTON = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TABLA_HEADER = new Font("Segoe UI", Font.BOLD, 14);

    // --- BORDES ---
    public static final Border BORDE_CAMPO_TEXTO = BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xC0C0C0)),
            new EmptyBorder(5, 8, 5, 8)
    );
    public static final Border BORDE_DIALOGO = BorderFactory.createEmptyBorder(15, 15, 15, 15);
    public static final Border BORDE_PANEL_INTERNO = BorderFactory.createEmptyBorder(10, 10, 10, 10);

    /**
     * Aplica el fondo claro estándar a un componente.
     */
    public static void aplicarFondoClaro(Container comp) {
        comp.setBackground(GRIS_FONDO);
    }

    /**
     * Estiliza un botón como acción principal (fondo rojo).
     */
    public static void estilizarBotonPrincipal(JButton b) {
        b.setFont(FONT_BOTON);
        b.setOpaque(true); // <-- *** ESTA ES LA LÍNEA QUE FALTABA ***
        b.setBackground(ROJO_PRINCIPAL);
        b.setForeground(BLANCO);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 25, 10, 25));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Efecto Hover
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                b.setBackground(ROJO_OSCURO_HOVER);
            }
            public void mouseExited(MouseEvent evt) {
                b.setBackground(ROJO_PRINCIPAL);
            }
        });
    }

    /**
     * Estiliza un botón como acción secundaria (borde rojo).
     */
    public static void estilizarBotonSecundario(JButton b) {
        b.setFont(FONT_BOTON);
        b.setOpaque(true); // <-- *** ESTA ES LA LÍNEA QUE FALTABA ***
        b.setBackground(BLANCO);
        b.setForeground(ROJO_PRINCIPAL);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ROJO_PRINCIPAL, 1),
                new EmptyBorder(8, 20, 8, 20)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Efecto Hover
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                b.setBackground(GRIS_FONDO);
            }
            public void mouseExited(MouseEvent evt) {
                b.setBackground(BLANCO);
            }
        });
    }

    /**
     * Estiliza un panel con un TitledBorder (borde con título).
     */
    public static void estilizarTitledPanel(JPanel panel, String titulo) {
        panel.setBackground(BLANCO);
        TitledBorder tb = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0xDDDDDD)),
                " " + titulo + " ",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                FONT_SUBTITULO,
                ROJO_PRINCIPAL
        );
        panel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 0, 10, 0), // Margen exterior
                tb
        ));
    }

    /**
     * Estiliza una JTable (cabecera roja, trama de cebra).
     */
    public static void estilizarTabla(JTable tabla) {
        tabla.setFont(FONT_TEXTO);
        tabla.setForeground(GRIS_TEXTO);
        tabla.setRowHeight(30);
        tabla.setGridColor(new Color(0xE0E0E0));
        tabla.setSelectionBackground(ROJO_SUAVE_BORDE);
        tabla.setSelectionForeground(BLANCO);

        // Cabecera de la tabla
        JTableHeader header = tabla.getTableHeader();
        header.setFont(FONT_TABLA_HEADER);
        header.setBackground(ROJO_PRINCIPAL);
        header.setForeground(BLANCO);
        header.setOpaque(false);
        header.setBorder(BorderFactory.createLineBorder(ROJO_PRINCIPAL));
        ((DefaultTableCellRenderer)header.getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.LEFT);
        header.setPreferredSize(new Dimension(100, 40));

        // Aplicar trama (Zebra)
        TableCellRenderer rendererZebra = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? BLANCO : GRIS_TABLA_ZEBRA);
                }
                // Añadir padding a las celdas
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        };
        tabla.setDefaultRenderer(Object.class, rendererZebra);
    }

    /**
     * Estiliza un JDialog estándar.
     */
    public static void estilizarDialogo(JDialog dialog) {
        dialog.getContentPane().setBackground(GRIS_FONDO);
        // Casting seguro
        if (dialog.getContentPane() instanceof JComponent) {
            ((JComponent)dialog.getContentPane()).setBorder(BORDE_DIALOGO);
        }
    }

    /**
     * Estiliza un JScrollPane.
     */
    public static void estilizarScrollPane(JScrollPane scroll) {
        scroll.getViewport().setBackground(BLANCO);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xDDDDDD)));
    }

    /**
     * Estiliza los campos de texto estándar.
     */
    public static void estilizarCampo(JTextField field) {
        field.setFont(FONT_TEXTO);
        field.setBorder(BORDE_CAMPO_TEXTO);
    }

    /**
     * Estiliza los ComboBox.
     */
    public static void estilizarComboBox(JComboBox<?> combo) {
        combo.setFont(FONT_TEXTO);
        combo.setBackground(BLANCO);
        combo.setBorder(BorderFactory.createLineBorder(new Color(0xC0C0C0)));
    }

    /**
     * Estiliza las etiquetas.
     */
    public static void estilizarLabel(JLabel label) {
        label.setFont(FONT_TEXTO);
        label.setForeground(GRIS_TEXTO);
    }
}