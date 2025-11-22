package com.cmhuancayo.creditos.unico;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Utilitario para exportar a PDF el cronograma de un crédito
 * usando Apache PDFBox.
 *
 * Se usa desde PanelGestionCliente.descargarCronograma(...)
 */
public class CronogramaPdfExporter {

    // Formatos
    private static final DecimalFormat MONEDA = new DecimalFormat("###,##0.00");
    private static final SimpleDateFormat F_FECHA = new SimpleDateFormat("yyyy-MM-dd");

    // Tamaños y márgenes
    private static final float MARGIN = 50f;
    private static final float TITLE_FONT_SIZE = 16f;
    private static final float TEXT_FONT_SIZE = 10f;
    private static final float ROW_HEIGHT = 18f;

    /**
     * Genera el PDF del cronograma.
     *
     * @param credito  contrato seleccionado
     * @param cliente  cliente dueño del crédito
     * @param cuotas   lista de cuotas (en orden)
     * @param destino  archivo PDF a generar
     */
    public static void exportar(Credito credito,
                                Cliente cliente,
                                List<Cuota> cuotas,
                                File destino) throws IOException {

        if (credito == null) {
            throw new IllegalArgumentException("El crédito no puede ser null");
        }
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser null");
        }
        if (cuotas == null || cuotas.isEmpty()) {
            throw new IllegalArgumentException("La lista de cuotas está vacía");
        }

        try (PDDocument doc = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(doc, page);

            float y = page.getMediaBox().getUpperRightY() - MARGIN;

            // ----------------------------------------------------------
            // Título
            // ----------------------------------------------------------
            drawText(cs, PDType1Font.HELVETICA_BOLD, TITLE_FONT_SIZE,
                    MARGIN, y, "CRONOGRAMA DE PAGOS");
            y -= 30f;

            // ----------------------------------------------------------
            // Resumen del crédito
            // ----------------------------------------------------------
            String nombreCliente = construirNombreCliente(cliente);
            String docCliente = safe(cliente.getNumeroDocumento());
            String contrato = String.valueOf(credito.getCodigoContrato());
            String desembolso = "S/ " + formatBig(credito.getMontoDesembolso());
            String tasa = formatBig(credito.getTasaInteresCompensatorio()) + " %";
            String cuotasStr = String.valueOf(credito.getNumeroCuotas());
            String vigencia = formatFecha(credito.getFechaDesembolso());

            drawText(cs, PDType1Font.HELVETICA, TEXT_FONT_SIZE,
                    MARGIN, y, "CLIENTE: " + nombreCliente);
            y -= ROW_HEIGHT;
            drawText(cs, PDType1Font.HELVETICA, TEXT_FONT_SIZE,
                    MARGIN, y, "DOC.: " + docCliente);
            y -= ROW_HEIGHT;
            drawText(cs, PDType1Font.HELVETICA, TEXT_FONT_SIZE,
                    MARGIN, y, "CONTRATO: " + contrato);
            y -= ROW_HEIGHT;
            drawText(cs, PDType1Font.HELVETICA, TEXT_FONT_SIZE,
                    MARGIN, y, "VIGENCIA: " + vigencia);
            y -= (ROW_HEIGHT + 5);

            drawText(cs, PDType1Font.HELVETICA, TEXT_FONT_SIZE,
                    MARGIN, y, "DESEMBOLSO: " + desembolso);
            y -= ROW_HEIGHT;
            drawText(cs, PDType1Font.HELVETICA, TEXT_FONT_SIZE,
                    MARGIN, y, "CUOTAS: " + cuotasStr);
            y -= ROW_HEIGHT;
            drawText(cs, PDType1Font.HELVETICA, TEXT_FONT_SIZE,
                    MARGIN, y, "TASA ANUAL: " + tasa);
            y -= (ROW_HEIGHT + 10);

            // ----------------------------------------------------------
            // Cabecera de la tabla (SIN columna ESTADO)
            // ----------------------------------------------------------
            float[] colWidths = {
                    35f,   // N°
                    70f,   // Fecha venc.
                    70f,   // Capital
                    70f,   // Interés
                    70f,   // Seg. degrav.
                    85f,   // Monto cuota
                    90f    // Saldo capital
            };

            String[] headers = {
                    "N°",
                    "Fecha venc.",
                    "Capital",
                    "Interés",
                    "Seg. degrav.",
                    "Monto cuota",
                    "Saldo capital"
            };

            float x = MARGIN;
            for (int i = 0; i < headers.length; i++) {
                drawText(cs, PDType1Font.HELVETICA_BOLD, TEXT_FONT_SIZE,
                        x, y, headers[i]);
                x += colWidths[i];
            }
            y -= (ROW_HEIGHT + 4);

            // ----------------------------------------------------------
            // Filas de cuotas
            // ----------------------------------------------------------
            for (Cuota cuota : cuotas) {

                // ¿Hay espacio en la página?
                if (y < MARGIN + 40) {
                    cs.close();

                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    y = page.getMediaBox().getUpperRightY() - MARGIN;

                    // Reimprimir cabecera en la nueva página
                    x = MARGIN;
                    for (int i = 0; i < headers.length; i++) {
                        drawText(cs, PDType1Font.HELVETICA_BOLD, TEXT_FONT_SIZE,
                                x, y, headers[i]);
                        x += colWidths[i];
                    }
                    y -= (ROW_HEIGHT + 4);
                }

                x = MARGIN;

                // IMPORTANTE: ajusta getters si tu clase Cuota se llama distinto
                String nro = String.valueOf(cuota.getNroCuota());
                String fechaV = formatFecha(cuota.getFechaVencimiento());
                String capital = formatBig(cuota.getCapital());
                String interes = formatBig(cuota.getInteres());
                String segDegrav = formatBig(cuota.getSeguroDegravamen());
                String montoCuota = formatBig(cuota.getMontoCuota());
                String saldo = formatBig(cuota.getSaldoCapital());

                String[] valores = {
                        nro, fechaV, capital, interes, segDegrav, montoCuota, saldo
                };

                for (int i = 0; i < valores.length; i++) {
                    drawText(cs, PDType1Font.HELVETICA, TEXT_FONT_SIZE,
                            x, y, valores[i]);
                    x += colWidths[i];
                }
                y -= ROW_HEIGHT;
            }

            cs.close();
            doc.save(destino);
        }
    }

    /* =================== Helpers =================== */

    private static void drawText(PDPageContentStream cs,
                                 PDType1Font font,
                                 float size,
                                 float x,
                                 float y,
                                 String text) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text != null ? text : "");
        cs.endText();
    }

    private static String formatBig(BigDecimal valor) {
        if (valor == null) return "0.00";
        return MONEDA.format(valor);
    }

    private static String formatFecha(Date fecha) {
        if (fecha == null) return "-";
        return F_FECHA.format(fecha);
    }

    private static String construirNombreCliente(Cliente c) {
        StringBuilder sb = new StringBuilder();
        if (c.getApellidoPaterno() != null && !c.getApellidoPaterno().isEmpty()) {
            sb.append(c.getApellidoPaterno()).append(' ');
        }
        if (c.getApellidoMaterno() != null && !c.getApellidoMaterno().isEmpty()) {
            sb.append(c.getApellidoMaterno()).append(' ');
        }
        if (c.getNombres() != null && !c.getNombres().isEmpty()) {
            sb.append(c.getNombres());
        }
        String s = sb.toString().trim();
        return s.isEmpty() ? "-" : s;
    }

    private static String safe(String s) {
        return (s == null || s.isEmpty()) ? "-" : s;
    }
}
