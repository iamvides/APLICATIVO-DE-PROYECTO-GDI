package com.cmhuancayo.creditos.unico;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CargaDb3 {

    // 👉 AQUÍ PEGAS TODO el contenido de tu 02_schema.sql LIMPIO
    //    usando un text block (Java 15+)
    private static final String SCHEMA_SQL = """

CREATE TABLE cliente (
    codigo_cliente      INTEGER GENERATED ALWAYS AS IDENTITY,
    apellido_paterno    VARCHAR(50)  NOT NULL,
    apellido_materno    VARCHAR(50),
    nombres             VARCHAR(100) NOT NULL,
    sexo                VARCHAR(1)   NOT NULL
                        CHECK (sexo IN ('M','F')),
    fecha_nacimiento    DATE         NOT NULL,
    tipo_documento      VARCHAR(25)  NOT NULL
                        CHECK (tipo_documento IN
                               ('DNI','CARNET_EXTRANJERIA','PASAPORTE','PNAC')),
    numero_documento    VARCHAR(15)  NOT NULL UNIQUE,
    lugar_nacimiento_pais         VARCHAR(30),
    lugar_nacimiento_departamento VARCHAR(30),
    lugar_nacimiento_provincia    VARCHAR(30),
    lugar_nacimiento_distrito     VARCHAR(30),
    estado_civil        VARCHAR(10) NOT NULL
                        CHECK (estado_civil IN
                               ('SOLTERO','CASADO','DIVORCIADO','VIUDO')),
    ruc                 CHAR(11),
    correo_electronico  VARCHAR(50),
    profesion           VARCHAR(40),
    regimen_tributario  VARCHAR(25),
    grado_instruccion   VARCHAR(15) NOT NULL
                        CHECK (grado_instruccion IN
                               ('ANALFABETO','PRIMARIA','SECUNDARIA',
                                'TECNICO','SUPERIOR')),
    scoring             NUMERIC(5,2) NOT NULL,
    -- 1 = activo, 0 = eliminado lógico
    estado_registro     SMALLINT     NOT NULL DEFAULT 1,
    CONSTRAINT pk_cliente PRIMARY KEY (codigo_cliente),
    CONSTRAINT chk_cliente_numero_documento
        CHECK (
            (tipo_documento = 'DNI'                  AND char_length(numero_documento) = 8)  OR
            (tipo_documento = 'CARNET_EXTRANJERIA'   AND char_length(numero_documento) BETWEEN 9 AND 12) OR
            (tipo_documento = 'PASAPORTE'            AND char_length(numero_documento) BETWEEN 8 AND 12) OR
            (tipo_documento = 'PNAC'                 AND char_length(numero_documento) BETWEEN 8 AND 10)
        )
);

CREATE TABLE carga_familiar_cliente (
    codigo_carga    INTEGER GENERATED ALWAYS AS IDENTITY,
    codigo_cliente  INTEGER NOT NULL,
    nombre          VARCHAR(100) NOT NULL,
    edad            INTEGER      NOT NULL,
    parentesco      VARCHAR(30)  NOT NULL,
    CONSTRAINT pk_carga_familiar_cliente
        PRIMARY KEY (codigo_carga, codigo_cliente),
    CONSTRAINT fk_carga_cliente
        FOREIGN KEY (codigo_cliente)
        REFERENCES cliente(codigo_cliente)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE telefono_cliente (
    codigo_telefono INTEGER GENERATED ALWAYS AS IDENTITY,
    codigo_cliente  INTEGER NOT NULL,
    telefono_celular VARCHAR(9) NOT NULL UNIQUE,
    CONSTRAINT pk_telefono_cliente
        PRIMARY KEY (codigo_telefono, codigo_cliente),
    CONSTRAINT fk_telefono_cliente
        FOREIGN KEY (codigo_cliente)
        REFERENCES cliente(codigo_cliente)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE domicilio_cliente (
    codigo_domicilio INTEGER GENERATED ALWAYS AS IDENTITY,
    codigo_cliente   INTEGER NOT NULL,
    direccion        VARCHAR(120) NOT NULL,
    referencia       VARCHAR(80),
    condicion_vivienda VARCHAR(20) NOT NULL,
    propietario      VARCHAR(80),
    anio_residencia  INTEGER NOT NULL,
    codigo_suministro VARCHAR(15) NOT NULL,
    CONSTRAINT pk_domicilio_cliente
        PRIMARY KEY (codigo_domicilio, codigo_cliente),
    CONSTRAINT fk_domicilio_cliente
        FOREIGN KEY (codigo_cliente)
        REFERENCES cliente(codigo_cliente)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE actividad_cliente (
    codigo_actividad INTEGER GENERATED ALWAYS AS IDENTITY,
    codigo_cliente   INTEGER NOT NULL,
    tipo_actividad   VARCHAR(30)  NOT NULL,
    nombre_negocio   VARCHAR(80),
    direccion        VARCHAR(120) NOT NULL,
    telefono         VARCHAR(9),
    fecha_inicio     DATE         NOT NULL,
    sector_economico VARCHAR(40)  NOT NULL,
    ingreso_promedio_mensual NUMERIC(8,2) NOT NULL,
    CONSTRAINT pk_actividad_cliente
        PRIMARY KEY (codigo_actividad, codigo_cliente),
    CONSTRAINT fk_actividad_cliente
        FOREIGN KEY (codigo_cliente)
        REFERENCES cliente(codigo_cliente)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE solicitud (
    codigo_solicitud  INTEGER GENERATED ALWAYS AS IDENTITY,
    codigo_cliente    INTEGER NOT NULL,
    fecha_solicitud   DATE    NOT NULL,
    monto             NUMERIC(8,2) NOT NULL,
    moneda            VARCHAR(10)  NOT NULL,
    plazo_dias        INTEGER      NOT NULL,
    nro_cuotas        INTEGER      NOT NULL,
    gracia            INTEGER,
    estado_solicitud  VARCHAR(20)  NOT NULL
                     CHECK (estado_solicitud IN ('Solicitado','En evaluación',
                                                  'Aprobado','Rechazado')),
    motivo            VARCHAR(60) NOT NULL,
    codigo_expediente VARCHAR(30)  NOT NULL UNIQUE,
    reevaluacion_semestral SMALLINT NOT NULL
                     CHECK (reevaluacion_semestral IN (0,1)),
    CONSTRAINT pk_solicitud PRIMARY KEY (codigo_solicitud),
    CONSTRAINT fk_solicitud_cliente
        FOREIGN KEY (codigo_cliente)
        REFERENCES cliente(codigo_cliente)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE pre_aprobacion (
    codigo_solicitud     INTEGER     NOT NULL,
    fecha_aprobacion     DATE        NOT NULL,
    estado_solicitud     VARCHAR(20) NOT NULL
                           CHECK (estado_solicitud IN
                                  ('SOLICITADO','EN REVISIÓN',
                                   'PREAPROBADO','RECHAZADO')),
    situacion_solicitud  VARCHAR(25) NOT NULL
                           CHECK (situacion_solicitud IN
                                  ('PENDIENTE DE COMITÉ','OBSERVADA',
                                   'EN ANÁLISIS','ACEPTADA','DENEGADA')),
    interviniente_mora   SMALLINT    NOT NULL
                           CHECK (interviniente_mora IN (0,1)),
    enviar_atencion      SMALLINT    NOT NULL
                           CHECK (enviar_atencion IN (0,1)),
    monto_preaprobado    NUMERIC(8,2) NOT NULL,
    moneda               VARCHAR(10)  NOT NULL,
    plazo_dias           INTEGER      NOT NULL,
    nro_cuotas           INTEGER      NOT NULL,
    comite               VARCHAR(50)  NOT NULL,
    tipo_acta            VARCHAR(30)  NOT NULL,
    gracia               INTEGER,
    CONSTRAINT pk_pre_aprobacion PRIMARY KEY (codigo_solicitud),
    CONSTRAINT fk_preaprobacion_solicitud
        FOREIGN KEY (codigo_solicitud)
        REFERENCES solicitud(codigo_solicitud)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE contrato (
    codigo_contrato              INTEGER GENERATED ALWAYS AS IDENTITY,
    codigo_solicitud             INTEGER      NOT NULL,
    monto_desembolso             NUMERIC(8,2) NOT NULL,
    monto_interes_compensatorio  NUMERIC(8,2) NOT NULL,
    moneda                       VARCHAR(10)  NOT NULL,
    plazo_credito                INTEGER      NOT NULL,
    numero_cuotas                INTEGER      NOT NULL,
    tasa_interes_compensatorio   NUMERIC(5,2) NOT NULL,
    tasa_interes_moratoria       NUMERIC(5,2) NOT NULL,
    tasa_costo_efectivo_anual    NUMERIC(5,2) NOT NULL,
    fecha_vigencia               DATE         NOT NULL,
    cuenta_credito               VARCHAR(20)  NOT NULL,
    linea_credito                VARCHAR(30)  NOT NULL,
    estado_contrato              VARCHAR(15)  NOT NULL
                                   DEFAULT 'VIGENTE'
                                   CHECK (estado_contrato IN
                                          ('VIGENTE','CANCELADO','REFINANCIADO')),
    CONSTRAINT pk_contrato PRIMARY KEY (codigo_contrato),
    CONSTRAINT fk_contrato_solicitud
        FOREIGN KEY (codigo_solicitud)
        REFERENCES pre_aprobacion(codigo_solicitud)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE cronograma_de_pago (
    nro_cuota          INTEGER      NOT NULL,
    codigo_contrato    INTEGER      NOT NULL,
    estado_cuota       VARCHAR(20)  NOT NULL,
    fecha_vencimiento  DATE         NOT NULL,
    capital            NUMERIC(8,2) NOT NULL,
    interes            NUMERIC(8,2) NOT NULL,
    seguro_degravamen  NUMERIC(8,2) NOT NULL,
    seguros_comisiones NUMERIC(8,2) NOT NULL,
    itf                NUMERIC(8,4) NOT NULL,
    monto_cuota        NUMERIC(8,2) NOT NULL,
    dias               INTEGER      NOT NULL,
    saldo_capital      NUMERIC(10,2) NOT NULL,
    CONSTRAINT pk_cronograma_de_pago
        PRIMARY KEY (nro_cuota, codigo_contrato),
    CONSTRAINT fk_cronograma_contrato
        FOREIGN KEY (codigo_contrato)
        REFERENCES contrato(codigo_contrato)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE analista (
    codigo_analista    INTEGER GENERATED ALWAYS AS IDENTITY,
    apellido_paterno   VARCHAR(50)  NOT NULL,
    apellido_materno   VARCHAR(50),
    nombres            VARCHAR(100) NOT NULL,
    CONSTRAINT pk_analista PRIMARY KEY (codigo_analista)
);

CREATE TABLE evalua (
    codigo_solicitud   INTEGER NOT NULL,
    codigo_analista    INTEGER NOT NULL,
    fecha_evaluacion   DATE    NOT NULL,
    resultado          VARCHAR(20) NOT NULL,
    observacion        VARCHAR(150),
    CONSTRAINT pk_evalua PRIMARY KEY (codigo_solicitud, codigo_analista),
    CONSTRAINT fk_evalua_solicitud
        FOREIGN KEY (codigo_solicitud)
        REFERENCES solicitud(codigo_solicitud)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_evalua_analista
        FOREIGN KEY (codigo_analista)
        REFERENCES analista(codigo_analista)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE movimiento_pago (
    codigo_movimiento  INTEGER GENERATED ALWAYS AS IDENTITY,
    codigo_contrato    INTEGER      NOT NULL,
    nro_cuota          INTEGER,
    fecha_movimiento   TIMESTAMP    NOT NULL,
    tipo_movimiento    VARCHAR(25)  NOT NULL
                        CHECK (tipo_movimiento IN
                               ('PAGO_CUOTA','PAGO_PARCIAL',
                                'AMORTIZACION_CAPITAL','PAGO_MORA',
                                'CONDONACION','CANCELACION_TOTAL')),
    monto              NUMERIC(8,2) NOT NULL,
    usuario_registro   VARCHAR(50),
    CONSTRAINT pk_movimiento_pago PRIMARY KEY (codigo_movimiento),
    CONSTRAINT fk_movimiento_contrato
        FOREIGN KEY (codigo_contrato)
        REFERENCES contrato(codigo_contrato)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE historial_cambios_cronograma (
    codigo_historial   INTEGER GENERATED ALWAYS AS IDENTITY,
    codigo_contrato    INTEGER      NOT NULL,
    fecha_cambio       TIMESTAMP    NOT NULL,
    tipo_cambio        VARCHAR(30)  NOT NULL
                        CHECK (tipo_cambio IN
                               ('REPROGRAMACION_PLAZO',
                                'CAMBIO_TASA',
                                'CAMBIO_FECHA_PAGO',
                                'REFINANCIACION',
                                'AMORTIZACION')),
    descripcion_cambio VARCHAR(255) NOT NULL,
    usuario_registro   VARCHAR(50),
    CONSTRAINT pk_historial_cambios PRIMARY KEY (codigo_historial),
    CONSTRAINT fk_historial_contrato
        FOREIGN KEY (codigo_contrato)
        REFERENCES contrato(codigo_contrato)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE historial_cronograma_detalle (
    codigo_historial_detalle INTEGER GENERATED ALWAYS AS IDENTITY,
    codigo_historial         INTEGER      NOT NULL,
    nro_cuota                INTEGER      NOT NULL,
    codigo_contrato          INTEGER      NOT NULL,
    estado_cuota             VARCHAR(20)  NOT NULL,
    fecha_vencimiento        DATE         NOT NULL,
    capital                  NUMERIC(10,2) NOT NULL,
    interes                  NUMERIC(10,2) NOT NULL,
    seguro_degravamen        NUMERIC(10,2) NOT NULL,
    seguros_comisiones       NUMERIC(10,2) NOT NULL,
    itf                      NUMERIC(10,2) NOT NULL,
    monto_cuota              NUMERIC(10,2) NOT NULL,
    dias                     INTEGER       NOT NULL,
    saldo_capital            NUMERIC(10,2),
    CONSTRAINT pk_historial_cronograma_detalle
        PRIMARY KEY (codigo_historial_detalle),
    CONSTRAINT fk_historial_detalle_historial
        FOREIGN KEY (codigo_historial)
        REFERENCES historial_cambios_cronograma(codigo_historial)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- Cliente
CREATE INDEX idx_cliente_apellido_paterno
    ON cliente(apellido_paterno);

CREATE INDEX idx_cliente_numero_documento
    ON cliente(numero_documento);

CREATE INDEX idx_cliente_scoring
    ON cliente(scoring);

-- Solicitud
CREATE INDEX idx_solicitud_fecha
    ON solicitud(fecha_solicitud);

CREATE INDEX idx_solicitud_monto
    ON solicitud(monto);

CREATE INDEX idx_solicitud_estado
    ON solicitud(estado_solicitud);

-- Pre_aprobacion
CREATE INDEX idx_preaprobacion_estado
    ON pre_aprobacion(estado_solicitud);

CREATE INDEX idx_preaprobacion_situacion
    ON pre_aprobacion(situacion_solicitud);

-- Contrato
CREATE INDEX idx_contrato_monto_desembolso
    ON contrato(monto_desembolso);

CREATE INDEX idx_contrato_fecha_vigencia
    ON contrato(fecha_vigencia);

-- Cronograma
CREATE INDEX idx_cronograma_monto_cuota
    ON cronograma_de_pago(monto_cuota);

-- Analista
CREATE INDEX idx_analista_apellido_paterno
    ON analista(apellido_paterno);

-- Evalua
CREATE INDEX idx_evalua_resultado
    ON evalua(resultado);

-- ============================================================================
-- sp_generarcronogramadepago
-- Genera (o regenera) el cronograma de un contrato
-- ============================================================================

CREATE OR REPLACE PROCEDURE sp_generarcronogramadepago(
    IN in_codigo_contrato INTEGER
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_monto_desembolso           NUMERIC(10,2);
    v_numero_cuotas              INTEGER;
    v_fecha_vigencia             DATE;
    v_tcea_anual                 NUMERIC(5,2);
    v_tea_anual                  NUMERIC(5,2);

    v_tasa_prima_seguro_mensual  NUMERIC(10,5) := 0.0008;
    v_tasa_itf                   NUMERIC(10,5) := 0.00005;

    v_tasa_diaria_interes        NUMERIC(20,10);
    v_tasa_mensual_cuota         NUMERIC(20,10);
    v_monto_cuota_fija           NUMERIC(10,2);
    v_saldo_pendiente            NUMERIC(10,2);
    v_saldo_posterior            NUMERIC(10,2);
    v_interes_mes                NUMERIC(8,2);
    v_capital_mes                NUMERIC(8,2);
    v_seguro_mes                 NUMERIC(8,2);
    v_itf_mes                    NUMERIC(8,4);
    v_fecha_vencimiento          DATE;
    v_fecha_anterior             DATE;
    v_dias_mes                   INTEGER;
    i                            INTEGER;
BEGIN
    -- 1) Obtener datos del contrato
    SELECT  monto_desembolso,
            numero_cuotas,
            fecha_vigencia,
            tasa_costo_efectivo_anual,
            tasa_interes_compensatorio
    INTO    v_monto_desembolso,
            v_numero_cuotas,
            v_fecha_vigencia,
            v_tcea_anual,
            v_tea_anual
    FROM contrato
    WHERE codigo_contrato = in_codigo_contrato;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Contrato % no existe', in_codigo_contrato
        USING ERRCODE = '45000';
    END IF;

    -- 2) Limpiar cronograma anterior
    DELETE FROM cronograma_de_pago
    WHERE codigo_contrato = in_codigo_contrato;

    -- 3) Cálculos iniciales
    v_saldo_pendiente   := v_monto_desembolso;
    v_fecha_anterior    := v_fecha_vigencia;
    v_tasa_diaria_interes :=
        power(1 + (v_tea_anual / 100), 1.0/360.0) - 1;
    v_tasa_mensual_cuota :=
        power(1 + (v_tcea_anual / 100), 1.0/12.0) - 1;

    v_monto_cuota_fija :=
        v_monto_desembolso *
        ( v_tasa_mensual_cuota *
          power(1 + v_tasa_mensual_cuota, v_numero_cuotas) ) /
        ( power(1 + v_tasa_mensual_cuota, v_numero_cuotas) - 1 );

    i := 1;

    -- 4) Generar cuotas
    WHILE i <= v_numero_cuotas LOOP
        v_fecha_vencimiento := (v_fecha_anterior + INTERVAL '1 month')::date;
        v_dias_mes          := (v_fecha_vencimiento - v_fecha_anterior);

        v_interes_mes := v_saldo_pendiente *
                         ( power(1 + v_tasa_diaria_interes, v_dias_mes) - 1 );
        v_seguro_mes  := (v_saldo_pendiente * v_tasa_prima_seguro_mensual)
                         * (v_dias_mes / 30.0);
        v_itf_mes     := v_monto_cuota_fija * v_tasa_itf;
        v_capital_mes := v_monto_cuota_fija
                         - v_interes_mes - v_seguro_mes - v_itf_mes;

        IF i = v_numero_cuotas THEN
            v_capital_mes    := v_saldo_pendiente;
            v_monto_cuota_fija :=
                v_capital_mes + v_interes_mes + v_seguro_mes + v_itf_mes;
        END IF;

        v_saldo_posterior := v_saldo_pendiente - v_capital_mes;

        INSERT INTO cronograma_de_pago (
            nro_cuota, codigo_contrato, estado_cuota,
            fecha_vencimiento,
            capital, interes, seguro_degravamen, seguros_comisiones,
            itf, monto_cuota, dias, saldo_capital
        )
        VALUES (
            i, in_codigo_contrato, 'Pendiente',
            v_fecha_vencimiento,
            v_capital_mes, v_interes_mes, v_seguro_mes, 0.00,
            v_itf_mes,
            v_capital_mes + v_interes_mes + v_seguro_mes + v_itf_mes,
            v_dias_mes, v_saldo_posterior
        );

        v_saldo_pendiente := v_saldo_posterior;
        v_fecha_anterior  := v_fecha_vencimiento;
        i := i + 1;
    END LOOP;
EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION
            'Error al generar cronograma (contrato %): %',
            in_codigo_contrato, SQLERRM
            USING ERRCODE = '45000';
END;
$$;

-- ============================================================================
-- sp_eliminarclientelogico
-- Marca al cliente como inactivo (eliminación lógica)
-- ============================================================================

CREATE OR REPLACE PROCEDURE sp_eliminarclientelogico(
    IN p_codigo_cliente INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE cliente
    SET estado_registro = 0
    WHERE codigo_cliente = p_codigo_cliente;
END;
$$;

-- ============================================================================
-- sp_registrarclientecompleto
-- Inserta cliente + domicilio + actividad + teléfono + cargas
-- ============================================================================

CREATE OR REPLACE PROCEDURE sp_registrarclientecompleto(
    IN p_apellido_paterno      VARCHAR(50),
    IN p_apellido_materno      VARCHAR(50),
    IN p_nombres               VARCHAR(100),
    IN p_sexo                  VARCHAR(1),
    IN p_fecha_nacimiento      DATE,
    IN p_estado_civil          VARCHAR(10),
    IN p_grado_instruccion     VARCHAR(15),
    IN p_numero_documento      VARCHAR(15),

    IN p_direccion             VARCHAR(120),
    IN p_referencia            VARCHAR(80),
    IN p_condicion_vivienda    VARCHAR(20),
    IN p_propietario           VARCHAR(80),
    IN p_anio_residencia       INTEGER,
    IN p_codigo_suministro     VARCHAR(15),

    IN p_tipo_actividad        VARCHAR(30),
    IN p_nombre_negocio        VARCHAR(80),
    IN p_direccion_negocio     VARCHAR(120),
    IN p_telefono_negocio      VARCHAR(9),
    IN p_fecha_inicio_act      DATE,
    IN p_sector_economico      VARCHAR(40),
    IN p_ingreso_mensual       NUMERIC(8,2),

    IN p_telefono_celular      VARCHAR(9),
    IN p_scoring               NUMERIC(5,2),
    IN p_num_dependientes      INTEGER
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_codigo_cliente INTEGER;
    v_scoring        NUMERIC(5,2);
    v_ingreso        NUMERIC(8,2);
    i                INTEGER;
BEGIN
    v_scoring := COALESCE(p_scoring, 80.00);
    v_ingreso := COALESCE(p_ingreso_mensual, 0.00);

    -- 1) CLIENTE
    INSERT INTO cliente(
        apellido_paterno,
        apellido_materno,
        nombres,
        sexo,
        fecha_nacimiento,
        tipo_documento,
        numero_documento,
        lugar_nacimiento_pais,
        lugar_nacimiento_departamento,
        lugar_nacimiento_provincia,
        lugar_nacimiento_distrito,
        estado_civil,
        ruc,
        correo_electronico,
        profesion,
        regimen_tributario,
        grado_instruccion,
        scoring,
        estado_registro
    )
    VALUES(
        p_apellido_paterno,
        p_apellido_materno,
        p_nombres,
        p_sexo,
        p_fecha_nacimiento,
        'DNI',
        p_numero_documento,
        NULL, NULL, NULL, NULL,
        p_estado_civil,
        NULL,
        NULL,
        NULL,
        NULL,
        p_grado_instruccion,
        v_scoring,
        1
    )
    RETURNING codigo_cliente INTO v_codigo_cliente;

    -- 2) DOMICILIO
    INSERT INTO domicilio_cliente(
        codigo_cliente,
        direccion,
        referencia,
        condicion_vivienda,
        propietario,
        anio_residencia,
        codigo_suministro
    )
    VALUES(
        v_codigo_cliente,
        p_direccion,
        p_referencia,
        p_condicion_vivienda,
        p_propietario,
        p_anio_residencia,
        p_codigo_suministro
    );

    -- 3) ACTIVIDAD / INGRESOS
    INSERT INTO actividad_cliente(
        codigo_cliente,
        tipo_actividad,
        nombre_negocio,
        direccion,
        telefono,
        fecha_inicio,
        sector_economico,
        ingreso_promedio_mensual
    )
    VALUES(
        v_codigo_cliente,
        p_tipo_actividad,
        p_nombre_negocio,
        p_direccion_negocio,
        p_telefono_negocio,
        p_fecha_inicio_act,
        p_sector_economico,
        v_ingreso
    );

    -- 4) TELÉFONO PRINCIPAL
    IF p_telefono_celular IS NOT NULL AND p_telefono_celular <> '' THEN
        INSERT INTO telefono_cliente(codigo_cliente, telefono_celular)
        VALUES (v_codigo_cliente, p_telefono_celular);
    END IF;

    -- 5) CARGA FAMILIAR
    IF p_num_dependientes IS NOT NULL AND p_num_dependientes > 0 THEN
        i := 1;
        WHILE i <= p_num_dependientes LOOP
            INSERT INTO carga_familiar_cliente(
                codigo_cliente, nombre, edad, parentesco
            )
            VALUES (
                v_codigo_cliente,
                'HIJO ' || i,
                0,
                'HIJO'
            );
            i := i + 1;
        END LOOP;
    END IF;
END;
$$;

-- ============================================================================
-- sp_crearcreditoclientebasico
-- Crea solicitud + pre_aprobación + contrato + cronograma
-- Tiene parámetro OUT (INOUT en PostgreSQL)
-- ============================================================================

CREATE OR REPLACE PROCEDURE sp_crearcreditoclientebasico(
    IN  p_codigo_cliente   INTEGER,
    IN  p_monto            NUMERIC(8,2),
    IN  p_nro_cuotas       INTEGER,
    IN  p_tasa_anual       NUMERIC(5,2),
    IN  p_fecha_desembolso DATE,
    INOUT p_codigo_contrato INTEGER
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_codigo_solicitud   INTEGER;
    v_plazo_dias         INTEGER;
    v_codigo_expediente  VARCHAR(30);
    v_monto_interes      NUMERIC(8,2);
    v_tasa_mora          NUMERIC(5,2);
    v_tcea               NUMERIC(5,2);
    v_creditos_vigentes  INTEGER;
BEGIN
    -- 1) Validar límite de 4 créditos vigentes
    SELECT COUNT(*)
    INTO v_creditos_vigentes
    FROM contrato c
    JOIN solicitud s ON c.codigo_solicitud = s.codigo_solicitud
    WHERE s.codigo_cliente = p_codigo_cliente
      AND c.estado_contrato = 'VIGENTE';

    IF v_creditos_vigentes >= 4 THEN
        RAISE EXCEPTION 'El cliente ya tiene 4 créditos vigentes.'
        USING ERRCODE = '45000';
    END IF;

    -- 2) Cálculos generales
    v_plazo_dias        := p_nro_cuotas * 30;
    v_codigo_expediente := 'EXP' ||
                           to_char(NOW(), 'YYMMDDHH24MISS') ||
                           lpad(p_codigo_cliente::text, 3, '0');

    v_tasa_mora := 18.00;
    v_tcea      := p_tasa_anual + 2.00;
    v_monto_interes :=
        round(p_monto * (p_tasa_anual / 100.0) * (v_plazo_dias / 360.0), 2);

    -- 3) SOLICITUD
    INSERT INTO solicitud(
        codigo_cliente,
        fecha_solicitud,
        monto,
        moneda,
        plazo_dias,
        nro_cuotas,
        gracia,
        estado_solicitud,
        motivo,
        codigo_expediente,
        reevaluacion_semestral
    ) VALUES (
        p_codigo_cliente,
        CURRENT_DATE,
        p_monto,
        'SOLES',
        v_plazo_dias,
        p_nro_cuotas,
        0,
        'Aprobado',
        'Crédito personal generado desde sistema',
        v_codigo_expediente,
        1
    )
    RETURNING codigo_solicitud INTO v_codigo_solicitud;

    -- 4) PRE_APROBACION
    INSERT INTO pre_aprobacion(
        codigo_solicitud,
        fecha_aprobacion,
        estado_solicitud,
        situacion_solicitud,
        interviniente_mora,
        enviar_atencion,
        monto_preaprobado,
        moneda,
        plazo_dias,
        nro_cuotas,
        comite,
        tipo_acta,
        gracia
    ) VALUES (
        v_codigo_solicitud,
        CURRENT_DATE,
        'PREAPROBADO',
        'ACEPTADA',
        0,
        1,
        p_monto,
        'SOLES',
        v_plazo_dias,
        p_nro_cuotas,
        'Comité Automático',
        'ACTA-' || v_codigo_solicitud::text,
        0
    );

    -- 5) CONTRATO
    INSERT INTO contrato(
        codigo_solicitud,
        monto_desembolso,
        monto_interes_compensatorio,
        moneda,
        plazo_credito,
        numero_cuotas,
        tasa_interes_compensatorio,
        tasa_interes_moratoria,
        tasa_costo_efectivo_anual,
        fecha_vigencia,
        cuenta_credito,
        linea_credito,
        estado_contrato
    ) VALUES (
        v_codigo_solicitud,
        p_monto,
        v_monto_interes,
        'SOLES',
        v_plazo_dias,
        p_nro_cuotas,
        p_tasa_anual,
        v_tasa_mora,
        v_tcea,
        p_fecha_desembolso,
        'CRSYS_' || lpad(v_codigo_solicitud::text, 6, '0'),
        'Consumo',
        'VIGENTE'
    )
    RETURNING codigo_contrato INTO p_codigo_contrato;

    -- 6) CRONOGRAMA
    CALL sp_generarcronogramadepago(p_codigo_contrato);
END;
$$;

-- ============================================================================
-- sp_registrar_pago_cuota
-- Inserta un movimiento de tipo PAGO_CUOTA
-- ============================================================================

CREATE OR REPLACE PROCEDURE sp_registrar_pago_cuota(
    IN p_codigo_contrato INTEGER,
    IN p_nro_cuota       INTEGER,
    IN p_monto           NUMERIC(8,2),
    IN p_usuario         VARCHAR(50)
)
LANGUAGE plpgsql
AS $$
BEGIN
    -- 1) Registrar movimiento
    INSERT INTO movimiento_pago(
        codigo_contrato,
        nro_cuota,
        fecha_movimiento,
        tipo_movimiento,
        monto,
        usuario_registro
    )
    VALUES(
        p_codigo_contrato,
        p_nro_cuota,
        CURRENT_TIMESTAMP,
        'PAGO_CUOTA',
        p_monto,
        p_usuario
    );

    -- 2) Marcar la cuota como Pagada
    UPDATE cronograma_de_pago
       SET estado_cuota = 'Pagada'
     WHERE codigo_contrato = p_codigo_contrato
       AND nro_cuota       = p_nro_cuota;

    -- 3) Si YA NO EXISTE ninguna cuota distinta de 'Pagada',
    --    marcar el contrato como CANCELADO
    IF NOT EXISTS (
        SELECT 1
          FROM cronograma_de_pago
         WHERE codigo_contrato = p_codigo_contrato
           AND estado_cuota <> 'Pagada'
    ) THEN
        UPDATE contrato
           SET estado_contrato = 'CANCELADO'
         WHERE codigo_contrato = p_codigo_contrato;
    END IF;
END;
$$;

-- ============================================================================
-- sp_registrarpagoparcial
-- Distribuye un monto sobre varias cuotas (PAGO_PARCIAL)
-- ============================================================================

CREATE OR REPLACE PROCEDURE sp_registrarpagoparcial(
    IN p_codigo_contrato  INTEGER,
    IN p_nro_cuota_inicio INTEGER,
    IN p_monto_pago       NUMERIC(10,2),
    IN p_usuario          VARCHAR(50)
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_monto_restante NUMERIC(10,2);
    v_nro_cuota      INTEGER;
    v_max_nro        INTEGER;
    v_monto_cuota    NUMERIC(10,2);
    v_estado_actual  VARCHAR(20);
    v_pagado_actual  NUMERIC(10,2);
    v_saldo_cuota    NUMERIC(10,2);
BEGIN
    IF p_monto_pago <= 0 THEN
        RAISE EXCEPTION 'El monto a pagar debe ser mayor que cero'
        USING ERRCODE = '45000';
    END IF;

    SELECT MAX(nro_cuota)
    INTO v_max_nro
    FROM cronograma_de_pago
    WHERE codigo_contrato = p_codigo_contrato;

    IF v_max_nro IS NULL THEN
        RAISE EXCEPTION 'El contrato % no tiene cronograma de pago',
            p_codigo_contrato
        USING ERRCODE = '45000';
    END IF;

    v_monto_restante := p_monto_pago;
    v_nro_cuota      := p_nro_cuota_inicio;

    IF v_nro_cuota < 1 THEN
        v_nro_cuota := 1;
    END IF;
    IF v_nro_cuota > v_max_nro THEN
        v_nro_cuota := v_max_nro;
    END IF;

    WHILE v_nro_cuota <= v_max_nro AND v_monto_restante > 0 LOOP

        SELECT  c.monto_cuota,
                c.estado_cuota,
                COALESCE(SUM(m.monto),0)
        INTO    v_monto_cuota,
                v_estado_actual,
                v_pagado_actual
        FROM cronograma_de_pago c
        LEFT JOIN movimiento_pago m
               ON m.codigo_contrato = c.codigo_contrato
              AND m.nro_cuota       = c.nro_cuota
              AND m.tipo_movimiento IN ('PAGO_CUOTA','PAGO_PARCIAL')
        WHERE c.codigo_contrato = p_codigo_contrato
          AND c.nro_cuota       = v_nro_cuota
        GROUP BY c.monto_cuota, c.estado_cuota;

        IF v_monto_cuota IS NULL THEN
            EXIT;
        END IF;

        IF v_pagado_actual >= v_monto_cuota
           OR v_estado_actual = 'Pagada' THEN
            v_nro_cuota := v_nro_cuota + 1;
            CONTINUE;
        END IF;

        v_saldo_cuota := v_monto_cuota - v_pagado_actual;

        IF v_monto_restante >= v_saldo_cuota THEN
            -- Completa la cuota
            INSERT INTO movimiento_pago(
                codigo_contrato, nro_cuota,
                fecha_movimiento, tipo_movimiento,
                monto, usuario_registro
            ) VALUES (
                p_codigo_contrato, v_nro_cuota,
                CURRENT_TIMESTAMP, 'PAGO_PARCIAL',
                v_saldo_cuota, p_usuario
            );

            UPDATE cronograma_de_pago
            SET estado_cuota = 'Pagada'
            WHERE codigo_contrato = p_codigo_contrato
              AND nro_cuota       = v_nro_cuota;

            v_monto_restante := v_monto_restante - v_saldo_cuota;
            v_nro_cuota      := v_nro_cuota + 1;

        ELSE
            -- Solo parte de la cuota
            INSERT INTO movimiento_pago(
                codigo_contrato, nro_cuota,
                fecha_movimiento, tipo_movimiento,
                monto, usuario_registro
            ) VALUES (
                p_codigo_contrato, v_nro_cuota,
                CURRENT_TIMESTAMP, 'PAGO_PARCIAL',
                v_monto_restante, p_usuario
            );

            UPDATE cronograma_de_pago
            SET estado_cuota = 'PARCIAL'
            WHERE codigo_contrato = p_codigo_contrato
              AND nro_cuota       = v_nro_cuota;

            v_monto_restante := 0;
        END IF;
    END LOOP;
END;
$$;

-- ============================================================================
-- sp_archivarcronogramaactual
-- Copia el cronograma actual a las tablas de historial
-- ============================================================================

CREATE OR REPLACE PROCEDURE sp_archivarcronogramaactual(
    IN p_codigo_contrato INTEGER,
    IN p_tipo_cambio     VARCHAR(30),
    IN p_descripcion     VARCHAR(255),
    IN p_usuario         VARCHAR(50)
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_codigo_historial INTEGER;
BEGIN
    -- 1) Cabecera
    INSERT INTO historial_cambios_cronograma(
        codigo_contrato,
        fecha_cambio,
        tipo_cambio,
        descripcion_cambio,
        usuario_registro
    )
    VALUES(
        p_codigo_contrato,
        CURRENT_TIMESTAMP,
        p_tipo_cambio,
        p_descripcion,
        p_usuario
    )
    RETURNING codigo_historial INTO v_codigo_historial;

    -- 2) Detalle
    INSERT INTO historial_cronograma_detalle(
        codigo_historial,
        nro_cuota,
        codigo_contrato,
        estado_cuota,
        fecha_vencimiento,
        capital,
        interes,
        seguro_degravamen,
        seguros_comisiones,
        itf,
        monto_cuota,
        dias,
        saldo_capital
    )
    SELECT
        v_codigo_historial,
        cp.nro_cuota,
        cp.codigo_contrato,
        cp.estado_cuota,
        cp.fecha_vencimiento,
        cp.capital,
        cp.interes,
        cp.seguro_degravamen,
        cp.seguros_comisiones,
        cp.itf,
        cp.monto_cuota,
        cp.dias,
        cp.saldo_capital
    FROM cronograma_de_pago cp
    WHERE cp.codigo_contrato = p_codigo_contrato;
END;
$$;

-- ============================================================================
-- sp_recalcularestadocliente (dummy)
-- ============================================================================
CREATE OR REPLACE PROCEDURE sp_recalcularestadocliente(
    IN p_codigo_cliente INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    -- Ya no actualiza una columna; el estado se calcula al vuelo en las consultas.
    RAISE NOTICE 'sp_recalcularestadocliente(%) ejecutado (sin cambios).',
                 p_codigo_cliente;
END;
$$;

-- ============================================================================
-- sp_recalcularestadostodosclientes (cursor simple)
-- ============================================================================
CREATE OR REPLACE PROCEDURE sp_recalcularestadostodosclientes()
LANGUAGE plpgsql
AS $$
DECLARE
    v_codigo INTEGER;
BEGIN
    FOR v_codigo IN
        SELECT codigo_cliente FROM cliente
    LOOP
        CALL sp_recalcularestadocliente(v_codigo);
    END LOOP;
END;
$$;

-- ============================================================================
-- TRIGGER movimiento_pago AFTER INSERT
-- ============================================================================

CREATE OR REPLACE FUNCTION trg_movimiento_pago_ai_fn()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.tipo_movimiento = 'PAGO_CUOTA'
       AND NEW.nro_cuota IS NOT NULL THEN
        UPDATE cronograma_de_pago
        SET estado_cuota = 'Pagada'
        WHERE codigo_contrato = NEW.codigo_contrato
          AND nro_cuota       = NEW.nro_cuota;
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_movimiento_pago_ai
    ON movimiento_pago;

CREATE TRIGGER trg_movimiento_pago_ai
AFTER INSERT ON movimiento_pago
FOR EACH ROW
EXECUTE FUNCTION trg_movimiento_pago_ai_fn();

-- ============================================================================
-- 3. INSERTS DE PRUEBA
-- ============================================================================

-- 3.1 Clientes
INSERT INTO cliente (
    apellido_paterno,
    apellido_materno,
    nombres,
    sexo,
    fecha_nacimiento,
    tipo_documento,
    numero_documento,
    lugar_nacimiento_pais,
    lugar_nacimiento_departamento,
    lugar_nacimiento_provincia,
    lugar_nacimiento_distrito,
    estado_civil,
    ruc,
    correo_electronico,
    profesion,
    regimen_tributario,
    grado_instruccion,
    scoring,
    estado_registro
) VALUES
('Huamán',   'Gutiérrez', 'Luis Alberto',      'M', DATE '1991-04-12', 'DNI', '76451234', 'PERÚ', 'JUNÍN', 'HUANCAYO', 'EL TAMBO',  'CASADO',   NULL, 'lhuaman@gmail.com',           'ING. INDUSTRIAL', 'RUS',           'SUPERIOR', 82.50, 1),
('Quispe',   'Soto',      'María Fernanda',   'F', DATE '1995-09-23', 'DNI', '75678901', 'PERÚ', 'JUNÍN', 'HUANCAYO', 'CHILCA',    'SOLTERO',  NULL, 'maria.quispe@hotmail.com',   'CONTADORA',       'RUS',           'SUPERIOR', 88.30, 1),
('Rojas',    'Mamani',    'Jorge Enrique',    'M', DATE '1988-01-08', 'DNI', '74890123', 'PERÚ', 'AYACUCHO','HUAMANGA','AYACUCHO', 'CASADO',   NULL, 'jrojas@gmail.com',           'ING. CIVIL',      'REGIMEN MYPE',  'SUPERIOR', 79.90, 1),
('Pérez',    'Lazo',      'Carla Paola',      'F', DATE '1993-06-17', 'DNI', '73984561', 'PERÚ', 'CUSCO', 'CUSCO',    'WANCHAQ',   'SOLTERO',  NULL, 'carlap.lazo@yahoo.es',       'ADMINISTRADORA',  'RUS',           'SUPERIOR', 91.20, 1),
('Condori',  'Salas',     'Diego Armando',    'M', DATE '1990-11-30', 'DNI', '73214567', 'PERÚ', 'PUNO',  'PUNO',     'PUNO',      'CASADO',   NULL, 'dcondori@gmail.com',         'DOCENTE',         '4TA',           'SUPERIOR', 84.10, 1),
('Ramírez',  'Paredes',   'Lucía Beatriz',    'F', DATE '1994-03-05', 'DNI', '72563418', 'PERÚ', 'JUNÍN', 'HUANCAYO', 'HUANCAYO',  'SOLTERO',  NULL, 'lucia.rp@gmail.com',         'ABOGADA',         'RUS',           'SUPERIOR', 89.75, 1),
('Vargas',   'Cárdenas',  'Ricardo Jesús',    'M', DATE '1987-07-21', 'DNI', '71928364', 'PERÚ', 'LIMA',  'LIMA',     'ATE',       'CASADO',   NULL, 'rvargas@hotmail.com',        'TÉC. ELECTRÓNICO','MYPE',          'TECNICO',  81.40, 1),
('Flores',   'Ñahui',     'Tatiana Roxana',   'F', DATE '1996-02-14', 'DNI', '71345982', 'PERÚ', 'APURÍMAC','ABANCAY','ABANCAY', 'SOLTERO',  NULL, 'tatiana.flores@gmail.com',   'ING. SISTEMAS',   'RUS',           'SUPERIOR', 92.10, 1),
('García',   'Reátegui',  'Pedro Alejandro',  'M', DATE '1992-05-09', 'DNI', '70897654', 'PERÚ', 'SAN MARTÍN','MOYOBAMBA','MOYOBAMBA','SOLTERO',NULL,'pgarcia@gmail.com',         'ING. AGRÓNOMO',   'RUS',           'SUPERIOR', 83.60, 1),
('Suárez',   'Campos',    'Brenda Sofía',     'F', DATE '1997-12-01', 'DNI', '70123498', 'PERÚ', 'LA LIBERTAD','TRUJILLO','TRUJILLO','SOLTERO',NULL,'brenda.sc@hotmail.com',     'ENFERMERA',       '4TA',           'SUPERIOR', 87.90, 1);

-- 3.2 Teléfonos
INSERT INTO telefono_cliente (codigo_cliente, telefono_celular)
SELECT c.codigo_cliente, v.tel
FROM (
    VALUES
     ('76451234','987456321'),
     ('75678901','984123765'),
     ('74890123','989654321'),
     ('73984561','991237845'),
     ('73214567','986543219'),
     ('72563418','983214567'),
     ('71928364','982345671'),
     ('71345982','981234569'),
     ('70897654','980987654'),
     ('70123498','989321654')
) AS v(dni, tel)
JOIN cliente c ON c.numero_documento = v.dni;

-- 3.3 Domicilio  (CORREGIDO: ya no se usa d.*)
INSERT INTO domicilio_cliente(
    codigo_cliente,
    direccion,
    referencia,
    condicion_vivienda,
    propietario,
    anio_residencia,
    codigo_suministro
)
SELECT
    c.codigo_cliente,
    d.dir,
    d.ref,
    d.cond,
    d.prop,
    d.anios,
    d.sum
FROM (
    VALUES
('76451234','Jr. Real 345 - El Tambo','Frente al colegio Salesiano','PROPIA','Luis Huamán', 4,'S30001'),
('75678901','Pasaje Los Pinos 120 - Chilca','Espalda del parque Los Pinos','ALQUILADA','María Fernanda Quispe',2,'S30002'),
('74890123','Av. Mariscal Cáceres 980','A tres cuadras del terminal','PROPIA','Jorge Rojas',6,'S30003'),
('73984561','Urb. Santa Mónica Mz B Lt 4','Cerca al hospital regional','ALQUILADA','Carla Pérez',3,'S30004'),
('73214567','Jr. Moquegua 450','Frente al mercado central','PROPIA','Diego Condori',5,'S30005'),
('72563418','Av. Giráldez 210','Al costado de botica InkaFarma','ALQUILADA','Lucía Ramírez',2,'S30006'),
('71928364','Av. Metropolitana 1020 - Ate','Paradero óvalo Ate','PROPIA','Ricardo Vargas',7,'S30007'),
('71345982','Jr. Grau 530 - Abancay','Frente al parque principal','PROPIA','Tatiana Flores',3,'S30008'),
('70897654','Jr. San Martín 225 - Moyobamba','A media cuadra de la plaza','ALQUILADA','Pedro García',1,'S30009'),
('70123498','Urb. San Andrés Mz C Lt 8 - Trujillo','Cerca al mall Aventura Plaza','PROPIA','Brenda Suárez',4,'S30010')
) AS d(dni, dir, ref, cond, prop, anios, sum)
JOIN cliente c ON c.numero_documento = d.dni;

-- 3.4 Actividad
INSERT INTO actividad_cliente(
    codigo_cliente,
    tipo_actividad,
    nombre_negocio,
    direccion,
    telefono,
    fecha_inicio,
    sector_economico,
    ingreso_promedio_mensual
)
SELECT c.codigo_cliente, a.tipo_act, a.nom_neg, a.dir, a.tel,
       a.fec_ini, a.sector, a.ingreso
FROM (
    VALUES
('76451234','DEPENDIENTE','Backus Huancayo','Planta El Tambo','987456321',DATE '2018-03-01','INDUSTRIAL',4200.00),
('75678901','INDEPENDIENTE','Bodega Santa Rosa','Jr. Libertad 210 - Chilca','984123765',DATE '2020-07-15','COMERCIAL',2800.00),
('74890123','DEPENDIENTE','Municipalidad Provincial','Plaza de Armas s/n','989654321',DATE '2017-01-10','PUBLICO',3500.00),
('73984561','INDEPENDIENTE','Café Qosqo','Calle Plateros 450 - Cusco','991237845',DATE '2019-05-20','GASTRONOMÍA',3200.00),
('73214567','DEPENDIENTE','IE San Juan Bosco','Jr. Tacna 345 - Puno','986543219',DATE '2016-03-01','EDUCACION',3100.00),
('72563418','INDEPENDIENTE','Estudio Jurídico Ramírez','Jr. Puno 120 - Huancayo','983214567',DATE '2021-02-01','SERVICIOS',3900.00),
('71928364','DEPENDIENTE','Empresa Electrotec','Av. Nicolás Ayllón 4520 - Ate','982345671',DATE '2015-09-15','SERVICIOS',3300.00),
('71345982','INDEPENDIENTE','Soluciones TI Abancay','Av. Garcilaso 560','981234569',DATE '2022-01-05','TECNOLOGÍA',3000.00),
('70897654','DEPENDIENTE','AgroSan Martín SAC','Carretera Fernando Belaunde','980987654',DATE '2018-06-01','AGROINDUSTRIA',3400.00),
('70123498','INDEPENDIENTE','Consultorio Santa Sofía','Jr. Zela 140 - Trujillo','989321654',DATE '2021-08-12','SALUD',3100.00)
) AS a(dni, tipo_act, nom_neg, dir, tel, fec_ini, sector, ingreso)
JOIN cliente c ON c.numero_documento = a.dni;

-- 3.5 Solicitudes
INSERT INTO solicitud(
    codigo_cliente,
    fecha_solicitud,
    monto,
    moneda,
    plazo_dias,
    nro_cuotas,
    gracia,
    estado_solicitud,
    motivo,
    codigo_expediente,
    reevaluacion_semestral
)
SELECT
    c.codigo_cliente,
    (DATE '2024-09-15' + (rn * INTERVAL '1 day'))::date AS fecha_solicitud,
    CASE
        WHEN c.numero_documento IN ('76451234','75678901') THEN 12000.00
        WHEN c.numero_documento IN ('74890123','73984561') THEN 15000.00
        WHEN c.numero_documento IN ('73214567','72563418') THEN 10000.00
        WHEN c.numero_documento IN ('71928364','71345982') THEN  8000.00
        ELSE 9000.00
    END AS monto,
    'SOLES' AS moneda,
    360 AS plazo_dias,
    12  AS nro_cuotas,
    0   AS gracia,
    'Aprobado' AS estado_solicitud,
    'Crédito personal para ' || c.nombres AS motivo,
    'EXPGEN_' || c.numero_documento AS codigo_expediente,
    1 AS reevaluacion_semestral
FROM (
    SELECT c.*,
           ROW_NUMBER() OVER (ORDER BY c.numero_documento) AS rn
    FROM cliente c
    WHERE c.numero_documento IN (
        '76451234','75678901','74890123','73984561','73214567',
        '72563418','71928364','71345982','70897654','70123498'
    )
) c;

-- 3.6 Pre-aprobación
INSERT INTO pre_aprobacion(
    codigo_solicitud,
    fecha_aprobacion,
    estado_solicitud,
    situacion_solicitud,
    interviniente_mora,
    enviar_atencion,
    monto_preaprobado,
    moneda,
    plazo_dias,
    nro_cuotas,
    comite,
    tipo_acta,
    gracia
)
SELECT
    s.codigo_solicitud,
    s.fecha_solicitud + INTERVAL '3 days' AS fecha_aprobacion,
    'PREAPROBADO',
    'ACEPTADA',
    0,
    1,
    s.monto,
    s.moneda,
    s.plazo_dias,
    s.nro_cuotas,
    'Comité Huancayo',
    'Acta GEN-' || s.codigo_solicitud::text,
    0
FROM solicitud s
JOIN cliente c ON c.codigo_cliente = s.codigo_cliente
WHERE c.numero_documento IN (
    '76451234','75678901','74890123','73984561','73214567',
    '72563418','71928364','71345982','70897654','70123498'
);

-- 3.7 Contratos
INSERT INTO contrato(
    codigo_solicitud,
    monto_desembolso,
    monto_interes_compensatorio,
    moneda,
    plazo_credito,
    numero_cuotas,
    tasa_interes_compensatorio,
    tasa_interes_moratoria,
    tasa_costo_efectivo_anual,
    fecha_vigencia,
    cuenta_credito,
    linea_credito,
    estado_contrato
)
SELECT
    p.codigo_solicitud,
    p.monto_preaprobado,
    round(p.monto_preaprobado * 0.15, 2),
    p.moneda,
    p.plazo_dias,
    p.nro_cuotas,
    13.5,
    18.0,
    20.2,
    p.fecha_aprobacion + INTERVAL '5 days',
    'CRGEN_' || lpad(p.codigo_solicitud::text, 6, '0'),
    'Consumo',
    'VIGENTE'
FROM pre_aprobacion p
JOIN solicitud s ON s.codigo_solicitud = p.codigo_solicitud
JOIN cliente   c ON c.codigo_cliente   = s.codigo_cliente
WHERE c.numero_documento IN (
    '76451234','75678901','74890123','73984561','73214567',
    '72563418','71928364','71345982','70897654','70123498'
);



-- 3.8 Generar cronogramas para los contratos creados
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN SELECT codigo_contrato FROM contrato LOOP
        CALL sp_generarcronogramadepago(r.codigo_contrato);
    END LOOP;
END;
$$;

-- Función envoltorio para poder usarla cómodamente desde JDBC
CREATE OR REPLACE FUNCTION fn_crearcreditoclientebasico(
    p_codigo_cliente   INTEGER,
    p_monto            NUMERIC(8,2),
    p_nro_cuotas       INTEGER,
    p_tasa_anual       NUMERIC(5,2),
    p_fecha_desembolso DATE
)
RETURNS INTEGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_codigo_contrato INTEGER;
BEGIN
    -- Llamamos al PROCEDURE original (INOUT en el último parámetro)
    v_codigo_contrato := 0;
    CALL sp_crearcreditoclientebasico(
        p_codigo_cliente,
        p_monto,
        p_nro_cuotas,
        p_tasa_anual,
        p_fecha_desembolso,
        v_codigo_contrato
    );
    RETURN v_codigo_contrato;
END;
$$;


""";

    public static void ejecutar(Connection conn) throws SQLException {
        ejecutarScript(conn, SCHEMA_SQL);
    }

    /**
     * Ejecuta el script completo respetando:
     *  - bloques $$...$$
     *  - comillas simples '...'
     *  - que hay muchos ';' dentro de funciones
     */
    private static void ejecutarScript(Connection conn, String script) throws SQLException {
        Statement st = null;
        try {
            st = conn.createStatement();

            StringBuilder sb = new StringBuilder();
            boolean dentroComillaSimple = false;
            boolean dentroDollar = false;

            for (int i = 0; i < script.length(); i++) {
                char c = script.charAt(i);
                char next = (i + 1 < script.length()) ? script.charAt(i + 1) : '\0';

                // Manejo de $$ ... $$
                if (!dentroComillaSimple && c == '$' && next == '$') {
                    sb.append(c).append(next);
                    dentroDollar = !dentroDollar;
                    i++; // saltar el segundo $
                    continue;
                }

                // Manejo de comillas simples
                if (!dentroDollar && c == '\'') {
                    dentroComillaSimple = !dentroComillaSimple;
                    sb.append(c);
                    continue;
                }

                // Comentarios de línea --
                if (!dentroComillaSimple && !dentroDollar && c == '-' && next == '-') {
                    // consumir hasta el fin de la línea
                    while (i < script.length() && script.charAt(i) != '\n') {
                        i++;
                    }
                    sb.append('\n');
                    continue;
                }

                // Fin de sentencia: ';' fuera de $$ y fuera de comillas
                if (!dentroComillaSimple && !dentroDollar && c == ';') {
                    sb.append(c);
                    String sentencia = sb.toString().trim();
                    if (!sentencia.isEmpty()) {
                        st.execute(sentencia);
                    }
                    sb.setLength(0);
                    continue;
                }

                sb.append(c);
            }

            // Por si queda algo sin ';' al final
            String restante = sb.toString().trim();
            if (!restante.isEmpty()) {
                st.execute(restante);
            }

        } finally {
            if (st != null) st.close();
        }
    }
}
