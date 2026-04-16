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

-- =====================================================================
-- 4. TABLA DE USUARIOS (login / registro)
-- =====================================================================
BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE USUARIOS CASCADE CONSTRAINTS';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -942 THEN RAISE; END IF;
END;
/

CREATE TABLE USUARIOS (
    id_usuario   NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username     VARCHAR2(100) NOT NULL UNIQUE,
    password_enc VARCHAR2(500) NOT NULL,  -- Contraseña cifrada AES-128/CBC en Base64
    data_registre TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
/

-- 5. PROCEDIMIENTO: REGISTRAR USUARIO
--    Lanza error -20010 si el username ya existe.
CREATE OR REPLACE PROCEDURE RegistrarUsuario (
    p_username   IN VARCHAR2,
    p_password   IN VARCHAR2   -- Llega ya cifrada desde Java
) AS
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM USUARIOS WHERE username = p_username;
    IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20010, 'El usuario ya existe: ' || p_username);
    END IF;
    INSERT INTO USUARIOS (username, password_enc) VALUES (p_username, p_password);
    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END RegistrarUsuario;
/

-- 6. PROCEDIMIENTO: VALIDAR LOGIN
--    Devuelve 1 en p_resultado si las credenciales son correctas, 0 si no.
CREATE OR REPLACE PROCEDURE ValidarLogin (
    p_username  IN  VARCHAR2,
    p_password  IN  VARCHAR2,   -- Llega ya cifrada
    p_resultado OUT NUMBER
) AS
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count
    FROM USUARIOS
    WHERE username    = p_username
      AND password_enc = p_password;
    p_resultado := v_count;
END ValidarLogin;
/

-- Prueba de inserción manual (Opcional):
-- EXEC RegistrarUsuario('admin', 'ENCRYPTED_PASS');
-- SELECT * FROM USUARIOS;
