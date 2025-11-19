package com.cmhuancayo.creditos.unico;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Servicio de cálculo de cronogramas.
 * - Genera cronograma estándar (cuota fija, sistema francés)
 * - Recalcula por amortización extraordinaria
 * - Recalcula por reprogramación (nuevo plazo / nueva tasa)
 *
 * No escribe en BD, solo devuelve listas de Cuota.
 */
public class CalculadoraCreditoService {

    private static final int SCALE_INTERES = 10;
    private static final int SCALE_MONEDA = 2;
    private static final RoundingMode ROUND_MODE = RoundingMode.HALF_UP;

    /* =========================================================
     *  GENERACIÓN DE CRONOGRAMA ESTÁNDAR (CRÉDITO NUEVO)
     * ========================================================= */
    public List<Cuota> generarCronograma(Credito credito) {
        List<Cuota> cuotas = new ArrayList<>();

        if (credito.getMontoDesembolso() == null ||
                credito.getNumeroCuotas() <= 0 ||
                credito.getTasaInteresCompensatorio() == null) {
            return cuotas;
        }

        BigDecimal principal = credito.getMontoDesembolso();
        int n = credito.getNumeroCuotas();
        BigDecimal tasaAnual = credito.getTasaInteresCompensatorio();

        BigDecimal tasaMensual = tasaAnual
                .divide(BigDecimal.valueOf(12 * 100.0), SCALE_INTERES, ROUND_MODE);

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
        Date fechaBase = credito.getFechaDesembolso() != null
                ? credito.getFechaDesembolso()
                : new Date();

        for (int i = 1; i <= n; i++) {
            Cuota c = new Cuota();
            c.setCodigoContrato(credito.getCodigoContrato());
            c.setNroCuota(i);
            c.setEstadoCuota("Pendiente");

            Date fechaVenc = sumarMeses(fechaBase, i);
            c.setFechaVencimiento(fechaVenc);

            BigDecimal interes = saldo.multiply(tasaMensual)
                    .setScale(SCALE_MONEDA, ROUND_MODE);
            BigDecimal capital = cuotaFija.subtract(interes)
                    .setScale(SCALE_MONEDA, ROUND_MODE);

            if (i == n) {
                // Ajuste final: dejar saldo en 0
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

            cuotas.add(c);
        }

        return cuotas;
    }

    /* =========================================================
     *  AMORTIZACIÓN EXTRAORDINARIA
     * ========================================================= */

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

        // 1. Cuotas pendientes (no pagadas)
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

        // 2. Saldo vivo = suma de capital (de cuotas pendientes)
        BigDecimal saldoVivo = BigDecimal.ZERO;
        for (Cuota c : pendientes) {
            if (c.getCapital() != null) {
                saldoVivo = saldoVivo.add(c.getCapital());
            }
        }

        BigDecimal nuevoPrincipal = saldoVivo.subtract(montoAmortizacion);
        if (nuevoPrincipal.compareTo(BigDecimal.ZERO) <= 0) {
            return nuevo;
        }

        BigDecimal tasaAnual = credito.getTasaInteresCompensatorio();
        if (tasaAnual == null) {
            tasaAnual = BigDecimal.ZERO;
        }
        BigDecimal tasaMensual = tasaAnual
                .divide(BigDecimal.valueOf(12 * 100.0), SCALE_INTERES, ROUND_MODE);

        if ("REDUCIR_CUOTA".equalsIgnoreCase(tipoReduccion)) {
            nuevo = recalcularReduciendoCuota(nuevoPrincipal, tasaMensual, pendientes);
        } else {
            BigDecimal cuotaReferencia = pendientes.get(0).getMontoCuota();
            if (cuotaReferencia == null || cuotaReferencia.compareTo(BigDecimal.ZERO) <= 0) {
                nuevo = recalcularReduciendoCuota(nuevoPrincipal, tasaMensual, pendientes);
            } else {
                nuevo = recalcularReduciendoPlazo(nuevoPrincipal, tasaMensual, pendientes, cuotaReferencia);
            }
        }

        // Reasignamos nro_cuota desde 1..N en el nuevo cronograma
        int nro = 1;
        for (Cuota c : nuevo) {
            c.setCodigoContrato(credito.getCodigoContrato());
            c.setNroCuota(nro++);
            c.setEstadoCuota("Pendiente");
        }

        return nuevo;
    }

    /* =========================================================
     *  REPROGRAMACIÓN (nuevo plazo / nueva tasa)
     * ========================================================= */

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

        // 1. Cuotas pendientes (no pagadas)
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

        // 2. Saldo vivo = suma de capital (de cuotas pendientes)
        BigDecimal saldoVivo = BigDecimal.ZERO;
        for (Cuota c : pendientes) {
            if (c.getCapital() != null) {
                saldoVivo = saldoVivo.add(c.getCapital());
            }
        }

        if (saldoVivo.compareTo(BigDecimal.ZERO) <= 0) {
            return nuevo;
        }

        // 3. Tasa a usar: nueva (si se ingresó) o la actual
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

        // Base de fechas: la fecha de vencimiento de la primera cuota pendiente
        Date fechaBase = pendientes.get(0).getFechaVencimiento();
        if (fechaBase == null) {
            fechaBase = new Date();
        }

        BigDecimal saldo = saldoVivo;

        for (int i = 1; i <= n; i++) {
            Cuota c = new Cuota();
            c.setEstadoCuota("Pendiente");

            // Para la primera cuota usamos la misma fecha de la primera pendiente
            // y a partir de la segunda vamos sumando meses.
            Date fechaVenc = sumarMeses(fechaBase, i - 1);
            c.setFechaVencimiento(fechaVenc);

            BigDecimal interes = saldo.multiply(tasaMensual)
                    .setScale(SCALE_MONEDA, ROUND_MODE);
            BigDecimal capital = cuotaFija.subtract(interes)
                    .setScale(SCALE_MONEDA, ROUND_MODE);

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

        // IMPORTANTE: numerar cuotas y asignar contrato
        int nro = 1;
        for (Cuota c : nuevo) {
            c.setCodigoContrato(credito.getCodigoContrato());
            c.setNroCuota(nro++);
            c.setEstadoCuota("Pendiente");
        }

        return nuevo;
    }

    /* =========================================================
     *  AUXILIARES INTERNOS
     * ========================================================= */

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

            c.setFechaVencimiento(original.getFechaVencimiento());

            BigDecimal interes = saldo.multiply(tasaMensual)
                    .setScale(SCALE_MONEDA, ROUND_MODE);
            BigDecimal capital = cuotaFija.subtract(interes)
                    .setScale(SCALE_MONEDA, ROUND_MODE);

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

            c.setFechaVencimiento(original.getFechaVencimiento());

            BigDecimal interes = saldo.multiply(tasaMensual)
                    .setScale(SCALE_MONEDA, ROUND_MODE);
            BigDecimal capital = cuotaReferencia.subtract(interes)
                    .setScale(SCALE_MONEDA, ROUND_MODE);

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

    private Date sumarMeses(Date base, int meses) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(base);
        cal.add(Calendar.MONTH, meses);
        return cal.getTime();
    }
}