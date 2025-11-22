package com.cmhuancayo.creditos.unico;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Servicio de cálculo de cronogramas de crédito.
 *
 * Responsabilidades principales:
 * <ul>
 *     <li>Generar el cronograma inicial de un crédito (cuota fija, sistema francés).</li>
 *     <li>Recalcular un cronograma por una amortización extraordinaria.</li>
 *     <li>Recalcular un cronograma por reprogramación (nuevo plazo y/o nueva tasa).</li>
 * </ul>
 *
 * Importante: esta clase NO accede a la base de datos, solo trabaja con objetos
 * {@link Credito} y {@link Cuota} en memoria.
 */
public class CalculadoraCreditoService {

    /** Escala usada para tasas de interés intermedias. */
    private static final int SCALE_INTERES = 10;
    /** Escala usada para montos en moneda (dos decimales). */
    private static final int SCALE_MONEDA = 2;
    /** Modo de redondeo estándar para montos y tasas. */
    private static final RoundingMode ROUND_MODE = RoundingMode.HALF_UP;

    // =========================================================
    // 1. GENERACIÓN DE CRONOGRAMA ESTÁNDAR (CRÉDITO NUEVO)
    // =========================================================

    /**
     * Genera el cronograma completo de un crédito nuevo usando el sistema francés
     * (cuota fija). A partir de:
     * <ul>
     *     <li>Monto desembolsado</li>
     *     <li>Número de cuotas</li>
     *     <li>Tasa de interés compensatorio anual</li>
     * </ul>
     * calcula la cuota fija mensual y descompone cada cuota en capital e interés.
     *
     * @param credito Datos básicos del crédito (monto, tasa, número de cuotas, fecha de desembolso).
     * @return Lista de cuotas generadas. Si faltan datos mínimos, retorna una lista vacía.
     */
    public List<Cuota> generarCronograma(Credito credito) {
        List<Cuota> cuotas = new ArrayList<>();

        // Validación mínima de datos
        if (credito.getMontoDesembolso() == null ||
                credito.getNumeroCuotas() <= 0 ||
                credito.getTasaInteresCompensatorio() == null) {
            return cuotas;
        }

        BigDecimal principal = credito.getMontoDesembolso();
        int n = credito.getNumeroCuotas();
        BigDecimal tasaAnual = credito.getTasaInteresCompensatorio();

        // TEA -> tasa mensual (r)
        BigDecimal tasaMensual = tasaAnual
                .divide(BigDecimal.valueOf(12 * 100.0), SCALE_INTERES, ROUND_MODE);

        // Cálculo de la cuota fija usando fórmula del sistema francés
        BigDecimal cuotaFija;
        double P = principal.doubleValue();
        double r = tasaMensual.doubleValue();
        double nDouble = n;

        if (r == 0.0) {
            // Sin interés: dividir capital entre número de cuotas
            cuotaFija = principal
                    .divide(BigDecimal.valueOf(n), SCALE_MONEDA, ROUND_MODE);
        } else {
            double cuotaDouble = P * r / (1 - Math.pow(1 + r, -nDouble));
            cuotaFija = BigDecimal.valueOf(cuotaDouble)
                    .setScale(SCALE_MONEDA, ROUND_MODE);
        }

        BigDecimal saldo = principal;
        Date fechaBase = (credito.getFechaDesembolso() != null)
                ? credito.getFechaDesembolso()
                : new Date();

        // Generación cuota por cuota
        for (int i = 1; i <= n; i++) {
            Cuota c = new Cuota();
            c.setCodigoContrato(credito.getCodigoContrato());
            c.setNroCuota(i);
            c.setEstadoCuota("Pendiente");

            // Fecha de vencimiento = fecha desembolso + i meses
            Date fechaVenc = sumarMeses(fechaBase, i);
            c.setFechaVencimiento(fechaVenc);

            BigDecimal interes = saldo.multiply(tasaMensual)
                    .setScale(SCALE_MONEDA, ROUND_MODE);
            BigDecimal capital = cuotaFija.subtract(interes)
                    .setScale(SCALE_MONEDA, ROUND_MODE);

            // En la última cuota se ajusta el capital para que el saldo quede en 0
            if (i == n) {
                capital = saldo;
                cuotaFija = capital.add(interes).setScale(SCALE_MONEDA, ROUND_MODE);
            }

            saldo = saldo.subtract(capital).setScale(SCALE_MONEDA, ROUND_MODE);

            c.setCapital(capital);
            c.setInteres(interes);
            c.setSeguroDegravamen(BigDecimal.ZERO);
            c.setSegurosComisiones(BigDecimal.ZERO);
            c.setItf(BigDecimal.ZERO);
            c.setDias(30); // aproximación a 30 días por periodo
            c.setMontoCuota(cuotaFija);
            c.setSaldoCapital(saldo.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : saldo);

            cuotas.add(c);
        }

        return cuotas;
    }

