package com.example.proyectocurso

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.proyectocurso.distribucion.Ac_Quadruple
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class miSQLiteHelper(context: Context) :
// Incrementamos la versión de la base
    SQLiteOpenHelper(context, "medidores.db", null, 30) {

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL("PRAGMA foreign_keys = ON")

        // === CREACIÓN DE TABLAS ===
        db.execSQL(
            """
            CREATE TABLE departamentos (
              departamento_id INTEGER PRIMARY KEY AUTOINCREMENT,
              clave TEXT NOT NULL UNIQUE,
              nombre TEXT NOT NULL
            )
        """
        )

        db.execSQL(
            """
            CREATE TABLE provincias (
              provincia_id INTEGER PRIMARY KEY AUTOINCREMENT,
              clave TEXT NOT NULL UNIQUE,
              nombre TEXT NOT NULL,
              departamento_id INTEGER NOT NULL,
              FOREIGN KEY (departamento_id) REFERENCES departamentos(departamento_id)
            )
        """
        )

        db.execSQL(
            """
            CREATE TABLE distritos (
              distrito_id INTEGER PRIMARY KEY AUTOINCREMENT,
              clave TEXT NOT NULL UNIQUE,
              nombre TEXT NOT NULL,
              provincia_id INTEGER NOT NULL,
              FOREIGN KEY (provincia_id) REFERENCES provincias(provincia_id)
            )
        """
        )

        db.execSQL(
            """
            CREATE TABLE autenticacion (
              auth_id INTEGER PRIMARY KEY AUTOINCREMENT,
              nombre TEXT NOT NULL,
              usuario TEXT NOT NULL UNIQUE,
              pasword TEXT NOT NULL,
              activo INTEGER NOT NULL DEFAULT 1
            )
        """
        )

        db.execSQL(
            """
            CREATE TABLE predios (
              predio_id INTEGER PRIMARY KEY AUTOINCREMENT,
              clave TEXT NOT NULL UNIQUE,
              nombre TEXT NOT NULL,
              distrito_id INTEGER NOT NULL,
              direccion TEXT NOT NULL,
              eliminado INTEGER NOT NULL DEFAULT 0,
              notas TEXT,
              FOREIGN KEY (distrito_id) REFERENCES distritos(distrito_id)
            )
        """
        )

        db.execSQL(
            """
            CREATE TABLE unidades (
              unidad_id INTEGER PRIMARY KEY AUTOINCREMENT,
              predio_id INTEGER NOT NULL,
              clasificacion TEXT CHECK(clasificacion IN ('HAB','DEP','MDE','DUP','LCO','COC')),
              nombre TEXT NOT NULL,
              ocupantes INTEGER,
              activo INTEGER NOT NULL DEFAULT 1,
              eliminado INTEGER NOT NULL DEFAULT 0,
              notas TEXT,
              FOREIGN KEY (predio_id) REFERENCES predios(predio_id)
            )
        """
        )

        db.execSQL(
            """
            CREATE TABLE submedidores (
              submedidor_id INTEGER PRIMARY KEY AUTOINCREMENT,
              unidad_id INTEGER NOT NULL,
              identificador TEXT NOT NULL,
              activo INTEGER NOT NULL DEFAULT 1,
              eliminado INTEGER NOT NULL DEFAULT 0,
              notas TEXT,
              FOREIGN KEY (unidad_id) REFERENCES unidades(unidad_id)
            )
        """
        )

        // ACTUALIZADO: añadida columna foto_ruta
        db.execSQL(
            """
            CREATE TABLE lecturas_sub (
              lectura_id INTEGER PRIMARY KEY AUTOINCREMENT,
              submedidor_id INTEGER NOT NULL,
              valor REAL NOT NULL,
              es_inicial INTEGER NOT NULL DEFAULT 0,
              fecha TEXT NOT NULL,
              tipo_lectura TEXT NOT NULL CHECK(tipo_lectura IN ('NORMAL','INICIAL')),
              estimacion TEXT NOT NULL CHECK(estimacion IN ('EXACTO','CALCULADO')),
              foto_ruta TEXT, 
              eliminado INTEGER NOT NULL DEFAULT 0,
              notas TEXT,
              FOREIGN KEY (submedidor_id) REFERENCES submedidores(submedidor_id)
            )
        """
        )

        db.execSQL(
            """
            CREATE TABLE inquilinos (
              inquilino_id INTEGER PRIMARY KEY AUTOINCREMENT,
              nombres TEXT NOT NULL,
              apellidos TEXT NOT NULL,
              documento_tipo TEXT CHECK(documento_tipo IN ('DNI','RUC','CE','PAS')) NOT NULL,
              documento_numero TEXT NOT NULL,
              telefono TEXT,
              correo TEXT,
              activo INTEGER DEFAULT 1 NOT NULL,
              eliminado INTEGER DEFAULT 0 NOT NULL
            )
        """
        )

        db.execSQL(
            """
            CREATE TABLE contratos (
              contrato_id INTEGER PRIMARY KEY AUTOINCREMENT,
              unidad_id INTEGER NOT NULL,
              inquilino_id INTEGER NOT NULL,
              fecha_inicio TEXT NOT NULL,
              fecha_fin TEXT,
              lectura_id INTEGER,
              estado TEXT NOT NULL CHECK(estado IN ('PENDIENTE', 'ACTIVO', 'FINALIZADO', 'CANCELADO')) DEFAULT 'PENDIENTE',
              notas TEXT,
              FOREIGN KEY (unidad_id) REFERENCES unidades(unidad_id),
              FOREIGN KEY (inquilino_id) REFERENCES inquilinos(inquilino_id),
              FOREIGN KEY (lectura_id) REFERENCES lecturas_sub(lectura_id)
            )
        """
        )

        db.execSQL(
            """
            CREATE TABLE recibos_proveedor (
              recibo_prov_id INTEGER PRIMARY KEY AUTOINCREMENT,
              predio_id INTEGER NOT NULL,
              proveedor_id TEXT NOT NULL,
              periodo_inicio TEXT NOT NULL,
              periodo_fin TEXT NOT NULL,
              consumo_total REAL NOT NULL,
              monto_total REAL NOT NULL,
              eliminado INTEGER NOT NULL DEFAULT 0,
              fecha_registro TEXT NOT NULL DEFAULT (datetime('now','localtime')),
              notas TEXT,
              FOREIGN KEY (predio_id) REFERENCES predios(predio_id)
            )
        """
        )

        db.execSQL(
            """
            CREATE TABLE calculos_distribucion (
              calculo_id INTEGER PRIMARY KEY AUTOINCREMENT,
              recibo_prov_id INTEGER NOT NULL,
              estado TEXT NOT NULL DEFAULT 'pendiente' CHECK(estado IN ('pendiente','calculado','error')),
              ejecutado_en DATETIME,
              suma_total_interna REAL,
              discrepancia REAL,
              notas TEXT,
              FOREIGN KEY (recibo_prov_id) REFERENCES recibos_proveedor(recibo_prov_id)
            )
        """
        )

        db.execSQL(
            """
            CREATE TABLE calculos_medidores (
              calculo_medidor_id INTEGER PRIMARY KEY AUTOINCREMENT,
              contrato_id       INTEGER,
              fechaasig         DATETIME,
              lectura_inicio_id INTEGER,
              lectura_fin_id INTEGER,
              consumo_raw REAL,
              dias_medidos INTEGER,
              consumo_normalizado REAL,
              notas TEXT,
              FOREIGN KEY (contrato_id) REFERENCES contratos(contrato_id),
              FOREIGN KEY (lectura_inicio_id) REFERENCES lecturas_sub(lectura_id),
              FOREIGN KEY (lectura_fin_id) REFERENCES lecturas_sub(lectura_id)
            )
        """
        )

        db.execSQL(
            """
            CREATE TABLE distribucion_monetaria_unidades (
              dm_unidades_id INTEGER PRIMARY KEY AUTOINCREMENT,
              calculo_id INTEGER NOT NULL,
              calculo_medidor_id    INTEGER NOT NULL,
              consumo_total_unidad REAL,
              monto_asignado REAL,
              notas TEXT,
              FOREIGN KEY (calculo_id) REFERENCES calculos_distribucion(calculo_id),
              FOREIGN KEY (calculo_medidor_id) REFERENCES calculos_medidores(calculo_medidor_id)
            )
        """
        )

        db.execSQL(
            """
            CREATE TABLE recibos_internos (
              recibo_int_id TEXT PRIMARY KEY,
              unidad_id INTEGER NOT NULL,
              fecha_emision TEXT NOT NULL,
              fecha_vencimiento TEXT,
              monto_total REAL NOT NULL,
              pdf_ruta TEXT,
              submedidor_id INTEGER,
              descripcion TEXT,
              fecha_inicio TEXT,
              fecha_fin TEXT,
              lectura_inicio REAL,
              lectura_fin REAL,
              costoKw REAL,
              consumo REAL,
              emitido INTEGER NOT NULL DEFAULT 0,
              eliminado INTEGER NOT NULL DEFAULT 0,
              notas TEXT,
              calculo_medidor_id  INTEGER,
              FOREIGN KEY (unidad_id) REFERENCES unidades(unidad_id),
              FOREIGN KEY (submedidor_id) REFERENCES submedidores(submedidor_id),
              FOREIGN KEY (calculo_medidor_id) REFERENCES calculos_medidores(calculo_medidor_id)
            )
        """
        )


        // === NUEVOS INSERTS - DATOS DE PRUEBA COMPLETOS ===

        // DEPARTAMENTOS (Solo Lima y Callao)
        db.execSQL("INSERT INTO departamentos (clave, nombre) VALUES ('DEP-LIMA', 'LIMA')")
        db.execSQL("INSERT INTO departamentos (clave, nombre) VALUES ('DEP-CALLAO', 'CALLAO')")

        // PROVINCIAS (Lima y Callao)
        db.execSQL("INSERT INTO provincias (clave, nombre, departamento_id) VALUES ('PROV-LIMA', 'LIMA', 1)")
        db.execSQL("INSERT INTO provincias (clave, nombre, departamento_id) VALUES ('PROV-CALLAO', 'PROV. CONST. DEL CALLAO', 2)")

        // DISTRITOS (Miraflores, San Isidro, La Punta, Bellavista)
        db.execSQL("INSERT INTO distritos (clave, nombre, provincia_id) VALUES ('DIS-MIRA', 'MIRAFLORES', 1)")
        db.execSQL("INSERT INTO distritos (clave, nombre, provincia_id) VALUES ('DIS-SANI', 'SAN ISIDRO', 1)")
        db.execSQL("INSERT INTO distritos (clave, nombre, provincia_id) VALUES ('DIS-LAPU', 'LA PUNTA', 2)")
        db.execSQL("INSERT INTO distritos (clave, nombre, provincia_id) VALUES ('DIS-BELL', 'BELLAVISTA', 2)")

        // AUTENTICACIÓN (1 usuario)
        db.execSQL("INSERT INTO autenticacion (nombre, usuario, pasword, activo) VALUES ('Administrador', 'admin', '123456', 1)")

        // PREDIOS (2 predios)
        db.execSQL("INSERT INTO predios (clave, nombre, distrito_id, direccion, notas) VALUES ('P-MIRA-001', 'Edificio Miraflores Prime', 1, 'Av. Larco 123, Miraflores', 'Edificio residencial de 8 pisos')")
        db.execSQL("INSERT INTO predios (clave, nombre, distrito_id, direccion, notas) VALUES ('P-CALLAO-001', 'Condominio Callao Mar', 3, 'Av. Costanera 456, La Punta', 'Condominio frente al mar')")

        // UNIDADES (Predio 1: 4 unidades, Predio 2: 2 unidades)
        // Predio 1: Edificio Miraflores Prime
        db.execSQL("INSERT INTO unidades (predio_id, clasificacion, nombre, ocupantes, notas) VALUES (1, 'DEP', 'Departamento 101', 3, '3 dormitorios, 2 baños')")
        db.execSQL("INSERT INTO unidades (predio_id, clasificacion, nombre, ocupantes, notas) VALUES (1, 'DEP', 'Departamento 201', 2, '2 dormitorios, 1 baño')")
        db.execSQL("INSERT INTO unidades (predio_id, clasificacion, nombre, ocupantes, notas) VALUES (1, 'DEP', 'Departamento 301', 4, '4 dormitorios, 2 baños')")
        db.execSQL("INSERT INTO unidades (predio_id, clasificacion, nombre, ocupantes, notas) VALUES (1, 'LCO', 'Local Comercial A', 2, 'Local en primer piso')")
        // Predio 2: Condominio Callao Mar
        db.execSQL("INSERT INTO unidades (predio_id, clasificacion, nombre, ocupantes, notas) VALUES (2, 'DEP', 'Casa A', 5, 'Casa principal con jardín')")
        db.execSQL("INSERT INTO unidades (predio_id, clasificacion, nombre, ocupantes, notas) VALUES (2, 'DEP', 'Casa B', 3, 'Casa secundaria')")

        // SUBMEDIDORES (1 por unidad)
        db.execSQL("INSERT INTO submedidores (unidad_id, identificador, notas) VALUES (1, 'MED-MIRA-101', 'Medidor digital nuevo')")
        db.execSQL("INSERT INTO submedidores (unidad_id, identificador, notas) VALUES (2, 'MED-MIRA-201', 'Medidor analógico')")
        db.execSQL("INSERT INTO submedidores (unidad_id, identificador, notas) VALUES (3, 'MED-MIRA-301', 'Medidor digital')")
        db.execSQL("INSERT INTO submedidores (unidad_id, identificador, notas) VALUES (4, 'MED-MIRA-LCA', 'Medidor comercial trifásico')")
        db.execSQL("INSERT INTO submedidores (unidad_id, identificador, notas) VALUES (5, 'MED-CALLAO-CA', 'Medidor casa principal')")
        db.execSQL("INSERT INTO submedidores (unidad_id, identificador, notas) VALUES (6, 'MED-CALLAO-CB', 'Medidor casa secundaria')")

        // INQUILINOS (6 nuevos, IDs 1–6)
        db.execSQL("INSERT INTO inquilinos (nombres, apellidos, documento_tipo, documento_numero, telefono, correo, activo) VALUES ('Lucía Andrea', 'Martínez Rojas', 'DNI', '74125896', '987654321', 'lucia.martinez@email.com', 1)")
        db.execSQL("INSERT INTO inquilinos (nombres, apellidos, documento_tipo, documento_numero, telefono, correo, activo) VALUES ('Fernando José', 'Díaz Campos', 'DNI', '85236974', '987654322', 'fernando.diaz@email.com', 1)")
        db.execSQL("INSERT INTO inquilinos (nombres, apellidos, documento_tipo, documento_numero, telefono, correo, activo) VALUES ('Valeria Sofía', 'Herrera Luna', 'DNI', '96325874', '987654323', 'valeria.herrera@email.com', 1)")
        db.execSQL("INSERT INTO inquilinos (nombres, apellidos, documento_tipo, documento_numero, telefono, correo, activo) VALUES ('Comercial Andina', 'SAC', 'RUC', '20987654321', '015678901', 'contacto@comercialandina.com', 1)")
        db.execSQL("INSERT INTO inquilinos (nombres, apellidos, documento_tipo, documento_numero, telefono, correo, activo) VALUES ('Miguel Ángel', 'Vargas Peña', 'DNI', '14785236', '987654324', 'miguel.vargas@email.com', 1)")
        db.execSQL("INSERT INTO inquilinos (nombres, apellidos, documento_tipo, documento_numero, telefono, correo, activo) VALUES ('Claudia Elena', 'Ramos Ortiz', 'DNI', '25874136', '987654325', 'claudia.ramos@email.com', 1)")

        // LECTURAS_SUB (30 registros: 6 iniciales + 24 finales)
        // Iniciales (IDs 1–6)
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (1, 2000.0, 1, '2025-07-01', 'INICIAL', 'EXACTO', 'Lectura inicial Dpto 101')")
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (2, 1800.0, 1, '2025-07-01', 'INICIAL', 'EXACTO', 'Lectura inicial Dpto 201')")
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (3, 2200.0, 1, '2025-07-01', 'INICIAL', 'EXACTO', 'Lectura inicial Dpto 301')")
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (4, 5000.0, 1, '2025-07-01', 'INICIAL', 'EXACTO', 'Lectura inicial Local A')")
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (5, 3000.0, 1, '2025-07-01', 'INICIAL', 'EXACTO', 'Lectura inicial Casa A')")
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (6, 2500.0, 1, '2025-07-01', 'INICIAL', 'EXACTO', 'Lectura inicial Casa B')")
        // Julio 2025 (IDs 7–12)
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (1, 2120.0, 0, '2025-07-17', 'NORMAL', 'EXACTO', 'Lectura julio Dpto 101')")
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (2, 1910.0, 0, '2025-07-16', 'NORMAL', 'EXACTO', 'Lectura julio Dpto 201')")
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (3, 2320.0, 0, '2025-07-18', 'NORMAL', 'EXACTO', 'Lectura julio Dpto 301')")
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (4, 5250.0, 0, '2025-07-17', 'NORMAL', 'EXACTO', 'Lectura julio Local A')")
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (5, 3120.0, 0, '2025-07-17', 'NORMAL', 'EXACTO', 'Lectura julio Casa A')")
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (6, 2620.0, 0, '2025-07-17', 'NORMAL', 'EXACTO', 'Lectura julio Casa B')")
        // Agosto 2025 (IDs 13–18)
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (1, 2240.0, 0, '2025-08-17', 'NORMAL', 'EXACTO', 'Lectura agosto Dpto 101')")
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (2, 2020.0, 0, '2025-08-17', 'NORMAL', 'EXACTO', 'Lectura agosto Dpto 201')")
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (3, 2440.0, 0, '2025-08-16', 'NORMAL', 'EXACTO', 'Lectura agosto Dpto 301')")
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (4, 5500.0, 0, '2025-08-18', 'NORMAL', 'EXACTO', 'Lectura agosto Local A')")
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (5, 3240.0, 0, '2025-08-17', 'NORMAL', 'EXACTO', 'Lectura agosto Casa A')")
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (6, 2740.0, 0, '2025-08-17', 'NORMAL', 'EXACTO', 'Lectura agosto Casa B')")
        // Septiembre 2025 (IDs 19–24)
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (1, 2360.0, 0, '2025-09-17', 'NORMAL', 'EXACTO', 'Lectura septiembre Dpto 101')")
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (2, 2130.0, 0, '2025-09-18', 'NORMAL', 'EXACTO', 'Lectura septiembre Dpto 201')")
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (3, 2560.0, 0, '2025-09-17', 'NORMAL', 'EXACTO', 'Lectura septiembre Dpto 301')")
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (4, 5750.0, 0, '2025-09-16', 'NORMAL', 'EXACTO', 'Lectura septiembre Local A')")
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (5, 3360.0, 0, '2025-09-17', 'NORMAL', 'EXACTO', 'Lectura septiembre Casa A')")
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (6, 2860.0, 0, '2025-09-17', 'NORMAL', 'EXACTO', 'Lectura septiembre Casa B')")
        // Octubre 2025 (IDs 25–30) – solo lecturas, sin cálculos
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (1, 2480.0, 0, '2025-10-17', 'NORMAL', 'EXACTO', 'Lectura octubre Dpto 101')")
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (2, 2240.0, 0, '2025-10-17', 'NORMAL', 'EXACTO', 'Lectura octubre Dpto 201')")
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (3, 2680.0, 0, '2025-10-16', 'NORMAL', 'EXACTO', 'Lectura octubre Dpto 301')")
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (4, 6000.0, 0, '2025-10-18', 'NORMAL', 'EXACTO', 'Lectura octubre Local A')")
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (5, 3480.0, 0, '2025-10-17', 'NORMAL', 'EXACTO', 'Lectura octubre Casa A')")
        db.execSQL("INSERT INTO lecturas_sub (submedidor_id, valor, es_inicial, fecha, tipo_lectura, estimacion, notas) VALUES (6, 2980.0, 0, '2025-10-17', 'NORMAL', 'EXACTO', 'Lectura octubre Casa B')")

        // CONTRATOS (6 nuevos, IDs 1–6, lectura_id = ID de lectura inicial)
        db.execSQL("INSERT INTO contratos (unidad_id, inquilino_id, fecha_inicio, fecha_fin, lectura_id, estado, notas) VALUES (1, 1, '2025-07-01', '2026-06-30', 1, 'ACTIVO', 'Contrato jul 2025 - Dpto 101')")
        db.execSQL("INSERT INTO contratos (unidad_id, inquilino_id, fecha_inicio, fecha_fin, lectura_id, estado, notas) VALUES (2, 2, '2025-07-01', '2026-06-30', 2, 'ACTIVO', 'Contrato jul 2025 - Dpto 201')")
        db.execSQL("INSERT INTO contratos (unidad_id, inquilino_id, fecha_inicio, fecha_fin, lectura_id, estado, notas) VALUES (3, 3, '2025-07-01', '2026-06-30', 3, 'ACTIVO', 'Contrato jul 2025 - Dpto 301')")
        db.execSQL("INSERT INTO contratos (unidad_id, inquilino_id, fecha_inicio, fecha_fin, lectura_id, estado, notas) VALUES (4, 4, '2025-07-01', '2026-06-30', 4, 'ACTIVO', 'Contrato jul 2025 - Local A')")
        db.execSQL("INSERT INTO contratos (unidad_id, inquilino_id, fecha_inicio, fecha_fin, lectura_id, estado, notas) VALUES (5, 5, '2025-07-01', '2026-06-30', 5, 'ACTIVO', 'Contrato jul 2025 - Casa A')")
        db.execSQL("INSERT INTO contratos (unidad_id, inquilino_id, fecha_inicio, fecha_fin, lectura_id, estado, notas) VALUES (6, 6, '2025-07-01', '2026-06-30', 6, 'ACTIVO', 'Contrato jul 2025 - Casa B')")

        // RECIBOS_PROVEEDOR (8 registros: jul, ago, sep, oct × 2 predios)
        // Predio 1 (Miraflores) - consumo_total ajustado a 600 + discrepancia
        db.execSQL("INSERT INTO recibos_proveedor (predio_id, proveedor_id, periodo_inicio, periodo_fin, consumo_total, monto_total, fecha_registro, notas) VALUES (1, 'LUZ_DEL_SUR', '2025-07-01', '2025-07-31', 630.0, 504.0, '2025-07-31', 'Recibo julio 2025')")
        db.execSQL("INSERT INTO recibos_proveedor (predio_id, proveedor_id, periodo_inicio, periodo_fin, consumo_total, monto_total, fecha_registro, notas) VALUES (1, 'LUZ_DEL_SUR', '2025-08-01', '2025-08-31', 635.0, 508.0, '2025-08-31', 'Recibo agosto 2025')")
        db.execSQL("INSERT INTO recibos_proveedor (predio_id, proveedor_id, periodo_inicio, periodo_fin, consumo_total, monto_total, fecha_registro, notas) VALUES (1, 'LUZ_DEL_SUR', '2025-09-01', '2025-09-30', 640.0, 512.0, '2025-09-30', 'Recibo septiembre 2025')")
        db.execSQL("INSERT INTO recibos_proveedor (predio_id, proveedor_id, periodo_inicio, periodo_fin, consumo_total, monto_total, fecha_registro, notas) VALUES (1, 'LUZ_DEL_SUR', '2025-10-01', '2025-10-31', 645.0, 516.0, '2025-10-31', 'Recibo octubre 2025')")
        // Predio 2 (Callao) - consumo_total ajustado a 240 + discrepancia
        db.execSQL("INSERT INTO recibos_proveedor (predio_id, proveedor_id, periodo_inicio, periodo_fin, consumo_total, monto_total, fecha_registro, notas) VALUES (2, 'ENEL', '2025-07-01', '2025-07-31', 265.0, 212.0, '2025-07-31', 'Recibo julio 2025')")
        db.execSQL("INSERT INTO recibos_proveedor (predio_id, proveedor_id, periodo_inicio, periodo_fin, consumo_total, monto_total, fecha_registro, notas) VALUES (2, 'ENEL', '2025-08-01', '2025-08-31', 270.0, 216.0, '2025-08-31', 'Recibo agosto 2025')")
        db.execSQL("INSERT INTO recibos_proveedor (predio_id, proveedor_id, periodo_inicio, periodo_fin, consumo_total, monto_total, fecha_registro, notas) VALUES (2, 'ENEL', '2025-09-01', '2025-09-30', 275.0, 220.0, '2025-09-30', 'Recibo septiembre 2025')")
        db.execSQL("INSERT INTO recibos_proveedor (predio_id, proveedor_id, periodo_inicio, periodo_fin, consumo_total, monto_total, fecha_registro, notas) VALUES (2, 'ENEL', '2025-10-01', '2025-10-31', 280.0, 224.0, '2025-10-31', 'Recibo octubre 2025')")

        // CALCULOS_DISTRIBUCION (solo jul, ago, sep - octubre no tiene cálculos aún)
        // Predio 1
        db.execSQL("INSERT INTO calculos_distribucion (recibo_prov_id, estado, ejecutado_en, suma_total_interna, discrepancia, notas) VALUES (1, 'calculado', '2025-07-31 11:00:00', 600.0, 30.0, 'Distribución julio predio 1')")
        db.execSQL("INSERT INTO calculos_distribucion (recibo_prov_id, estado, ejecutado_en, suma_total_interna, discrepancia, notas) VALUES (2, 'calculado', '2025-08-31 11:00:00', 600.0, 35.0, 'Distribución agosto predio 1')")
        db.execSQL("INSERT INTO calculos_distribucion (recibo_prov_id, estado, ejecutado_en, suma_total_interna, discrepancia, notas) VALUES (3, 'calculado', '2025-09-30 11:00:00', 600.0, 40.0, 'Distribución septiembre predio 1')")
        // Predio 2
        db.execSQL("INSERT INTO calculos_distribucion (recibo_prov_id, estado, ejecutado_en, suma_total_interna, discrepancia, notas) VALUES (5, 'calculado', '2025-07-31 11:05:00', 240.0, 25.0, 'Distribución julio predio 2')")
        db.execSQL("INSERT INTO calculos_distribucion (recibo_prov_id, estado, ejecutado_en, suma_total_interna, discrepancia, notas) VALUES (6, 'calculado', '2025-08-31 11:05:00', 240.0, 30.0, 'Distribución agosto predio 2')")
        db.execSQL("INSERT INTO calculos_distribucion (recibo_prov_id, estado, ejecutado_en, suma_total_interna, discrepancia, notas) VALUES (7, 'calculado', '2025-09-30 11:05:00', 240.0, 35.0, 'Distribución septiembre predio 2')")

        // CALCULOS_MEDIDORES (18 registros: jul, ago, sep × 6 unidades)
        // Julio
        db.execSQL("INSERT INTO calculos_medidores (contrato_id, fechaasig, lectura_inicio_id, lectura_fin_id, consumo_raw, dias_medidos, consumo_normalizado, notas) VALUES (1, '2025-07-31 10:00:00', 1, 7, 120.0, 17, 120.0, 'Julio Dpto 101')")
        db.execSQL("INSERT INTO calculos_medidores (contrato_id, fechaasig, lectura_inicio_id, lectura_fin_id, consumo_raw, dias_medidos, consumo_normalizado, notas) VALUES (2, '2025-07-31 10:05:00', 2, 8, 110.0, 16, 110.0, 'Julio Dpto 201')")
        db.execSQL("INSERT INTO calculos_medidores (contrato_id, fechaasig, lectura_inicio_id, lectura_fin_id, consumo_raw, dias_medidos, consumo_normalizado, notas) VALUES (3, '2025-07-31 10:10:00', 3, 9, 120.0, 18, 120.0, 'Julio Dpto 301')")
        db.execSQL("INSERT INTO calculos_medidores (contrato_id, fechaasig, lectura_inicio_id, lectura_fin_id, consumo_raw, dias_medidos, consumo_normalizado, notas) VALUES (4, '2025-07-31 10:15:00', 4, 10, 250.0, 17, 250.0, 'Julio Local A')")
        db.execSQL("INSERT INTO calculos_medidores (contrato_id, fechaasig, lectura_inicio_id, lectura_fin_id, consumo_raw, dias_medidos, consumo_normalizado, notas) VALUES (5, '2025-07-31 10:20:00', 5, 11, 120.0, 17, 120.0, 'Julio Casa A')")
        db.execSQL("INSERT INTO calculos_medidores (contrato_id, fechaasig, lectura_inicio_id, lectura_fin_id, consumo_raw, dias_medidos, consumo_normalizado, notas) VALUES (6, '2025-07-31 10:25:00', 6, 12, 120.0, 17, 120.0, 'Julio Casa B')")
        // Agosto
        db.execSQL("INSERT INTO calculos_medidores (contrato_id, fechaasig, lectura_inicio_id, lectura_fin_id, consumo_raw, dias_medidos, consumo_normalizado, notas) VALUES (1, '2025-08-31 10:00:00', 7, 13, 120.0, 31, 120.0, 'Agosto Dpto 101')")
        db.execSQL("INSERT INTO calculos_medidores (contrato_id, fechaasig, lectura_inicio_id, lectura_fin_id, consumo_raw, dias_medidos, consumo_normalizado, notas) VALUES (2, '2025-08-31 10:05:00', 8, 14, 110.0, 31, 110.0, 'Agosto Dpto 201')")
        db.execSQL("INSERT INTO calculos_medidores (contrato_id, fechaasig, lectura_inicio_id, lectura_fin_id, consumo_raw, dias_medidos, consumo_normalizado, notas) VALUES (3, '2025-08-31 10:10:00', 9, 15, 120.0, 30, 120.0, 'Agosto Dpto 301')")
        db.execSQL("INSERT INTO calculos_medidores (contrato_id, fechaasig, lectura_inicio_id, lectura_fin_id, consumo_raw, dias_medidos, consumo_normalizado, notas) VALUES (4, '2025-08-31 10:15:00', 10, 16, 250.0, 32, 250.0, 'Agosto Local A')")
        db.execSQL("INSERT INTO calculos_medidores (contrato_id, fechaasig, lectura_inicio_id, lectura_fin_id, consumo_raw, dias_medidos, consumo_normalizado, notas) VALUES (5, '2025-08-31 10:20:00', 11, 17, 120.0, 31, 120.0, 'Agosto Casa A')")
        db.execSQL("INSERT INTO calculos_medidores (contrato_id, fechaasig, lectura_inicio_id, lectura_fin_id, consumo_raw, dias_medidos, consumo_normalizado, notas) VALUES (6, '2025-08-31 10:25:00', 12, 18, 120.0, 31, 120.0, 'Agosto Casa B')")
        // Septiembre
        db.execSQL("INSERT INTO calculos_medidores (contrato_id, fechaasig, lectura_inicio_id, lectura_fin_id, consumo_raw, dias_medidos, consumo_normalizado, notas) VALUES (1, '2025-09-30 10:00:00', 13, 19, 120.0, 30, 120.0, 'Septiembre Dpto 101')")
        db.execSQL("INSERT INTO calculos_medidores (contrato_id, fechaasig, lectura_inicio_id, lectura_fin_id, consumo_raw, dias_medidos, consumo_normalizado, notas) VALUES (2, '2025-09-30 10:05:00', 14, 20, 110.0, 31, 110.0, 'Septiembre Dpto 201')")
        db.execSQL("INSERT INTO calculos_medidores (contrato_id, fechaasig, lectura_inicio_id, lectura_fin_id, consumo_raw, dias_medidos, consumo_normalizado, notas) VALUES (3, '2025-09-30 10:10:00', 15, 21, 120.0, 30, 120.0, 'Septiembre Dpto 301')")
        db.execSQL("INSERT INTO calculos_medidores (contrato_id, fechaasig, lectura_inicio_id, lectura_fin_id, consumo_raw, dias_medidos, consumo_normalizado, notas) VALUES (4, '2025-09-30 10:15:00', 16, 22, 250.0, 29, 250.0, 'Septiembre Local A')")
        db.execSQL("INSERT INTO calculos_medidores (contrato_id, fechaasig, lectura_inicio_id, lectura_fin_id, consumo_raw, dias_medidos, consumo_normalizado, notas) VALUES (5, '2025-09-30 10:20:00', 17, 23, 120.0, 30, 120.0, 'Septiembre Casa A')")
        db.execSQL("INSERT INTO calculos_medidores (contrato_id, fechaasig, lectura_inicio_id, lectura_fin_id, consumo_raw, dias_medidos, consumo_normalizado, notas) VALUES (6, '2025-09-30 10:25:00', 18, 24, 120.0, 30, 120.0, 'Septiembre Casa B')")

        // DISTRIBUCION_MONETARIA_UNIDADES (18 registros)
        // Julio - Predio 1 (calculo_id = 1)
        db.execSQL("INSERT INTO distribucion_monetaria_unidades (calculo_id, calculo_medidor_id, consumo_total_unidad, monto_asignado, notas) VALUES (1, 1, 120.0, 46.67, 'Dpto 101 julio')")
        db.execSQL("INSERT INTO distribucion_monetaria_unidades (calculo_id, calculo_medidor_id, consumo_total_unidad, monto_asignado, notas) VALUES (1, 2, 110.0, 42.78, 'Dpto 201 julio')")
        db.execSQL("INSERT INTO distribucion_monetaria_unidades (calculo_id, calculo_medidor_id, consumo_total_unidad, monto_asignado, notas) VALUES (1, 3, 120.0, 46.67, 'Dpto 301 julio')")
        db.execSQL("INSERT INTO distribucion_monetaria_unidades (calculo_id, calculo_medidor_id, consumo_total_unidad, monto_asignado, notas) VALUES (1, 4, 250.0, 97.22, 'Local A julio')")
        // Julio - Predio 2 (calculo_id = 4)
        db.execSQL("INSERT INTO distribucion_monetaria_unidades (calculo_id, calculo_medidor_id, consumo_total_unidad, monto_asignado, notas) VALUES (4, 5, 120.0, 85.71, 'Casa A julio')")
        db.execSQL("INSERT INTO distribucion_monetaria_unidades (calculo_id, calculo_medidor_id, consumo_total_unidad, monto_asignado, notas) VALUES (4, 6, 120.0, 85.71, 'Casa B julio')")
        // Agosto - Predio 1 (calculo_id = 2)
        db.execSQL("INSERT INTO distribucion_monetaria_unidades (calculo_id, calculo_medidor_id, consumo_total_unidad, monto_asignado, notas) VALUES (2, 7, 120.0, 46.67, 'Dpto 101 agosto')")
        db.execSQL("INSERT INTO distribucion_monetaria_unidades (calculo_id, calculo_medidor_id, consumo_total_unidad, monto_asignado, notas) VALUES (2, 8, 110.0, 42.78, 'Dpto 201 agosto')")
        db.execSQL("INSERT INTO distribucion_monetaria_unidades (calculo_id, calculo_medidor_id, consumo_total_unidad, monto_asignado, notas) VALUES (2, 9, 120.0, 46.67, 'Dpto 301 agosto')")
        db.execSQL("INSERT INTO distribucion_monetaria_unidades (calculo_id, calculo_medidor_id, consumo_total_unidad, monto_asignado, notas) VALUES (2, 10, 250.0, 97.22, 'Local A agosto')")
        // Agosto - Predio 2 (calculo_id = 5)
        db.execSQL("INSERT INTO distribucion_monetaria_unidades (calculo_id, calculo_medidor_id, consumo_total_unidad, monto_asignado, notas) VALUES (5, 11, 120.0, 85.71, 'Casa A agosto')")
        db.execSQL("INSERT INTO distribucion_monetaria_unidades (calculo_id, calculo_medidor_id, consumo_total_unidad, monto_asignado, notas) VALUES (5, 12, 120.0, 85.71, 'Casa B agosto')")
        // Septiembre - Predio 1 (calculo_id = 3)
        db.execSQL("INSERT INTO distribucion_monetaria_unidades (calculo_id, calculo_medidor_id, consumo_total_unidad, monto_asignado, notas) VALUES (3, 13, 120.0, 46.67, 'Dpto 101 septiembre')")
        db.execSQL("INSERT INTO distribucion_monetaria_unidades (calculo_id, calculo_medidor_id, consumo_total_unidad, monto_asignado, notas) VALUES (3, 14, 110.0, 42.78, 'Dpto 201 septiembre')")
        db.execSQL("INSERT INTO distribucion_monetaria_unidades (calculo_id, calculo_medidor_id, consumo_total_unidad, monto_asignado, notas) VALUES (3, 15, 120.0, 46.67, 'Dpto 301 septiembre')")
        db.execSQL("INSERT INTO distribucion_monetaria_unidades (calculo_id, calculo_medidor_id, consumo_total_unidad, monto_asignado, notas) VALUES (3, 16, 250.0, 97.22, 'Local A septiembre')")
        // Septiembre - Predio 2 (calculo_id = 6)
        db.execSQL("INSERT INTO distribucion_monetaria_unidades (calculo_id, calculo_medidor_id, consumo_total_unidad, monto_asignado, notas) VALUES (6, 17, 120.0, 85.71, 'Casa A septiembre')")
        db.execSQL("INSERT INTO distribucion_monetaria_unidades (calculo_id, calculo_medidor_id, consumo_total_unidad, monto_asignado, notas) VALUES (6, 18, 120.0, 85.71, 'Casa B septiembre')")

        // RECIBOS INTERNOS (18 registros: jul, ago, sep × 6 unidades)
        // Julio
        db.execSQL("INSERT INTO recibos_internos (recibo_int_id, unidad_id, fecha_emision, fecha_vencimiento, monto_total, pdf_ruta, submedidor_id, descripcion, fecha_inicio, fecha_fin, lectura_inicio, lectura_fin, costoKw, consumo, emitido, calculo_medidor_id, notas) VALUES ('R-2025-07-001', 1, '2025-07-30', '2025-08-15', 46.67, '/pdf/recibos/R-2025-07-001.pdf', 1, 'Recibo julio 2025', '2025-07-01', '2025-07-17', 2000.0, 2120.0, 0.389, 120.0, 1, 1, 'Dpto 101 julio')")
        db.execSQL("INSERT INTO recibos_internos (recibo_int_id, unidad_id, fecha_emision, fecha_vencimiento, monto_total, pdf_ruta, submedidor_id, descripcion, fecha_inicio, fecha_fin, lectura_inicio, lectura_fin, costoKw, consumo, emitido, calculo_medidor_id, notas) VALUES ('R-2025-07-002', 2, '2025-07-30', '2025-08-15', 42.78, '/pdf/recibos/R-2025-07-002.pdf', 2, 'Recibo julio 2025', '2025-07-01', '2025-07-16', 1800.0, 1910.0, 0.389, 110.0, 1, 2, 'Dpto 201 julio')")
        db.execSQL("INSERT INTO recibos_internos (recibo_int_id, unidad_id, fecha_emision, fecha_vencimiento, monto_total, pdf_ruta, submedidor_id, descripcion, fecha_inicio, fecha_fin, lectura_inicio, lectura_fin, costoKw, consumo, emitido, calculo_medidor_id, notas) VALUES ('R-2025-07-003', 3, '2025-07-30', '2025-08-15', 46.67, '/pdf/recibos/R-2025-07-003.pdf', 3, 'Recibo julio 2025', '2025-07-01', '2025-07-18', 2200.0, 2320.0, 0.389, 120.0, 1, 3, 'Dpto 301 julio')")
        db.execSQL("INSERT INTO recibos_internos (recibo_int_id, unidad_id, fecha_emision, fecha_vencimiento, monto_total, pdf_ruta, submedidor_id, descripcion, fecha_inicio, fecha_fin, lectura_inicio, lectura_fin, costoKw, consumo, emitido, calculo_medidor_id, notas) VALUES ('R-2025-07-004', 4, '2025-07-30', '2025-08-15', 97.22, '/pdf/recibos/R-2025-07-004.pdf', 4, 'Recibo julio 2025', '2025-07-01', '2025-07-17', 5000.0, 5250.0, 0.389, 250.0, 1, 4, 'Local A julio')")
        db.execSQL("INSERT INTO recibos_internos (recibo_int_id, unidad_id, fecha_emision, fecha_vencimiento, monto_total, pdf_ruta, submedidor_id, descripcion, fecha_inicio, fecha_fin, lectura_inicio, lectura_fin, costoKw, consumo, emitido, calculo_medidor_id, notas) VALUES ('R-2025-07-005', 5, '2025-07-30', '2025-08-15', 85.71, '/pdf/recibos/R-2025-07-005.pdf', 5, 'Recibo julio 2025', '2025-07-01', '2025-07-17', 3000.0, 3120.0, 0.714, 120.0, 1, 5, 'Casa A julio')")
        db.execSQL("INSERT INTO recibos_internos (recibo_int_id, unidad_id, fecha_emision, fecha_vencimiento, monto_total, pdf_ruta, submedidor_id, descripcion, fecha_inicio, fecha_fin, lectura_inicio, lectura_fin, costoKw, consumo, emitido, calculo_medidor_id, notas) VALUES ('R-2025-07-006', 6, '2025-07-30', '2025-08-15', 85.71, '/pdf/recibos/R-2025-07-006.pdf', 6, 'Recibo julio 2025', '2025-07-01', '2025-07-17', 2500.0, 2620.0, 0.714, 120.0, 1, 6, 'Casa B julio')")
        // Agosto
        db.execSQL("INSERT INTO recibos_internos (recibo_int_id, unidad_id, fecha_emision, fecha_vencimiento, monto_total, pdf_ruta, submedidor_id, descripcion, fecha_inicio, fecha_fin, lectura_inicio, lectura_fin, costoKw, consumo, emitido, calculo_medidor_id, notas) VALUES ('R-2025-08-001', 1, '2025-08-30', '2025-09-15', 46.67, '/pdf/recibos/R-2025-08-001.pdf', 1, 'Recibo agosto 2025', '2025-07-17', '2025-08-17', 2120.0, 2240.0, 0.389, 120.0, 1, 7, 'Dpto 101 agosto')")
        db.execSQL("INSERT INTO recibos_internos (recibo_int_id, unidad_id, fecha_emision, fecha_vencimiento, monto_total, pdf_ruta, submedidor_id, descripcion, fecha_inicio, fecha_fin, lectura_inicio, lectura_fin, costoKw, consumo, emitido, calculo_medidor_id, notas) VALUES ('R-2025-08-002', 2, '2025-08-30', '2025-09-15', 42.78, '/pdf/recibos/R-2025-08-002.pdf', 2, 'Recibo agosto 2025', '2025-07-16', '2025-08-17', 1910.0, 2020.0, 0.389, 110.0, 1, 8, 'Dpto 201 agosto')")
        db.execSQL("INSERT INTO recibos_internos (recibo_int_id, unidad_id, fecha_emision, fecha_vencimiento, monto_total, pdf_ruta, submedidor_id, descripcion, fecha_inicio, fecha_fin, lectura_inicio, lectura_fin, costoKw, consumo, emitido, calculo_medidor_id, notas) VALUES ('R-2025-08-003', 3, '2025-08-30', '2025-09-15', 46.67, '/pdf/recibos/R-2025-08-003.pdf', 3, 'Recibo agosto 2025', '2025-07-18', '2025-08-16', 2320.0, 2440.0, 0.389, 120.0, 1, 9, 'Dpto 301 agosto')")
        db.execSQL("INSERT INTO recibos_internos (recibo_int_id, unidad_id, fecha_emision, fecha_vencimiento, monto_total, pdf_ruta, submedidor_id, descripcion, fecha_inicio, fecha_fin, lectura_inicio, lectura_fin, costoKw, consumo, emitido, calculo_medidor_id, notas) VALUES ('R-2025-08-004', 4, '2025-08-30', '2025-09-15', 97.22, '/pdf/recibos/R-2025-08-004.pdf', 4, 'Recibo agosto 2025', '2025-07-17', '2025-08-18', 5250.0, 5500.0, 0.389, 250.0, 1, 10, 'Local A agosto')")
        db.execSQL("INSERT INTO recibos_internos (recibo_int_id, unidad_id, fecha_emision, fecha_vencimiento, monto_total, pdf_ruta, submedidor_id, descripcion, fecha_inicio, fecha_fin, lectura_inicio, lectura_fin, costoKw, consumo, emitido, calculo_medidor_id, notas) VALUES ('R-2025-08-005', 5, '2025-08-30', '2025-09-15', 85.71, '/pdf/recibos/R-2025-08-005.pdf', 5, 'Recibo agosto 2025', '2025-07-17', '2025-08-17', 3120.0, 3240.0, 0.714, 120.0, 1, 11, 'Casa A agosto')")
        db.execSQL("INSERT INTO recibos_internos (recibo_int_id, unidad_id, fecha_emision, fecha_vencimiento, monto_total, pdf_ruta, submedidor_id, descripcion, fecha_inicio, fecha_fin, lectura_inicio, lectura_fin, costoKw, consumo, emitido, calculo_medidor_id, notas) VALUES ('R-2025-08-006', 6, '2025-08-30', '2025-09-15', 85.71, '/pdf/recibos/R-2025-08-006.pdf', 6, 'Recibo agosto 2025', '2025-07-17', '2025-08-17', 2620.0, 2740.0, 0.714, 120.0, 1, 12, 'Casa B agosto')")
        // Septiembre
        db.execSQL("INSERT INTO recibos_internos (recibo_int_id, unidad_id, fecha_emision, fecha_vencimiento, monto_total, pdf_ruta, submedidor_id, descripcion, fecha_inicio, fecha_fin, lectura_inicio, lectura_fin, costoKw, consumo, emitido, calculo_medidor_id, notas) VALUES ('R-2025-09-001', 1, '2025-09-30', '2025-10-15', 46.67, '/pdf/recibos/R-2025-09-001.pdf', 1, 'Recibo septiembre 2025', '2025-08-17', '2025-09-17', 2240.0, 2360.0, 0.389, 120.0, 1, 13, 'Dpto 101 septiembre')")
        db.execSQL("INSERT INTO recibos_internos (recibo_int_id, unidad_id, fecha_emision, fecha_vencimiento, monto_total, pdf_ruta, submedidor_id, descripcion, fecha_inicio, fecha_fin, lectura_inicio, lectura_fin, costoKw, consumo, emitido, calculo_medidor_id, notas) VALUES ('R-2025-09-002', 2, '2025-09-30', '2025-10-15', 42.78, '/pdf/recibos/R-2025-09-002.pdf', 2, 'Recibo septiembre 2025', '2025-08-17', '2025-09-18', 2020.0, 2130.0, 0.389, 110.0, 1, 14, 'Dpto 201 septiembre')")
        db.execSQL("INSERT INTO recibos_internos (recibo_int_id, unidad_id, fecha_emision, fecha_vencimiento, monto_total, pdf_ruta, submedidor_id, descripcion, fecha_inicio, fecha_fin, lectura_inicio, lectura_fin, costoKw, consumo, emitido, calculo_medidor_id, notas) VALUES ('R-2025-09-003', 3, '2025-09-30', '2025-10-15', 46.67, '/pdf/recibos/R-2025-09-003.pdf', 3, 'Recibo septiembre 2025', '2025-08-16', '2025-09-17', 2440.0, 2560.0, 0.389, 120.0, 1, 15, 'Dpto 301 septiembre')")
        db.execSQL("INSERT INTO recibos_internos (recibo_int_id, unidad_id, fecha_emision, fecha_vencimiento, monto_total, pdf_ruta, submedidor_id, descripcion, fecha_inicio, fecha_fin, lectura_inicio, lectura_fin, costoKw, consumo, emitido, calculo_medidor_id, notas) VALUES ('R-2025-09-004', 4, '2025-09-30', '2025-10-15', 97.22, '/pdf/recibos/R-2025-09-004.pdf', 4, 'Recibo septiembre 2025', '2025-08-18', '2025-09-16', 5500.0, 5750.0, 0.389, 250.0, 1, 16, 'Local A septiembre')")
        db.execSQL("INSERT INTO recibos_internos (recibo_int_id, unidad_id, fecha_emision, fecha_vencimiento, monto_total, pdf_ruta, submedidor_id, descripcion, fecha_inicio, fecha_fin, lectura_inicio, lectura_fin, costoKw, consumo, emitido, calculo_medidor_id, notas) VALUES ('R-2025-09-005', 5, '2025-09-30', '2025-10-15', 85.71, '/pdf/recibos/R-2025-09-005.pdf', 5, 'Recibo septiembre 2025', '2025-08-17', '2025-09-17', 3240.0, 3360.0, 0.714, 120.0, 1, 17, 'Casa A septiembre')")
        db.execSQL("INSERT INTO recibos_internos (recibo_int_id, unidad_id, fecha_emision, fecha_vencimiento, monto_total, pdf_ruta, submedidor_id, descripcion, fecha_inicio, fecha_fin, lectura_inicio, lectura_fin, costoKw, consumo, emitido, calculo_medidor_id, notas) VALUES ('R-2025-09-006', 6, '2025-09-30', '2025-10-15', 85.71, '/pdf/recibos/R-2025-09-006.pdf', 6, 'Recibo septiembre 2025', '2025-08-17', '2025-09-17', 2740.0, 2860.0, 0.714, 120.0, 1, 18, 'Casa B septiembre')")
    }

    //  Forzamos recreación al cambiar versión
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS recibos_internos")
        db.execSQL("DROP TABLE IF EXISTS distribucion_monetaria_unidades")
        db.execSQL("DROP TABLE IF EXISTS calculos_medidores")
        db.execSQL("DROP TABLE IF EXISTS calculos_distribucion")
        db.execSQL("DROP TABLE IF EXISTS recibos_proveedor")
        db.execSQL("DROP TABLE IF EXISTS contratos")
        db.execSQL("DROP TABLE IF EXISTS inquilinos")
        db.execSQL("DROP TABLE IF EXISTS lecturas_sub")
        db.execSQL("DROP TABLE IF EXISTS submedidores")
        db.execSQL("DROP TABLE IF EXISTS unidades")
        db.execSQL("DROP TABLE IF EXISTS predios")
        db.execSQL("DROP TABLE IF EXISTS autenticacion")
        db.execSQL("DROP TABLE IF EXISTS distritos")
        db.execSQL("DROP TABLE IF EXISTS provincias")
        db.execSQL("DROP TABLE IF EXISTS departamentos")
        onCreate(db)
    }

    // === MÉTODOS ÚTILES ===
    fun registrarLecturaConFoto(
        submedidorId: Int,
        valor: Double,
        fecha: String,
        notas: String?,
        tipoLectura: String,
        fotoRuta: String?
    ) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("submedidor_id", submedidorId)
            put("valor", valor)
            put("es_inicial", if (tipoLectura == "INICIAL") 1 else 0)
            put("fecha", fecha)
            put("tipo_lectura", tipoLectura)
            put("estimacion", "EXACTO")
            put("notas", notas)
            put("foto_ruta", fotoRuta)
        }
        db.insert("lecturas_sub", null, values)
        db.close()
    }
    fun obtenerLecturasPorUnidad(unidadId: Int): List<Map<String, Any?>> {
        val lista = mutableListOf<Map<String, Any?>>()
        val db = this.readableDatabase

        val query = """
        SELECT 
        l.lectura_id,
        l.fecha,
        l.valor,
        l.notas,
        l.tipo_lectura,
        l.foto_ruta,
        (
            SELECT CASE 
                WHEN COUNT(*) > 0 THEN 1 ELSE 0 
            END
            FROM recibos_internos ri
            WHERE ri.submedidor_id = s.submedidor_id
            AND l.fecha BETWEEN ri.fecha_inicio AND ri.fecha_fin
        ) AS facturado
    FROM lecturas_sub l
    INNER JOIN submedidores s ON l.submedidor_id = s.submedidor_id
    WHERE s.unidad_id = ?
    GROUP BY l.lectura_id
    ORDER BY l.fecha DESC
    """

        val cursor = db.rawQuery(query, arrayOf(unidadId.toString()))
        while (cursor.moveToNext()) {
            val item = mapOf(
                "id" to cursor.getInt(0),
                "fecha" to cursor.getString(1),
                "valor" to cursor.getDouble(2),
                "notas" to cursor.getString(3),
                "tipo_lectura" to cursor.getString(4),
                "fotoRuta" to cursor.getString(5),
                "facturado" to cursor.getInt(6)
            )
            lista.add(item)
        }

        cursor.close()
        db.close()
        return lista
    }

    fun obtenerPredios(): List<Pair<Int, String>> {
        val lista = mutableListOf<Pair<Int, String>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT predio_id, nombre FROM predios WHERE eliminado = 0",
            null
        )
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val nombre = cursor.getString(1)
                lista.add(id to nombre)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }
    fun obtenerNombrePorCorreo(correo: String): String? {
        val db = this.readableDatabase
        var nombre: String? = null

        val cursor = db.rawQuery(
            "SELECT nombre FROM autenticacion WHERE usuario = ?",
            arrayOf(correo)
        )

        if (cursor.moveToFirst()) {
            nombre = cursor.getString(0)
        }

        cursor.close()
        db.close()
        return nombre
    }

    //  Procedimientos de José
    /* ==========  PARA SPINNERS CASCADA  ========== */

    fun obtenerDepartamentos(): List<Pair<Int, String>> =
        readableDatabase.rawQuery("SELECT departamento_id, nombre FROM departamentos ORDER BY nombre", null)
            .use { cursor ->
                mutableListOf<Pair<Int, String>>().apply {
                    while (cursor.moveToNext()) {
                        add(cursor.getInt(0) to cursor.getString(1))
                    }
                }
            }

    fun obtenerProvincias(deptoId: Int): List<Pair<Int, String>> =
        readableDatabase.rawQuery(
            "SELECT provincia_id, nombre FROM provincias WHERE departamento_id = ? ORDER BY nombre",
            arrayOf(deptoId.toString())
        ).use { cursor ->
            mutableListOf<Pair<Int, String>>().apply {
                while (cursor.moveToNext()) {
                    add(cursor.getInt(0) to cursor.getString(1))
                }
            }
        }

    fun obtenerDistritos(provId: Int): List<Pair<Int, String>> =
        readableDatabase.rawQuery(
            "SELECT distrito_id, nombre FROM distritos WHERE provincia_id = ? ORDER BY nombre",
            arrayOf(provId.toString())
        ).use { cursor ->
            mutableListOf<Pair<Int, String>>().apply {
                while (cursor.moveToNext()) {
                    add(cursor.getInt(0) to cursor.getString(1))
                }
            }
        }
    fun puedeEditarUnidad(unidadId: Int): Boolean {
        val sql = """
        SELECT COUNT(*) 
        FROM recibos_internos ri
        JOIN submedidores s ON s.submedidor_id = ri.submedidor_id
        JOIN unidades u ON u.unidad_id = s.unidad_id
        WHERE u.unidad_id = ? LIMIT 1
    """.trimIndent()
        readableDatabase.rawQuery(sql, arrayOf(unidadId.toString())).use { cursor ->
            cursor.moveToFirst()
            return cursor.getInt(0) == 0   // 0 = no hay recibos → puede editar
        }
    }
    // Devuelve Pair<nombres, apellidos> o null

    fun eliminarUnidadCompleta(unidadId: Int) {
        writableDatabase.apply {
            // 1. Borrar lecturas de sus submedidores
            delete("lecturas_sub", "submedidor_id IN (SELECT submedidor_id FROM submedidores WHERE unidad_id = ?)", arrayOf(unidadId.toString()))
            // 2. Borrar submedidores
            delete("submedidores", "unidad_id = ?", arrayOf(unidadId.toString()))
            // 3. Finalizar contratos
            execSQL("UPDATE contratos SET estado = 'FINALIZADO' WHERE unidad_id = ?", arrayOf(unidadId.toString()))
            // 4. Borrar unidad
            delete("unidades", "unidad_id = ?", arrayOf(unidadId.toString()))
        }
    }

    fun desocuparUnidad(unidadId: Int) {
        writableDatabase.apply {
            // Finaliza contrato
            execSQL("UPDATE contratos SET estado = 'FINALIZADO' WHERE unidad_id = ?", arrayOf(unidadId.toString()))
            // La unidad queda sin inquilino activo (no borramos inquilino por historial)
        }
    }
    fun obtenerInquilinoDeUnidad(unidadId: Int): Triple<Int, String, String>? =
        readableDatabase.rawQuery(
            """SELECT i.inquilino_id, i.nombres, i.apellidos 
           FROM contratos c JOIN inquilinos i ON i.inquilino_id = c.inquilino_id
           WHERE c.unidad_id = ? AND c.estado = 'ACTIVO' LIMIT 1""",
            arrayOf(unidadId.toString())
        ).use { if (it.moveToFirst()) Triple(it.getInt(0), it.getString(1), it.getString(2)) else null }

    fun getTipoDocumentoInquilino(inqId: Int): String =
        readableDatabase.rawQuery("SELECT documento_tipo FROM inquilinos WHERE inquilino_id = ?", arrayOf(inqId.toString()))
            .use { if (it.moveToFirst()) it.getString(0) else "DNI" }

    fun getDocumentoInquilino(inqId: Int): String =
        readableDatabase.rawQuery("SELECT documento_numero FROM inquilinos WHERE inquilino_id = ?", arrayOf(inqId.toString()))
            .use { if (it.moveToFirst()) it.getString(0) else "" }

    fun getTelefonoInquilino(inqId: Int): String =
        readableDatabase.rawQuery("SELECT telefono FROM inquilinos WHERE inquilino_id = ?", arrayOf(inqId.toString()))
            .use { if (it.moveToFirst()) it.getString(0) else "" }

    fun getCorreoInquilino(inqId: Int): String =
        readableDatabase.rawQuery("SELECT correo FROM inquilinos WHERE inquilino_id = ?", arrayOf(inqId.toString()))
            .use { if (it.moveToFirst()) it.getString(0) else "" }

    //  Procedimientos de Dani
    /* ================== */



    fun insertarRecibo(
        predioId: Int,
        fechaRegistro: String,
        periodoInicio: String,
        periodoFin: String,
        consumoTotal: Double,
        total: Double
    ): Long {
        return try {
            val db = writableDatabase

            // extraer año y mes del periodo_fin
            val partes = periodoFin.split("-")
            if (partes.size < 2) {
                db.close()
                return -1L // formato inválido
            }

            val anio = partes[0]
            val mes = partes[1]
            // verificar si ya existe un recibo en ese mes/año para ese predio
            val cursor = db.rawQuery(
                """
            SELECT COUNT(*) FROM recibos_proveedor
            WHERE predio_id = ? 
              AND strftime('%Y', periodo_fin) = ? 
              AND strftime('%m', periodo_fin) = ?
              AND eliminado = 0
            """,
                arrayOf(predioId.toString(), anio, mes)
            )

            var existe = false
            if (cursor.moveToFirst()) {
                existe = cursor.getInt(0) > 0
            }
            cursor.close()

            if (existe) {
                // si existe recibo en ese mes , no insertar
                db.close()
                return -2L
            }
            // si no existe, insertar el nuevo recibo
            val values = ContentValues().apply {
                put("predio_id", predioId)
                put("proveedor_id", "Luz") // fijo por ahora
                put("fecha_registro", fechaRegistro)
                put("periodo_inicio", periodoInicio)
                put("periodo_fin", periodoFin)
                put("consumo_total", consumoTotal)
                put("monto_total", total)
            }
            val resultado = db.insert("recibos_proveedor", null, values)
            db.close()
            resultado

        } catch (e: Exception) {
            e.printStackTrace()
            -1L
        }
    }



    fun listarRecibos(): ArrayList<HashMap<String, Any>> {
        val lista = ArrayList<HashMap<String, Any>>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT recibo_prov_id, proveedor_id, periodo_inicio, periodo_fin, consumo_total, monto_total, fecha_registro FROM recibos_proveedor WHERE eliminado = 0",
            null
        )

        if (cursor.moveToFirst()) {
            do {
                val map = HashMap<String, Any>()
                map["id"] = cursor.getInt(cursor.getColumnIndexOrThrow("recibo_prov_id"))
                map["proveedor"] = cursor.getString(cursor.getColumnIndexOrThrow("proveedor_id"))
                map["periodoInicio"] = cursor.getString(cursor.getColumnIndexOrThrow("periodo_inicio"))
                map["periodoFin"] = cursor.getString(cursor.getColumnIndexOrThrow("periodo_fin"))
                map["consumo"] = cursor.getDouble(cursor.getColumnIndexOrThrow("consumo_total"))
                map["total"] = cursor.getDouble(cursor.getColumnIndexOrThrow("monto_total"))
                map["fechaRegistro"] = cursor.getString(cursor.getColumnIndexOrThrow("fecha_registro"))
                lista.add(map)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return lista
    }

    fun obtenerRecibosPorPredio(predioId: Int): List<Map<String, Any>> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
        SELECT recibo_prov_id, proveedor_id, periodo_inicio, periodo_fin, monto_total, fecha_registro
        FROM recibos_proveedor
        WHERE predio_id = ? AND eliminado = 0
        ORDER BY fecha_registro DESC
        """, arrayOf(predioId.toString())
        )

        val lista = mutableListOf<Map<String, Any>>()
        if (cursor.moveToFirst()) {
            do {
                val idRecibo = cursor.getInt(cursor.getColumnIndexOrThrow("recibo_prov_id"))
                val proveedor = cursor.getString(cursor.getColumnIndexOrThrow("proveedor_id"))
                val periodoInicio = cursor.getString(cursor.getColumnIndexOrThrow("periodo_inicio"))
                val periodoFin = cursor.getString(cursor.getColumnIndexOrThrow("periodo_fin"))
                val montoTotal = cursor.getDouble(cursor.getColumnIndexOrThrow("monto_total"))
                val fechaRegistro = cursor.getString(cursor.getColumnIndexOrThrow("fecha_registro"))

                val recibo = mapOf(
                    "id_recibo" to idRecibo,
                    "proveedor" to proveedor,
                    "periodo_inicio" to periodoInicio,
                    "periodo_fin" to periodoFin,
                    "total" to montoTotal,
                    "fecha_registro" to fechaRegistro
                )
                lista.add(recibo)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }





    fun obtenerPrediosActivos(): List<Map<String, Any>> {
        val lista = mutableListOf<Map<String, Any>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM predios WHERE eliminado = 0", null)
        if (cursor.moveToFirst()) {
            do {
                val predio = mapOf(
                    "predio_id" to cursor.getInt(cursor.getColumnIndexOrThrow("predio_id")),
                    "clave" to cursor.getString(cursor.getColumnIndexOrThrow("clave")),
                    "nombre" to cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                    "distrito_id" to cursor.getInt(cursor.getColumnIndexOrThrow("distrito_id")),
                    "direccion" to cursor.getString(cursor.getColumnIndexOrThrow("direccion")),
                    "eliminado" to cursor.getInt(cursor.getColumnIndexOrThrow("eliminado")),
                    "notas" to (cursor.getString(cursor.getColumnIndexOrThrow("notas")) ?: "")
                )
                lista.add(predio)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }


    //METODOS BRIYIT

    // === Buscar predio por ID ===
    fun BCbuscarPredioPorId(id: Int): Cursor? {
        val db = readableDatabase
        return db.rawQuery(
            """
        SELECT p.*, 
               d.distrito_id, d.nombre AS distrito_nombre,
               pr.provincia_id, pr.nombre AS provincia_nombre,
               dep.departamento_id, dep.nombre AS departamento_nombre
        FROM predios p
        INNER JOIN distritos d ON p.distrito_id = d.distrito_id
        INNER JOIN provincias pr ON d.provincia_id = pr.provincia_id
        INNER JOIN departamentos dep ON pr.departamento_id = dep.departamento_id
        WHERE p.predio_id = ? AND p.eliminado = 0
        """, arrayOf(id.toString())
        )
    }

    // === Editar predio ===
    fun BCeditarPredio(
        id: Int,
        nombre: String,
        direccion: String,
        distritoId: Int,
        notas: String?
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nombre", nombre)
            put("direccion", direccion)
            put("distrito_id", distritoId)
            put("notas", notas)
        }

        val filas = db.update("predios", values, "predio_id = ?", arrayOf(id.toString()))
        return filas > 0
    }

    // === Listar departamentos ===
    fun BClistarDepartamentos(): Cursor? {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM departamentos ORDER BY nombre ASC", null)
    }

    // === Buscar provincias por departamento ===
    fun BCbuscarProvinciasPorDepartamento(departamentoId: Int): Cursor? {
        val db = readableDatabase
        return db.rawQuery(
            "SELECT * FROM provincias WHERE departamento_id = ? ORDER BY nombre ASC",
            arrayOf(departamentoId.toString())
        )
    }

    // === Buscar distritos por provincia ===
    fun BCbuscarDistritosPorProvincia(provinciaId: Int): Cursor? {
        val db = readableDatabase
        return db.rawQuery(
            "SELECT * FROM distritos WHERE provincia_id = ? ORDER BY nombre ASC",
            arrayOf(provinciaId.toString())
        )
    }

    // === Listar recibos por unidad con paginación ===
    fun BClistarRecibosPorUnidad(unidadId: Int, limit: Int, offset: Int): Cursor? {
        val db = readableDatabase
        return db.rawQuery(
            """
            SELECT recibo_int_id, unidad_id, fecha_emision, fecha_inicio, fecha_fin, monto_total, descripcion
            FROM recibos_internos
            WHERE unidad_id = ? AND eliminado = 0
            ORDER BY fecha_inicio DESC
            LIMIT ? OFFSET ?
            """,
            arrayOf(unidadId.toString(), limit.toString(), offset.toString())
        )
    }

    // === Contar recibos por unidad ===
    fun BCcontarRecibosPorUnidad(unidadId: Int): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) AS total FROM recibos_internos WHERE unidad_id = ? AND eliminado = 0",
            arrayOf(unidadId.toString())
        )
        var total = 0
        if (cursor.moveToFirst()) {
            total = cursor.getInt(cursor.getColumnIndexOrThrow("total"))
        }
        cursor.close()
        return total
    }

    fun BCobtenerReciboPorId(reciboId: String): Cursor? {
        val db = readableDatabase
        return db.rawQuery(
            """
        SELECT 
            p.direccion, 
            i.telefono, 
            d.nombre, 
            r.recibo_int_id, 
            r.unidad_id, 
            strftime('%m', r.fecha_emision) AS mes,
            i.nombres || ' ' || i.apellidos AS nombre,
            u.nombre, 
            r.fecha_emision, 
            de.nombre AS departamento,
            s.identificador, 
            r.lectura_inicio, 
            r.lectura_fin, 
            r.fecha_inicio, 
            r.fecha_fin, 
            r.costoKw, 
            r.consumo, 
            r.monto_total
        FROM recibos_internos r
        INNER JOIN submedidores s ON r.submedidor_id = s.submedidor_id
        INNER JOIN unidades u ON r.unidad_id = u.unidad_id
        INNER JOIN contratos c ON c.unidad_id = u.unidad_id
        INNER JOIN predios p ON p.predio_id = u.predio_id
        INNER JOIN inquilinos i ON c.inquilino_id = i.inquilino_id
        INNER JOIN distritos d ON d.distrito_id = p.distrito_id
        INNER JOIN provincias pro ON pro.provincia_id = d.provincia_id
        INNER JOIN departamentos de ON de.departamento_id = pro.departamento_id
        WHERE r.recibo_int_id = ?
        """.trimIndent(),
            arrayOf(reciboId)
        )
    }

    fun BClistarUnidades(): Cursor? {
        val db = readableDatabase
        return db.rawQuery(
            """
        SELECT 
            u.unidad_id, 
            p.nombre || ' - ' || u.nombre as nombre_completo
        FROM unidades u
        INNER JOIN predios p ON u.predio_id = p.predio_id
        WHERE u.eliminado = 0 
        ORDER BY p.nombre, u.nombre ASC
        """.trimIndent(),
            null
        )
    }

    fun BClistarRecibosInternosPorPredioYMes(predioId: Int, yearMonth: String): ArrayList<HashMap<String, Any?>> {
        val lista = ArrayList<HashMap<String, Any?>>()
        val db = readableDatabase

        // Consulta que filtra por predio_id y por el mes/año del campo fecha_inicio
        val sql = """
        SELECT 
            r.recibo_int_id,
            r.unidad_id,
            u.predio_id,
            p.nombre AS predio_nombre,
            u.nombre AS unidad_nombre,
            r.fecha_emision,
            r.fecha_inicio,
            r.fecha_fin,
            r.lectura_inicio,
            r.lectura_fin,
            r.consumo,
            r.costoKw,
            r.monto_total,
            r.descripcion
        FROM recibos_internos r
        INNER JOIN unidades u ON r.unidad_id = u.unidad_id
        INNER JOIN predios p ON u.predio_id = p.predio_id
        WHERE u.predio_id = ?
          AND r.eliminado = 0
          AND strftime('%Y-%m', r.fecha_emision) = ?  
        ORDER BY r.fecha_emision DESC
    """

        // yearMonth debe tener el formato "YYYY-MM"
        val args = arrayOf(predioId.toString(), yearMonth)
        val cursor = db.rawQuery(sql, args)

        if (cursor.moveToFirst()) {
            do {
                val fila = HashMap<String, Any?>()
                fila["recibo_int_id"] = cursor.getString(0)
                fila["unidad_id"] = cursor.getInt(1)
                fila["predio_id"] = cursor.getInt(2)
                fila["predio_nombre"] = cursor.getString(3)
                fila["unidad_nombre"] = cursor.getString(4)
                fila["fecha_emision"] = cursor.getString(5)
                fila["fecha_inicio"] = cursor.getString(6)
                fila["fecha_fin"] = cursor.getString(7)
                fila["lectura_inicio"] = if (!cursor.isNull(8)) cursor.getDouble(8) else null
                fila["lectura_fin"] = if (!cursor.isNull(9)) cursor.getDouble(9) else null
                fila["consumo"] = if (!cursor.isNull(10)) cursor.getDouble(10) else null
                fila["costoKw"] = if (!cursor.isNull(11)) cursor.getDouble(11) else null
                fila["monto_total"] = cursor.getDouble(12)
                fila["descripcion"] = cursor.getString(13)
                lista.add(fila)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return lista
    }

    fun BCobtenerHistorialConsumo(nombreUnidad: String): ArrayList<HashMap<String, Any>> {
        val lista = ArrayList<HashMap<String, Any>>()
        val db = readableDatabase
        val sql = """
        SELECT ri.fecha_inicio, ri.fecha_fin, ri.consumo, ri.monto_total
        FROM recibos_internos ri
        INNER JOIN unidades u ON u.unidad_id = ri.unidad_id
        WHERE u.nombre = ? AND ri.eliminado = 0
        ORDER BY ri.fecha_inicio DESC
    """
        val cursor = db.rawQuery(sql, arrayOf(nombreUnidad))

        if (cursor.moveToFirst()) {
            do {
                val map = HashMap<String, Any>()
                val fechaInicio = cursor.getString(1)
                val consumo = cursor.getDouble(2)
                val monto = cursor.getDouble(3)
                val kwhDia = consumo / 30

                map["mes"] = BCobtenerMesDesdeFecha(fechaInicio)
                map["consumo"] = consumo
                map["costo"] = monto
                map["kwhDia"] = String.format("%.2f", kwhDia)

                lista.add(map)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return lista
    }

    private fun BCobtenerMesDesdeFecha(fecha: String): String {
        val partes = fecha.split("-")
        val meses = arrayOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )
        return if (partes.size >= 2) "${meses[partes[1].toInt() - 1]} ${partes[0]}" else fecha
    }

    fun BCcalcularPromedios(lista: ArrayList<HashMap<String, Any>>): HashMap<String, Any> {
        val resultado = HashMap<String, Any>()
        if (lista.isEmpty()) {
            resultado["promedioMensual"] = 0.0
            resultado["costoPromedio"] = 0.0
            resultado["discrepancia"] = 0.0
            resultado["tendencia"] = 0.0
            return resultado
        }

        var sumaConsumo = 0.0
        var sumaCosto = 0.0
        var tendencia = 0.0

        for (i in lista.indices) {
            val consumo = lista[i]["consumo"].toString().toDouble()
            val costo = lista[i]["costo"].toString().toDouble()
            sumaConsumo += consumo
            sumaCosto += costo
            if (i > 0) {
                val anterior = lista[i - 1]["consumo"].toString().toDouble()
                tendencia += ((consumo - anterior) / anterior) * 100
            }
        }

        val promedioConsumo = sumaConsumo / lista.size
        val promedioCosto = sumaCosto / lista.size
        val promedioTendencia = if (lista.size > 1) tendencia / (lista.size - 1) else 0.0

        resultado["promedioMensual"] = promedioConsumo
        resultado["costoPromedio"] = promedioCosto
        resultado["discrepancia"] = promedioConsumo * 0.05
        resultado["tendencia"] = promedioTendencia

        return resultado
    }

    // ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
    // ▓▓▓▓▓▓▓▓▓▓▓  FUNCIONES DE JUAN AGUILAR  ▓▓▓▓▓▓▓▓▓▓▓
    // ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
    fun obtenerUnidadesPorPredio(predioId: Int): List<Pair<Int, String>> {
        val lista = mutableListOf<Pair<Int, String>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT unidad_id, nombre
            FROM unidades
            WHERE predio_id = ? AND eliminado = 0
            
            """,
            arrayOf(predioId.toString())
        )
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val nombre = cursor.getString(1)
                lista.add(id to nombre)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    fun ac_obtenerClasificacionPorId(unidadId: Int): String? {
        val db = this.readableDatabase
        val query = "SELECT clasificacion FROM unidades WHERE unidad_id = ? AND eliminado = 0"
        val cursor = db.rawQuery(query, arrayOf(unidadId.toString()))
        var clasificacion: String? = null
        if (cursor.moveToFirst()) {
            clasificacion = cursor.getString(0)
        }
        cursor.close()
        db.close()
        return clasificacion
    }

    fun ac_obtenerUltimoPeriodoProveedor(predioId: Int): Pair<String, String>?{
        val db = this.readableDatabase
        val query = """
            SELECT periodo_inicio, periodo_fin
            FROM recibos_proveedor
            WHERE predio_id = ? AND eliminado = 0
            ORDER BY periodo_fin DESC
            LIMIT 1
        """
        val cursor = db.rawQuery(query,arrayOf(predioId.toString()))
        var resultado: Pair<String,String>? = null
        if (cursor.moveToFirst()){
            val inicio = cursor.getString(0)
            val fin = cursor.getString(1)
            resultado = Pair(inicio,fin)
        }
        cursor.close()
        db.close()
        return resultado
    }

    // Método para obtener unidades con contrato vigente para un predio
    fun obtenerUnidadesConContratoVigente(predioId: Int): List<Pair<Int, String>> { // Devuelve (unidad_id, nombre_unidad)
        val db = this.readableDatabase
        // JOIN para relacionar unidades con contratos y predios
        // REMOVIDO: AND c.eliminado = 0
        val query = """
            SELECT DISTINCT u.unidad_id, u.nombre
            FROM unidades u
            INNER JOIN contratos c ON u.unidad_id = c.unidad_id
            INNER JOIN predios p ON u.predio_id = p.predio_id
            WHERE p.predio_id = ? AND c.estado = 'ACTIVO' AND u.eliminado = 0
        """
        val cursor = db.rawQuery(query, arrayOf(predioId.toString()))
        val unidades = mutableListOf<Pair<Int, String>>()

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val nombre = cursor.getString(1)
                unidades.add(Pair(id, nombre))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return unidades
    }

    // Método para obtener el contrato vigente de una unidad
    fun obtenerContratoVigente(unidadId: Int): Pair<Int, Int>? { // Devuelve (contrato_id, lectura_id_inicial_del_contrato) o null
        val db = this.readableDatabase
        // REMOVIDO: AND eliminado = 0
        val query = """
            SELECT contrato_id, lectura_id
            FROM contratos
            WHERE unidad_id = ? AND estado = 'ACTIVO'
            LIMIT 1
        """
        val cursor = db.rawQuery(query, arrayOf(unidadId.toString()))
        var resultado: Pair<Int, Int>? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(0)
            val lecturaIdInicial = cursor.getInt(1)
            resultado = Pair(id, lecturaIdInicial)
        }
        cursor.close()
        db.close()
        return resultado
    }

    // Método para obtener la lectura inicial de un contrato (la lectura con tipo 'INICIAL')
    fun obtenerLecturaInicialContrato(lecturaId: Int): Triple<Int, String, String>? { // Devuelve (lectura_id, valor, fecha) o null
        val db = this.readableDatabase
        val query = """
            SELECT lectura_id, valor, fecha
            FROM lecturas_sub
            WHERE lectura_id = ? AND tipo_lectura = 'INICIAL' AND eliminado = 0
        """
        val cursor = db.rawQuery(query, arrayOf(lecturaId.toString()))
        var resultado: Triple<Int, String, String>? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(0)
            val valor = cursor.getString(1)
            val fecha = cursor.getString(2)
            resultado = Triple(id, valor, fecha)
        }
        cursor.close()
        db.close()
        return resultado
    }

    // Método para obtener la última lectura final usada en un recibo emitido de un contrato
    fun obtenerUltimaLecturaFinRecibo(contratoId: Int): Triple<Int, String, String>? { // Devuelve (lectura_id, valor, fecha) o null
        val db = this.readableDatabase
        // Buscamos el recibo interno más reciente emitido (emitido = 1) para el contrato
        // REMOVIDO: AND c.eliminado = 0
        val query = """
            SELECT ls.lectura_id, ls.fecha, ls.valor
            FROM recibos_internos ri
            INNER JOIN calculos_medidores cm ON ri.calculo_medidor_id = cm.calculo_medidor_id
            INNER JOIN lecturas_sub ls ON cm.lectura_fin_id = ls.lectura_id
            WHERE cm.contrato_id = ?
              AND ri.emitido = 1
              AND ri.eliminado = 0
            ORDER BY ls.fecha DESC
            LIMIT 1
        """
        val cursor = db.rawQuery(query, arrayOf(contratoId.toString()))
        var resultado: Triple<Int, String, String>? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(0)
            val fecha = cursor.getString(1)
            val valor = cursor.getString(2)
            resultado = Triple(id, valor, fecha) // Triple(lectura_id, valor, fecha)
        }
        cursor.close()
        db.close()
        return resultado
    }

    // Método para obtener la última lectura final usada en un cálculo no asignado a recibo
    fun obtenerUltimaLecturaFinCalculo(contratoId: Int): Triple<Int, String, String>? { // Devuelve (lectura_id, valor, fecha) o null
        val db = this.readableDatabase
        // Buscamos el cálculo de medidor más reciente no asignado a un recibo para el contrato
        val query = """
            SELECT cm.lectura_fin_id, ls.fecha, ls.valor
            FROM calculos_medidores cm
            INNER JOIN lecturas_sub ls ON cm.lectura_fin_id = ls.lectura_id
            WHERE cm.contrato_id = ? AND cm.calculo_medidor_id NOT IN (
                SELECT DISTINCT calculo_medidor_id FROM recibos_internos WHERE calculo_medidor_id IS NOT NULL
            ) AND ls.eliminado = 0
            ORDER BY ls.fecha DESC
            LIMIT 1
        """
        val cursor = db.rawQuery(query, arrayOf(contratoId.toString()))
        var resultado: Triple<Int, String, String>? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(0)
            val fecha = cursor.getString(1)
            val valor = cursor.getString(2)
            resultado = Triple(id, valor, fecha) // Triple(lectura_id, valor, fecha)
        }
        cursor.close()
        db.close()
        return resultado
    }

    // Método para obtener lecturas posteriores a una fecha específica, no usadas en recibos o cálculos
    fun obtenerLecturasPosterioresDisponibles(ultimoIdUsado: Int, unidadId: Int): List<Triple<Int, String, String>> { // Devuelve (lectura_id, valor, fecha)
        val db = this.readableDatabase
        val query = """
            SELECT ls.lectura_id, ls.valor, ls.fecha
            FROM lecturas_sub ls
            WHERE ls.fecha > (SELECT fecha FROM lecturas_sub WHERE lectura_id = ?)
              AND ls.submedidor_id IN (SELECT submedidor_id FROM submedidores WHERE unidad_id = ?)
              AND ls.lectura_id NOT IN (
                  SELECT DISTINCT lectura_inicio FROM recibos_internos WHERE unidad_id = ?
                  UNION
                  SELECT DISTINCT lectura_fin FROM recibos_internos WHERE unidad_id = ?
                  UNION
                  SELECT DISTINCT lectura_inicio_id FROM calculos_medidores WHERE contrato_id IN (SELECT contrato_id FROM contratos WHERE unidad_id = ? AND estado = 'ACTIVO') -- Asumiendo que solo queremos del contrato ACTIVO
                  UNION
                  SELECT DISTINCT lectura_fin_id FROM calculos_medidores WHERE contrato_id IN (SELECT contrato_id FROM contratos WHERE unidad_id = ? AND estado = 'ACTIVO') -- Asumiendo que solo queremos del contrato ACTIVO
              )
              AND ls.eliminado = 0
            ORDER BY ls.fecha ASC
        """
        val cursor = db.rawQuery(query, arrayOf(ultimoIdUsado.toString(), unidadId.toString(), unidadId.toString(), unidadId.toString(), unidadId.toString(), unidadId.toString()))
        val lecturas = mutableListOf<Triple<Int, String, String>>() // Cambiado a Triple

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val valor = cursor.getString(1)
                val fecha = cursor.getString(2)
                lecturas.add(Triple(id, valor, fecha)) // Cambiado a Triple
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lecturas
    }

    // Método para obtener el estado de cálculo más reciente no asignado a recibo para un contrato
    fun obtenerUltimoCalculoNoAsignado(contratoId: Int): Triple<Int, Boolean, Boolean>? { // Devuelve (calculo_medidor_id, tieneFechas, estaNormalizado) o null
        val db = this.readableDatabase
        val query = """
            SELECT calculo_medidor_id, lectura_inicio_id, lectura_fin_id, consumo_raw, dias_medidos, consumo_normalizado
            FROM calculos_medidores
            WHERE contrato_id = ? AND calculo_medidor_id NOT IN (
                SELECT DISTINCT calculo_medidor_id FROM recibos_internos WHERE calculo_medidor_id IS NOT NULL
            )
            ORDER BY fechaasig DESC
            LIMIT 1
        """
        val cursor = db.rawQuery(query, arrayOf(contratoId.toString()))
        var resultado: Triple<Int, Boolean, Boolean>? = null

        if (cursor.moveToFirst()) {

            val id = cursor.getInt(0)
            Log.d("DEBUG PROPIO JP", "cursor id: $id")
            val inicioId = cursor.getInt(1)
            val finId = cursor.getInt(2)
            val consumoRaw = cursor.getString(3) // getString para verificar nulos
            val diasMedidos = cursor.getString(4)
            val consumoNormalizado = cursor.getString(5)

            val tieneFechas = inicioId != 0 && finId != 0 // Asumiendo que 0 indica null si no se asigna
            val estaNormalizado = !consumoRaw.isNullOrEmpty() && !diasMedidos.isNullOrEmpty() && !consumoNormalizado.isNullOrEmpty()

            resultado = Triple(id, tieneFechas, estaNormalizado) // Triple(calculo_id, tieneFechas, estaNormalizado)
        }
        cursor.close()
        db.close()
        return resultado
    }

    // Método para insertar o actualizar un cálculo de medidor
    fun insertarActualizarCalculoMedidor(contratoId: Int, lecturaInicioId: Int, lecturaFinId: Int, consumoRaw: Double?, diasMedidos: Int?, consumoNormalizado: Double?): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("contrato_id", contratoId)
            put("lectura_inicio_id", lecturaInicioId)
            put("lectura_fin_id", lecturaFinId)
            if (consumoRaw != null) put("consumo_raw", consumoRaw) else putNull("consumo_raw")
            if (diasMedidos != null) put("dias_medidos", diasMedidos) else putNull("dias_medidos")
            if (consumoNormalizado != null) put("consumo_normalizado", consumoNormalizado) else putNull("consumo_normalizado")
            put("fechaasig", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().time)) // Fecha actual
            put("notas", "Calculado desde UI") // Nota opcional
        }
        // Intentamos actualizar si ya existe un registro sin asignar
        val whereClause = "contrato_id = ? AND calculo_medidor_id NOT IN (SELECT DISTINCT calculo_medidor_id FROM recibos_internos WHERE calculo_medidor_id IS NOT NULL)"
        val whereArgs = arrayOf(contratoId.toString())
        val rowsUpdated = db.update("calculos_medidores", values, whereClause, whereArgs)

        return if (rowsUpdated > 0) {
            // Si se actualizó, devolvemos el ID del registro actualizado
            // Para obtenerlo, necesitamos la fecha de la actualización
            val query = "SELECT calculo_medidor_id FROM calculos_medidores WHERE contrato_id = ? AND fechaasig = ? AND calculo_medidor_id NOT IN (SELECT DISTINCT calculo_medidor_id FROM recibos_internos WHERE calculo_medidor_id IS NOT NULL) LIMIT 1"
            val cursor = db.rawQuery(query, arrayOf(contratoId.toString(), values.getAsString("fechaasig")))
            val id = if (cursor.moveToFirst()) cursor.getLong(0) else -1
            cursor.close()
            id
        } else {
            // Si no se actualizó, insertamos un nuevo registro
            db.insert("calculos_medidores", null, values)
        }
    }

    // Método para obtener detalles de lectura por ID
    fun obtenerDetalleLectura(lecturaId: Int): Pair<String, String>? { // Devuelve (fecha, valor) o null
        val db = this.readableDatabase
        val query = "SELECT fecha, valor FROM lecturas_sub WHERE lectura_id = ? AND eliminado = 0"
        val cursor = db.rawQuery(query, arrayOf(lecturaId.toString()))
        var resultado: Pair<String, String>? = null
        if (cursor.moveToFirst()) {
            val fecha = cursor.getString(0)
            val valor = cursor.getString(1)
            resultado = Pair(fecha, valor)
        }
        cursor.close()
        db.close()
        return resultado
    }

    // Obtiene los recibos proveedor del predio que NO están en calculos_distribucion
    fun obtenerRecibosProveedorSinDistribucion(predioId: Int): List<Triple<Int, String, String>> {
        val db = this.readableDatabase
        val query = """
        SELECT rp.recibo_prov_id, rp.periodo_inicio, rp.periodo_fin
        FROM recibos_proveedor rp
        LEFT JOIN calculos_distribucion cd ON rp.recibo_prov_id = cd.recibo_prov_id
        WHERE rp.predio_id = ? AND cd.recibo_prov_id IS NULL AND rp.eliminado = 0
        ORDER BY rp.periodo_fin DESC
    """
        val cursor = db.rawQuery(query, arrayOf(predioId.toString()))
        val lista = mutableListOf<Triple<Int, String, String>>()
        while (cursor.moveToNext()) {
            lista.add(Triple(
                cursor.getInt(0), // id
                cursor.getString(1), // inicio
                cursor.getString(2)  // fin
            ))
        }
        cursor.close()
        db.close()
        return lista
    }

    // Obtiene la suma de consumo_raw de cálculos no asignados a recibo interno, para un predio
    fun obtenerSumaConsumoRawNoAsignado(predioId: Int): Double {
        val db = this.readableDatabase
        val query = """
        SELECT SUM(cm.consumo_raw)
        FROM calculos_medidores cm
        INNER JOIN contratos c ON cm.contrato_id = c.contrato_id
        INNER JOIN unidades u ON c.unidad_id = u.unidad_id
        WHERE u.predio_id = ?
          AND cm.consumo_raw IS NOT NULL
          AND cm.calculo_medidor_id NOT IN (
              SELECT DISTINCT ri.calculo_medidor_id 
              FROM recibos_internos ri 
              WHERE ri.calculo_medidor_id IS NOT NULL
          )
    """
        val cursor = db.rawQuery(query, arrayOf(predioId.toString()))
        var suma = 0.0
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            suma = cursor.getDouble(0)
        }
        cursor.close()
        db.close()
        return suma
    }

    // Devuelve true si todas las unidades del predio tienen cálculo normalizado y no asignado a recibo
    fun todasUnidadesNormalizadas(predioId: Int): Boolean {
        val db = this.readableDatabase
        val query = """
        SELECT u.unidad_id
    FROM unidades u
    INNER JOIN contratos c ON u.unidad_id = c.unidad_id
    WHERE u.predio_id = ?
      AND c.estado = 'ACTIVO'
      AND u.eliminado = 0
      AND c.contrato_id NOT IN (
          SELECT DISTINCT cm2.contrato_id
          FROM calculos_medidores cm2
          WHERE cm2.consumo_raw IS NOT NULL
            AND cm2.dias_medidos IS NOT NULL
            AND cm2.consumo_normalizado IS NOT NULL
            AND cm2.calculo_medidor_id NOT IN (
                SELECT DISTINCT ri2.calculo_medidor_id 
                FROM recibos_internos ri2 
                WHERE ri2.calculo_medidor_id IS NOT NULL
            )
      )
    """
        val cursor = db.rawQuery(query, arrayOf(predioId.toString()))
        val todasListas = !cursor.moveToFirst() // Si no hay filas, todas están listas
        cursor.close()
        db.close()
        return todasListas
    }


    // Obtiene recibos no distribuidos + pendientes
    fun obtenerRecibosParaDistribucion(predioId: Int): List<Triple<Int, String, Boolean>> {
        val db = this.readableDatabase
        val query = """
        SELECT rp.recibo_prov_id, rp.periodo_inicio, rp.periodo_fin, cd.estado
        FROM recibos_proveedor rp
        LEFT JOIN calculos_distribucion cd ON rp.recibo_prov_id = cd.recibo_prov_id
        WHERE rp.predio_id = ? AND rp.eliminado = 0
          AND (cd.estado IS NULL OR cd.estado = 'pendiente')
        ORDER BY rp.periodo_fin DESC
    """
        val cursor = db.rawQuery(query, arrayOf(predioId.toString()))
        val lista = mutableListOf<Triple<Int, String, Boolean>>()
        val formatterDB = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatterUI = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val inicio = cursor.getString(1)
            val fin = cursor.getString(2)
            val estado = cursor.getString(3)
            val esPendiente = estado == "pendiente"
            val texto = "Recibo #${cursor.getInt(0)} (${formatterUI.format(formatterDB.parse(inicio))} - ${formatterUI.format(formatterDB.parse(fin))})"
            lista.add(Triple(id, texto, esPendiente))
        }
        cursor.close()
        db.close()
        return lista
    }

    // Obtiene datos de un recibo proveedor
    fun obtenerDatosRecibo(reciboProvId: Int): Ac_Quadruple<String, String, Double, Double>? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT periodo_inicio, periodo_fin, consumo_total, monto_total FROM recibos_proveedor WHERE recibo_prov_id = ?",
            arrayOf(reciboProvId.toString())
        )
        var result: Ac_Quadruple<String, String, Double, Double>? = null
        if (cursor.moveToFirst()) {
            result = Ac_Quadruple(
                cursor.getString(0),
                cursor.getString(1),
                cursor.getDouble(2),
                cursor.getDouble(3)
            )
        }
        cursor.close()
        db.close()
        return result
    }


    // Obtiene suma de consumo normalizado no asignado
    fun obtenerSumaConsumoNormalizado(predioId: Int): Double {
        val db = this.readableDatabase
        val query = """
        SELECT SUM(cm.consumo_normalizado)
        FROM calculos_medidores cm
        INNER JOIN contratos c ON cm.contrato_id = c.contrato_id
        INNER JOIN unidades u ON c.unidad_id = u.unidad_id
        WHERE u.predio_id = ?
          AND cm.consumo_normalizado IS NOT NULL
          AND cm.calculo_medidor_id NOT IN (
              SELECT DISTINCT ri.calculo_medidor_id 
              FROM recibos_internos ri 
              WHERE ri.calculo_medidor_id IS NOT NULL
          )
    """
        val cursor = db.rawQuery(query, arrayOf(predioId.toString()))
        val suma = if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getDouble(0) else 0.0
        cursor.close()
        db.close()
        return suma
    }

    // Obtiene unidades normalizadas no asignadas
    fun obtenerUnidadesNormalizadasNoAsignadas(predioId: Int): List<Ac_Quadruple<Int, String, Int, Double>> {
        val db = this.readableDatabase
        val query = """
        SELECT u.unidad_id, u.nombre, cm.calculo_medidor_id, cm.consumo_normalizado
        FROM unidades u
        INNER JOIN contratos c ON u.unidad_id = c.unidad_id
        INNER JOIN calculos_medidores cm ON c.contrato_id = cm.contrato_id
        WHERE u.predio_id = ?
          AND c.estado = 'ACTIVO'
          AND u.eliminado = 0
          AND cm.consumo_raw IS NOT NULL
          AND cm.dias_medidos IS NOT NULL
          AND cm.consumo_normalizado IS NOT NULL
          AND cm.calculo_medidor_id NOT IN (
              SELECT DISTINCT ri.calculo_medidor_id 
              FROM recibos_internos ri 
              WHERE ri.calculo_medidor_id IS NOT NULL
          )
    """
        val cursor = db.rawQuery(query, arrayOf(predioId.toString()))
        val lista = mutableListOf<Ac_Quadruple<Int, String, Int, Double>>()
        while (cursor.moveToNext()) {
            lista.add(Ac_Quadruple(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getInt(2),
                cursor.getDouble(3)
            ))
        }
        cursor.close()
        db.close()
        return lista
    }

    // Inserta en calculos_distribucion
    fun insertarCalculoDistribucion(
        reciboProvId: Int,
        estado: String,
        sumaTotalInterna: Double,
        discrepancia: Double
    ): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("recibo_prov_id", reciboProvId)
            put("estado", estado)
            put("ejecutado_en", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                java.util.Date()
            ))
            put("suma_total_interna", sumaTotalInterna)
            put("discrepancia", discrepancia)
            put("notas", "Cálculo automático desde UI")
        }
        return db.insert("calculos_distribucion", null, values).also { db.close() }
    }

    // Inserta en distribucion_monetaria_unidades
    // Inserta en distribucion_monetaria_unidades
    fun insertarDistribucionUnidad(
        calculoId: Long,
        calculoMedidorId: Int,
        consumoTotalUnidad: Double,
        montoAsignado: Double
    ) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("calculo_id", calculoId)
            put("calculo_medidor_id", calculoMedidorId)
            put("consumo_total_unidad", consumoTotalUnidad)
            put("monto_asignado", montoAsignado)
            put("notas", "Distribución automática")
        }
        db.insert("distribucion_monetaria_unidades", null, values)
        db.close()
    }

    // Obtiene ocupantes por unidad_id
    fun getOcupantes(unidadId: Int): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT ocupantes FROM unidades WHERE unidad_id = ?", arrayOf(unidadId.toString()))
        val ocupantes = if (cursor.moveToFirst()) cursor.getInt(0) else 1
        cursor.close()
        db.close()
        return ocupantes
    }

    // Obtiene ocupantes por nombre (para distribución existente)
    fun getOcupantesPorNombre(nombre: String): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT ocupantes FROM unidades WHERE nombre = ?", arrayOf(nombre))
        val ocupantes = if (cursor.moveToFirst()) cursor.getInt(0) else 1
        cursor.close()
        db.close()
        return ocupantes
    }

    // Obtiene distribución ya calculada
    fun obtenerDistribucionPorRecibo(reciboProvId: Int): List<Ac_Quadruple<String, Double, Double, Double>> {
        val db = this.readableDatabase
        val query = """
        SELECT 
            u.nombre,
            cm.consumo_normalizado,
            dm.consumo_total_unidad,
            dm.monto_asignado
        FROM distribucion_monetaria_unidades dm
        INNER JOIN calculos_medidores cm ON dm.calculo_medidor_id = cm.calculo_medidor_id
        INNER JOIN contratos c ON cm.contrato_id = c.contrato_id
        INNER JOIN unidades u ON c.unidad_id = u.unidad_id
        INNER JOIN calculos_distribucion cd ON dm.calculo_id = cd.calculo_id
        WHERE cd.recibo_prov_id = ?
    """
        val cursor = db.rawQuery(query, arrayOf(reciboProvId.toString()))
        val lista = mutableListOf<Ac_Quadruple<String, Double, Double, Double>>()
        while (cursor.moveToNext()) {
            lista.add(Ac_Quadruple(
                cursor.getString(0), // nombre
                cursor.getDouble(1), // consumo_normalizado
                cursor.getDouble(2), // consumo_total_unidad
                cursor.getDouble(3)  // monto_asignado
            ))
        }
        cursor.close()
        db.close()
        return lista
    }

    fun getUnidadIdPorNombre(nombre: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT unidad_id FROM unidades WHERE nombre = ?", arrayOf(nombre))
        return if (cursor.moveToFirst()) cursor.getInt(0) else -1.also { cursor.close() }.also { cursor.close() }
    }

    fun getSubmedidorIdPorUnidad(unidadId: Int): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT submedidor_id FROM submedidores WHERE unidad_id = ?", arrayOf(unidadId.toString()))
        return if (cursor.moveToFirst()) cursor.getInt(0) else -1.also { cursor.close() }.also { cursor.close() }
    }

    fun getCalculoDistribucionIdPorRecibo(reciboProvId: Int): Long {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT calculo_id FROM calculos_distribucion WHERE recibo_prov_id = ?", arrayOf(reciboProvId.toString()))
        return if (cursor.moveToFirst()) cursor.getLong(0) else -1L.also { cursor.close() }
    }

    fun getCalculoMedidorIdPorUnidadYDistribucion(unidadId: Int, calculoDistId: Long): Int {
        val db = readableDatabase
        val query = """
        SELECT cm.calculo_medidor_id
        FROM calculos_medidores cm
        INNER JOIN contratos c ON cm.contrato_id = c.contrato_id
        INNER JOIN distribucion_monetaria_unidades dm ON cm.calculo_medidor_id = dm.calculo_medidor_id
        WHERE c.unidad_id = ? AND dm.calculo_id = ?
    """
        val cursor = db.rawQuery(query, arrayOf(unidadId.toString(), calculoDistId.toString()))
        return if (cursor.moveToFirst()) cursor.getInt(0) else -1.also { cursor.close() }
    }

    fun getLecturasYFechasDesdeCalculo(calculoMedidorId: Int): Ac_Quadruple<String, String, Double, Double>? {
        val db = readableDatabase
        val query = """
        SELECT 
            ls_inicio.fecha, 
            ls_fin.fecha,
            ls_inicio.valor,
            ls_fin.valor
        FROM calculos_medidores cm
        INNER JOIN lecturas_sub ls_inicio ON cm.lectura_inicio_id = ls_inicio.lectura_id
        INNER JOIN lecturas_sub ls_fin ON cm.lectura_fin_id = ls_fin.lectura_id
        WHERE cm.calculo_medidor_id = ?
    """
        val cursor = db.rawQuery(query, arrayOf(calculoMedidorId.toString()))
        return if (cursor.moveToFirst()) {
            Ac_Quadruple(
                cursor.getString(0), // fecha_inicio
                cursor.getString(1), // fecha_fin
                cursor.getDouble(2), // lectura_inicio
                cursor.getDouble(3)  // lectura_fin
            )
        } else null.also { cursor.close() }
    }

    fun insertarReciboInterno(
        reciboIntId: String,
        unidadId: Int,
        fechaEmision: String,
        fechaVencimiento: String,
        montoTotal: Double,
        submedidorId: Int,
        fechaInicio: String,
        fechaFin: String,
        lecturaInicio: Double,
        lecturaFin: Double,
        costoKw: Double,
        consumo: Double,
        calculoMedidorId: Int
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("recibo_int_id", reciboIntId)
            put("unidad_id", unidadId)
            put("fecha_emision", fechaEmision)
            put("fecha_vencimiento", fechaVencimiento)
            put("monto_total", montoTotal)
            put("pdf_ruta", "") // vacío
            put("submedidor_id", submedidorId)
            put("descripcion", "")
            put("fecha_inicio", fechaInicio)
            put("fecha_fin", fechaFin)
            put("lectura_inicio", lecturaInicio)
            put("lectura_fin", lecturaFin)
            put("costoKw", costoKw)
            put("consumo", consumo)
            put("emitido", 1)
            put("eliminado", 0)
            put("calculo_medidor_id", calculoMedidorId)
        }
        db.insert("recibos_internos", null, values)
        db.close()
    }

    fun marcarDistribucionComoCalculada(reciboProvId: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("estado", "calculado")
        }
        db.update("calculos_distribucion", values, "recibo_prov_id = ?", arrayOf(reciboProvId.toString()))
        db.close()
    }

    // Obtiene el último recibo proveedor por fecha_registro
    fun obtenerUltimoReciboProveedor(predioId: Int): Triple<Int, String, String>? {
        val db = readableDatabase
        val query = """
        SELECT recibo_prov_id, periodo_inicio, periodo_fin
        FROM recibos_proveedor
        WHERE predio_id = ? AND eliminado = 0
        ORDER BY fecha_registro DESC
        LIMIT 1
    """
        val cursor = db.rawQuery(query, arrayOf(predioId.toString()))
        return if (cursor.moveToFirst()) {
            Triple(cursor.getInt(0), cursor.getString(1), cursor.getString(2))
        } else null.also { cursor.close() }
    }

    // Verifica si ya existe una distribución para un recibo
    fun existeDistribucionParaRecibo(reciboProvId: Int): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT 1 FROM calculos_distribucion WHERE recibo_prov_id = ? LIMIT 1",
            arrayOf(reciboProvId.toString())
        )
        val existe = cursor.moveToFirst()
        cursor.close()
        return existe
    }

}
