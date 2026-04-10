-- =====================================================================
-- SCRIPT DE CREACIÓN DE BASE DE DATOS Y PROCEDIMIENTOS (EL JOC D'EN PINGU)
-- Ejecutar en Oracle (SQL Developer, DBeaver, o SQL*Plus)
-- =====================================================================

-- 1. CREACIÓN DE LA TABLA PARTIDA
-- Esta tabla guarda el estado completo del juego de forma encriptada
BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE PARTIDA CASCADE CONSTRAINTS';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -942 THEN
            RAISE;
        END IF;
END;
/

CREATE TABLE PARTIDA (
    id_partida NUMBER PRIMARY KEY,
    estat CLOB NOT NULL, -- Datos sobre turnos y estado (encriptado en AES Base64)
    dades_jugadors CLOB NOT NULL, -- Inventario, posición y jugadores (encriptado)
    data_creacio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_modificacio TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
/

-- 2. PROCEDIMIENTO ALMACENADO PARA GUARDAR/ACTUALIZAR LA PARTIDA SUPER SEGURA
-- Cumple con el requisito de utilizar PL/SQL para la lógica de negocio
CREATE OR REPLACE PROCEDURE GuardarPartidaSegura (
    p_id_partida IN NUMBER,
    p_estat_encriptat IN CLOB,
    p_dades_jugadors IN CLOB
) 
AS
BEGIN
    -- Utiliza MERGE para actualizar si ya existe o insertar si es nueva
    MERGE INTO PARTIDA p
    USING dual
    ON (p.id_partida = p_id_partida)
    WHEN MATCHED THEN 
        UPDATE SET 
            estat = p_estat_encriptat,
            dades_jugadors = p_dades_jugadors,
            data_modificacio = SYSDATE
    WHEN NOT MATCHED THEN 
        INSERT (id_partida, estat, dades_jugadors, data_creacio)
        VALUES (p_id_partida, p_estat_encriptat, p_dades_jugadors, SYSDATE);
        
    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20001, 'Error al guardar la partida en PL/SQL: ' || SQLERRM);
END GuardarPartidaSegura;
/

-- 3. PROCEDIMIENTO ALMACENADO PARA ELIMINAR LA PARTIDA
CREATE OR REPLACE PROCEDURE EliminarPartida (
    p_id_partida IN NUMBER
)
AS
BEGIN
    DELETE FROM PARTIDA WHERE id_partida = p_id_partida;
    COMMIT;
END EliminarPartida;
/

-- Prueba rápida del funcionamiento (Opcional)
-- EXEC GuardarPartidaSegura(1, 'ENCRYPTED_STATE_TEST', 'ENCRYPTED_USERS_TEST');
-- SELECT * FROM PARTIDA;