    // =========================================================
    // 2. AMORTIZACIÓN EXTRAORDINARIA
    // =========================================================

    /**
     * Recalcula el cronograma a partir de una amortización extraordinaria al capital.
     * Toma las cuotas pendientes, calcula el saldo vivo de capital y aplica el monto
     * de amortización. Con el nuevo saldo puede:
     * <ul>
     *     <li>Reducir el monto de la cuota manteniendo el plazo (REDUCIR_CUOTA).</li>
     *     <li>Reducir el plazo manteniendo una cuota similar (cualquier otro valor en tipoReduccion).</li>
     * </ul>
     *
     * @param credito          Crédito original (para obtener la tasa anual).
     * @param cronogramaActual Cronograma vigente (con cuotas pagadas y pendientes).
     * @param montoAmortizacion Monto de la amortización extraordinaria.
     * @param tipoReduccion    "REDUCIR_CUOTA" o cualquier otro valor para reducir plazo.
     * @param indiceCuotaBase  (No se usa actualmente, reservado para extensiones futuras).
     * @return Nuevo cronograma solo de cuotas pendientes, ya recalculadas.
     */
    public List<Cuota> recalcularPorAmortizacion(
            Credito credito,
            List<Cuota> cronogramaActual,
            BigDecimal montoAmortizacion,
            String tipoReduccion,
            int indiceCuotaBase) {

        List<Cuota> nuevo = new ArrayList<>();
        if (cronogramaActual == null || cronogramaActual.isEmpty()) {
            return nuevo;
        }

        // 1) Se filtran solo las cuotas pendientes (no PAGADAS)
        List<Cuota> pendientes = new ArrayList<>();
        for (Cuota c : cronogramaActual) {
            if (c.getEstadoCuota() == null ||
                    !c.getEstadoCuota().equalsIgnoreCase("PAGADA")) {
                pendientes.add(c);
            }
        }

        if (pendientes.isEmpty()) {
            return nuevo;
        }

        // 2) Saldo vivo = suma del capital de las cuotas pendientes
        BigDecimal saldoVivo = BigDecimal.ZERO;
        for (Cuota c : pendientes) {
            if (c.getCapital() != null) {
                saldoVivo = saldoVivo.add(c.getCapital());
            }
        }

        // Nuevo principal después de aplicar la amortización
        BigDecimal nuevoPrincipal = saldoVivo.subtract(montoAmortizacion);
        if (nuevoPrincipal.compareTo(BigDecimal.ZERO) <= 0) {
            return nuevo;
        }

        // Tasa anual desde el crédito
        BigDecimal tasaAnual = credito.getTasaInteresCompensatorio();
        if (tasaAnual == null) {
            tasaAnual = BigDecimal.ZERO;
        }
        BigDecimal tasaMensual = tasaAnual
                .divide(BigDecimal.valueOf(12 * 100.0), SCALE_INTERES, ROUND_MODE);

        // 3) Estrategia de recálculo según tipoReduccion
        if ("REDUCIR_CUOTA".equalsIgnoreCase(tipoReduccion)) {
            // Mantener plazo, recalcular una cuota menor
            nuevo = recalcularReduciendoCuota(nuevoPrincipal, tasaMensual, pendientes);
        } else {
            // Mantener cuota similar a la actual, reducir número de cuotas
            BigDecimal cuotaReferencia = pendientes.get(0).getMontoCuota();
            if (cuotaReferencia == null || cuotaReferencia.compareTo(BigDecimal.ZERO) <= 0) {
                nuevo = recalcularReduciendoCuota(nuevoPrincipal, tasaMensual, pendientes);
            } else {
                nuevo = recalcularReduciendoPlazo(nuevoPrincipal, tasaMensual, pendientes, cuotaReferencia);
            }
        }

        // Numerar cuotas y asignar contrato
        int nro = 1;
        for (Cuota c : nuevo) {
            c.setCodigoContrato(credito.getCodigoContrato());
            c.setNroCuota(nro++);
            c.setEstadoCuota("Pendiente");
        }

        return nuevo;
    }

