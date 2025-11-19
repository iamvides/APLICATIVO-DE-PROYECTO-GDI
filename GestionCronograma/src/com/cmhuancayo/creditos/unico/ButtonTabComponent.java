package com.cmhuancayo.creditos.unico;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Componente UI para una pestaña con un botón de cierre 'X'.
 * Estilizado para coincidir con EstilosUI.
 */
public class ButtonTabComponent extends JPanel {
    private final JTabbedPane pane;

    public ButtonTabComponent(final JTabbedPane pane) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        if (pane == null) {
            throw new NullPointerException("TabbedPane is null");
        }
        this.pane = pane;
        setOpaque(false);
        
        // Etiqueta del título
        JLabel label = new JLabel() {
            public String getText() {
                int i = pane.indexOfTabComponent(ButtonTabComponent.this);
                if (i != -1) {
                    return pane.getTitleAt(i);
                }
                return null;
            }
        };
        
        label.setFont(EstilosUI.FONT_TEXTO); // Fuente estándar
        label.setForeground(EstilosUI.GRIS_TEXTO);
        add(label);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8)); // Espacio
        
        // Botón de cierre
        JButton button = new TabButton();
        add(button);
        
        // Padding general del componente
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    private class TabButton extends JButton implements ActionListener {
        public TabButton() {
            int size = 20; // Botón más grande
            setPreferredSize(new Dimension(size, size));
            setToolTipText("Cerrar esta pestaña");
            
            // Estilos
            setUI(new BasicButtonUI()); // UI limpia
            setContentAreaFilled(false);
            setFocusable(false);
            setBorder(null); // Sin borde
            setBorderPainted(false);
            setRolloverEnabled(true);
            
            // Efecto Hover
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setContentAreaFilled(true);
                    setBackground(EstilosUI.ROJO_SUAVE_BORDE);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    setContentAreaFilled(false);
                }
            });
            
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            int i = pane.indexOfTabComponent(ButtonTabComponent.this);
            if (i != -1) {
                pane.remove(i);
            }
        }

        // Dibuja la 'X'
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            
            // Antialiasing para suavizar líneas
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setStroke(new BasicStroke(2)); // Línea más gruesa
            g2.setColor(EstilosUI.GRIS_TEXTO); // Color estándar
            
            // Si el mouse está encima, cambia el color de la 'X'
            if (getModel().isRollover()) {
                g2.setColor(EstilosUI.ROJO_PRINCIPAL); // 'X' Roja
            }
            
            int delta = 6; // Margen interior
            g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
            g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
            g2.dispose();
        }
    }
}