    // =========================================================
    // 3. REPROGRAMACIÓN (NUEVO PLAZO / NUEVA TASA)
    // =========================================================

    /**
     * Recalcula completamente un cronograma cuando se reprograma el crédito.
     * Se toma el saldo vivo de capital de las cuotas pendientes y se genera un
     * nuevo cronograma con:
     * <ul>
     *     <li>Nuevo número de cuotas.</li>
     *     <li>Nueva tasa anual (si se indica; si no, se usa la actual del crédito).</li>
     * </ul>
     *
     * @param credito           Crédito original (se usa su tasa si no se pasa una nueva).
     * @param cronogramaActual  Cronograma vigente (con cuotas pagadas y pendientes).
     * @param nuevoNumeroCuotas Nuevo número total de cuotas para el saldo pendiente.
     * @param nuevaTasaAnual    Nueva tasa anual compensatoria; puede ser null para mantener la actual.
     * @return Lista de cuotas recalculadas para el saldo pendiente.
     */
    public List<Cuota> recalcularPorReprogramacion(
            Credito credito,
            List<Cuota> cronogramaActual,
            int nuevoNumeroCuotas,
            BigDecimal nuevaTasaAnual) {

        List<Cuota> nuevo = new ArrayList<>();
        if (cronogramaActual == null || cronogramaActual.isEmpty()) {
            return nuevo;
        }
        if (nuevoNumeroCuotas <= 0) {
            return nuevo;
        }

        // 1) Cuotas pendientes (no PAGADAS)
        List<Cuota> pendientes = new ArrayList<>();
        for (Cuota c : cronogramaActual) {
            if (c.getEstadoCuota() == null ||
                    !c.getEstadoCuota().equalsIgnoreCase("PAGADA")) {
                pendientes.add(c);
            }
        }

        if (pendientes.isEmpty()) {
            return nuevo;
        }

        // 2) Saldo vivo = suma del capital de las cuotas pendientes
        BigDecimal saldoVivo = BigDecimal.ZERO;
        for (Cuota c : pendientes) {
            if (c.getCapital() != null) {
                saldoVivo = saldoVivo.add(c.getCapital());
            }
        }

        if (saldoVivo.compareTo(BigDecimal.ZERO) <= 0) {
            return nuevo;
        }

        // 3) Tasa a usar: nueva (si se captura) o la tasa actual del crédito
        BigDecimal tasaAnual = (nuevaTasaAnual != null ? nuevaTasaAnual : credito.getTasaInteresCompensatorio());
        if (tasaAnual == null) {
            tasaAnual = BigDecimal.ZERO;
        }
        BigDecimal tasaMensual = tasaAnual
                .divide(BigDecimal.valueOf(12 * 100.0), SCALE_INTERES, ROUND_MODE);

        int n = nuevoNumeroCuotas;
        BigDecimal cuotaFija;

        double P = saldoVivo.doubleValue();
        double r = tasaMensual.doubleValue();
        double nDouble = n;

        if (r == 0.0) {
            cuotaFija = saldoVivo
                    .divide(BigDecimal.valueOf(n), SCALE_MONEDA, ROUND_MODE);
        } else {
            double cuotaDouble = P * r / (1 - Math.pow(1 + r, -nDouble));
            cuotaFija = BigDecimal.valueOf(cuotaDouble)
                    .setScale(SCALE_MONEDA, ROUND_MODE);
        }

        // Base de fechas: la fecha de vencimiento de la primera cuota pendiente,
        // y desde allí se van sumando meses.
        Date fechaBase = pendientes.get(0).getFechaVencimiento();
        if (fechaBase == null) {
            fechaBase = new Date();
        }

        BigDecimal saldo = saldoVivo;

        for (int i = 1; i <= n; i++) {
            Cuota c = new Cuota();
            c.setEstadoCuota("Pendiente");

            // Para la primera cuota usamos la fecha de la primera pendiente;
            // desde la segunda se suman meses.
            Date fechaVenc = sumarMeses(fechaBase, i - 1);
            c.setFechaVencimiento(fechaVenc);

            BigDecimal interes = saldo.multiply(tasaMensual)
                    .setScale(SCALE_MONEDA, ROUND_MODE);
            BigDecimal capital = cuotaFija.subtract(interes)
                    .setScale(SCALE_MONEDA, ROUND_MODE);

            // Ajuste en la última cuota para dejar saldo en 0
            if (i == n) {
                capital = saldo;
                cuotaFija = capital.add(interes).setScale(SCALE_MONEDA, ROUND_MODE);
            }

            saldo = saldo.subtract(capital).setScale(SCALE_MONEDA, ROUND_MODE);

            c.setCapital(capital);
            c.setInteres(interes);
            c.setSeguroDegravamen(BigDecimal.ZERO);
            c.setSegurosComisiones(BigDecimal.ZERO);
            c.setItf(BigDecimal.ZERO);
            c.setDias(30);
            c.setMontoCuota(cuotaFija);
            c.setSaldoCapital(saldo.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : saldo);

            nuevo.add(c);
        }

        // Numerar cuotas y asignar contrato
        int nro = 1;
        for (Cuota c : nuevo) {
            c.setCodigoContrato(credito.getCodigoContrato());
            c.setNroCuota(nro++);
            c.setEstadoCuota("Pendiente");
        }

        return nuevo;
    }

    // =========================================================
    // 4. MÉTODOS AUXILIARES INTERNOS
    // =========================================================

    /**
     * Recalcula manteniendo el número de cuotas, pero generando una nueva cuota fija
     * a partir del nuevo principal.
     */
    private List<Cuota> recalcularReduciendoCuota(BigDecimal principal,
                                                  BigDecimal tasaMensual,
                                                  List<Cuota> pendientes) {
        List<Cuota> nuevo = new ArrayList<>();

        int n = pendientes.size();
        BigDecimal cuotaFija;

        double P = principal.doubleValue();
        double r = tasaMensual.doubleValue();
        double nDouble = n;

        if (r == 0.0) {
            cuotaFija = principal
                    .divide(BigDecimal.valueOf(n), SCALE_MONEDA, ROUND_MODE);
        } else {
            double cuotaDouble = P * r / (1 - Math.pow(1 + r, -nDouble));
            cuotaFija = BigDecimal.valueOf(cuotaDouble)
                    .setScale(SCALE_MONEDA, ROUND_MODE);
        }

        BigDecimal saldo = principal;

        for (int i = 0; i < n; i++) {
            Cuota original = pendientes.get(i);
            Cuota c = new Cuota();

            // Se respeta la fecha de vencimiento original de cada cuota
            c.setFechaVencimiento(original.getFechaVencimiento());

            BigDecimal interes = saldo.multiply(tasaMensual)
                    .setScale(SCALE_MONEDA, ROUND_MODE);
            BigDecimal capital = cuotaFija.subtract(interes)
                    .setScale(SCALE_MONEDA, ROUND_MODE);

            // Última cuota ajustada para que el saldo quede en 0
            if (i == n - 1) {
                capital = saldo;
                cuotaFija = capital.add(interes).setScale(SCALE_MONEDA, ROUND_MODE);
            }

            saldo = saldo.subtract(capital).setScale(SCALE_MONEDA, ROUND_MODE);

            c.setCapital(capital);
            c.setInteres(interes);
            c.setSeguroDegravamen(BigDecimal.ZERO);
            c.setSegurosComisiones(BigDecimal.ZERO);
            c.setItf(BigDecimal.ZERO);
            c.setDias(30);
            c.setMontoCuota(cuotaFija);
            c.setSaldoCapital(saldo.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : saldo);

            nuevo.add(c);
        }

        return nuevo;
    }

    /**
     * Recalcula manteniendo una cuota de referencia (aprox. la misma cuota que
     * tenía el crédito) y reduciendo el número de cuotas necesarias hasta que
     * el saldo se cancele.
     */
    private List<Cuota> recalcularReduciendoPlazo(BigDecimal principal,
                                                  BigDecimal tasaMensual,
                                                  List<Cuota> pendientes,
                                                  BigDecimal cuotaReferencia) {
        List<Cuota> nuevo = new ArrayList<>();

        BigDecimal saldo = principal;
        int nMax = pendientes.size();

        for (int i = 0; i < nMax && saldo.compareTo(BigDecimal.ZERO) > 0; i++) {
            Cuota original = pendientes.get(i);
            Cuota c = new Cuota();

            // Mantener fechas originales mientras se pueda
            c.setFechaVencimiento(original.getFechaVencimiento());

            BigDecimal interes = saldo.multiply(tasaMensual)
                    .setScale(SCALE_MONEDA, ROUND_MODE);
            BigDecimal capital = cuotaReferencia.subtract(interes)
                    .setScale(SCALE_MONEDA, ROUND_MODE);

            // Si la cuota de referencia ya no cubre ni el interés, se cancela todo en una última cuota
            if (capital.compareTo(BigDecimal.ZERO) <= 0) {
                capital = saldo;
                BigDecimal cuotaFinal = capital.add(interes).setScale(SCALE_MONEDA, ROUND_MODE);
                saldo = BigDecimal.ZERO;

                c.setCapital(capital);
                c.setInteres(interes);
                c.setSeguroDegravamen(BigDecimal.ZERO);
                c.setSegurosComisiones(BigDecimal.ZERO);
                c.setItf(BigDecimal.ZERO);
                c.setDias(30);
                c.setMontoCuota(cuotaFinal);
                c.setSaldoCapital(BigDecimal.ZERO);
                nuevo.add(c);
                break;
            }

            // Si estamos en la última posición permitida, forzamos la cancelación del saldo
            if (i == nMax - 1) {
                capital = saldo;
                BigDecimal cuotaFinal = capital.add(interes).setScale(SCALE_MONEDA, ROUND_MODE);
                saldo = BigDecimal.ZERO;

                c.setCapital(capital);
                c.setInteres(interes);
                c.setSeguroDegravamen(BigDecimal.ZERO);
                c.setSegurosComisiones(BigDecimal.ZERO);
                c.setItf(BigDecimal.ZERO);
                c.setDias(30);
                c.setMontoCuota(cuotaFinal);
                c.setSaldoCapital(BigDecimal.ZERO);
                nuevo.add(c);
                break;

            } else {
                // Todavía quedan cuotas futuras: se mantiene la cuota de referencia
                saldo = saldo.subtract(capital).setScale(SCALE_MONEDA, ROUND_MODE);

                c.setCapital(capital);
                c.setInteres(interes);
                c.setSeguroDegravamen(BigDecimal.ZERO);
                c.setSegurosComisiones(BigDecimal.ZERO);
                c.setItf(BigDecimal.ZERO);
                c.setDias(30);
                c.setMontoCuota(cuotaReferencia.setScale(SCALE_MONEDA, ROUND_MODE));
                c.setSaldoCapital(saldo.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : saldo);
                nuevo.add(c);
            }
        }

        return nuevo;
    }

    /**
     * Suma una cantidad de meses a una fecha base.
     *
     * @param base   Fecha de inicio.
     * @param meses  Número de meses a sumar (puede ser 0, 1, 2, ...).
     * @return Nueva fecha con los meses sumados.
     */
    private Date sumarMeses(Date base, int meses) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(base);
        cal.add(Calendar.MONTH, meses);
        return cal.getTime();
    }
}
