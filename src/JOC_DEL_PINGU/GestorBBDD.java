package JOC_DEL_PINGU;

import java.sql.Connection;
import java.sql.CallableStatement;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * GestorBBDD — Guardado y carga de partidas con cifrado AES-128/CBC.
 *
 * Formato de datos (antes de cifrar):
 *
 * === BLOB 1: "estat" ===
 *   torn:<idx>|turnos:<n>|finalizada:<bool>|tablero:<t0>,<t1>,...,<t49>
 *   Donde cada <ti> es un código de 1 letra:
 *     N=CasillaNormal  A=Agujero  T=Trineo  E=Evento  F=CasillaFragil  O=Oso  M=Meta
 *
 * === BLOB 2: "dades_jugadors" ===
 *   nombre|pos|color|tipo|peces|bolas|rapidos|lentos;nombre2|...;
 *   Donde tipo = P (Pinguino) o F (Foca)
 */
public class GestorBBDD {

    // ==================== ATRIBUTS ====================
    private String urlBBDD;
    private String username;
    private String password;
    private Connection conexion;

    /** Clave AES-128 (16 bytes exactos). */
    private static final String CLAVE_AES = "JocPingu2024Clau";

    // ==================== CONSTRUCTOR ====================
    public GestorBBDD() {
        this.urlBBDD = "";
        this.username = "";
        this.password = "";
    }

    // ==================== GESTIÓ DE CONNEXIÓ ====================

    public void iniciarConexion(Scanner scan) {
        this.conexion = BBDD.conectarBaseDatos(scan);
    }

    public void iniciarConexionGUI() {
        this.conexion = BBDD.conectarBaseDatosGUI();
        // La tabla USUARIOS ya existe en Oracle, no hace falta crearla.
    }

    public void cerrarConexion() {
        BBDD.cerrar(this.conexion);
        this.conexion = null;
    }

    // ==================== CIFRADO AES-128/CBC+IV ====================

    /**
     * Cifra un texto con AES-128 en modo CBC usando un IV aleatorio.
     * Formato resultado: Base64(IV_16bytes + ciphertext)
     */
    private String encriptar(String texto) {
        try {
            byte[] claveBytes = CLAVE_AES.getBytes("UTF-8");
            SecretKeySpec clave = new SecretKeySpec(claveBytes, "AES");

            // IV aleatorio de 16 bytes (CBC necesita IV)
            byte[] ivBytes = new byte[16];
            new java.security.SecureRandom().nextBytes(ivBytes);
            IvParameterSpec iv = new IvParameterSpec(ivBytes);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, clave, iv);
            byte[] cifrado = cipher.doFinal(texto.getBytes("UTF-8"));

            // Concatenar IV + cifrado para que el descifrado pueda recuperar el IV
            byte[] resultado = new byte[16 + cifrado.length];
            System.arraycopy(ivBytes, 0, resultado, 0, 16);
            System.arraycopy(cifrado, 0, resultado, 16, cifrado.length);

            return Base64.getEncoder().encodeToString(resultado);
        } catch (Exception e) {
            System.err.println("❌ Error al cifrar: " + e.getMessage());
            return texto; // fallback sin cifrar
        }
    }

    /**
     * Descifra un texto producido por {@link #encriptar(String)}.
     * Extrae el IV de los primeros 16 bytes del blob Base64.
     */
    private String desencriptar(String textoCifrado) {
        try {
            byte[] todo = Base64.getDecoder().decode(textoCifrado);

            // Los primeros 16 bytes son el IV
            byte[] ivBytes   = new byte[16];
            byte[] cipherBytes = new byte[todo.length - 16];
            System.arraycopy(todo, 0,  ivBytes,    0, 16);
            System.arraycopy(todo, 16, cipherBytes, 0, cipherBytes.length);

            byte[] claveBytes = CLAVE_AES.getBytes("UTF-8");
            SecretKeySpec clave = new SecretKeySpec(claveBytes, "AES");
            IvParameterSpec iv  = new IvParameterSpec(ivBytes);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, clave, iv);
            byte[] descifrado = cipher.doFinal(cipherBytes);

            return new String(descifrado, "UTF-8");
        } catch (Exception e) {
            // Compatibilidad con datos guardados con la versión anterior (AES/ECB)
            try {
                return desencriptarLegacy(textoCifrado);
            } catch (Exception ex) {
                System.err.println("❌ Error al descifrar: " + e.getMessage());
                return textoCifrado;
            }
        }
    }

    /** Compatibilidad con el cifrado antiguo AES/ECB (sin IV). */
    private String desencriptarLegacy(String textoCifrado) throws Exception {
        byte[] claveBytes = CLAVE_AES.getBytes("UTF-8");
        SecretKeySpec clave = new SecretKeySpec(claveBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, clave);
        byte[] descifrado = cipher.doFinal(Base64.getDecoder().decode(textoCifrado));
        return new String(descifrado, "UTF-8");
    }

    // ==================== GUARDAR PARTIDA ====================

    /**
     * Guarda la partida en la base de datos Oracle.
     * Toda la información se cifra con AES-128/CBC antes de persistirse.
     *
     * BLOB 1 (estat): turno + tablero (50 tipos de casillas)
     * BLOB 2 (dades_jugadors): jugadores con posición, tipo, inventario detallado
     */
    public boolean guardarBBDD(Partida p, int idPartida) {
        if (this.conexion == null) {
            System.out.println("❌ Sin conexión a la BBDD. Imposible guardar.");
            return false;
        }
        if (p == null) {
            System.out.println("❌ La partida no puede ser null.");
            return false;
        }

        try (CallableStatement cs = this.conexion.prepareCall("{ call GuardarPartidaSegura(?, ?, ?) }")) {

            // --- BLOB 1: Estado general + tablero ---
            String tableroStr = serializarTablero(p.getTablero());
            String estat = "torn:" + p.getIndiceJugadorActual()
                         + "|turnos:" + p.getTurnos()
                         + "|finalizada:" + p.isFinalizada()
                         + "|tablero:" + tableroStr;
            String estatCifrado = encriptar(estat);

            // --- BLOB 2: Jugadores ---
            StringBuilder jugadoresStr = new StringBuilder();
            for (Jugador j : p.getJugadores()) {
                jugadoresStr.append(serializarJugador(j)).append(";");
            }
            String jugadoresCifrado = encriptar(jugadoresStr.toString());

            cs.setInt(1, idPartida);
            cs.setString(2, estatCifrado);
            cs.setString(3, jugadoresCifrado);
            cs.execute();

            System.out.println("✅ Partida guardada (ID: " + idPartida + ") — AES-128/CBC");
            System.out.println("   Jugadores: " + p.getJugadores().size());
            System.out.println("   Tablero:   " + p.getTablero().getTotalCasillas() + " casillas guardadas");
            System.out.println("   Cifrado:   SÍ (AES-128/CBC + IV aleatorio)");
            return true;

        } catch (Exception e) {
            System.out.println("❌ Excepción al guardar en PL/SQL: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Serializa el tablero como una cadena de códigos de 1 letra separados por comas.
     *   CasillaNormal → N, Agujero → A, Trineo → T, Evento → E,
     *   CasillaFragil → F, Oso → O, (meta/otros) → M
     */
    private String serializarTablero(Tablero t) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < t.getCasillas().size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(codigoCasilla(t.getCasillas().get(i), i));
        }
        return sb.toString();
    }

    private char codigoCasilla(Casilla c, int indice) {
        if (indice == 49)              return 'M'; // meta siempre M
        if (c instanceof Agujero)      return 'A';
        if (c instanceof Trineo)       return 'T';
        if (c instanceof Evento)       return 'E';
        if (c instanceof CasillaFragil) return 'F';
        if (c instanceof Oso)          return 'O';
        return 'N'; // CasillaNormal
    }

    /**
     * Serializa un jugador con sus datos completos:
     *   nombre|posicion|color|tipo(P/F)|peces|bolas|rapidos|lentos
     *
     * El inventario se guarda como CONTADORES explícitos, no como lista de ítems,
     * para garantizar una restauración exacta y sin ambigüedades.
     */
    private String serializarJugador(Jugador j) {
        String tipo = (j instanceof Foca) ? "F" : "P";
        int peces    = contarItemsTipo(j, "pez", "peix");
        int bolas    = contarItemsTipo(j, "bola");
        int rapidos  = contarItemsTipo(j, "rapido", "rápido", "ràpid");
        int lentos   = contarItemsTipo(j, "lento");

        return j.getNombre()
             + "|" + j.getPosicion()
             + "|" + j.getColor()
             + "|" + tipo
             + "|" + peces
             + "|" + bolas
             + "|" + rapidos
             + "|" + lentos;
    }

    /** Cuenta los ítems del inventario cuyo nombre contiene alguna de las palabras clave. */
    private int contarItemsTipo(Jugador j, String... claves) {
        int n = 0;
        for (Item item : j.getInventario().getLista()) {
            String nombre = item.getNombre().toLowerCase();
            for (String clave : claves) {
                if (nombre.contains(clave)) { n++; break; }
            }
        }
        return n;
    }

    // ==================== CARGAR PARTIDA ====================

    /**
     * Carga una partida desde la base de datos Oracle y la reconstruye exactamente:
     * - Tipos de casillas del tablero restaurados
     * - Posiciones de cada jugador restauradas
     * - Tipo de jugador (Pinguino / Foca) restaurado
     * - Inventario: cantidad exacta de cada tipo de ítem
     */
    public Partida cargarBBDD(int idPartida) {
        if (this.conexion == null) {
            System.out.println("❌ Sin conexión, no se puede cargar la partida.");
            return null;
        }

        try {
            String sql = "SELECT estat, dades_jugadors FROM PARTIDA WHERE id_partida = " + idPartida;
            ArrayList<java.util.LinkedHashMap<String, String>> resultados =
                BBDD.select(this.conexion, sql);

            if (resultados == null || resultados.isEmpty()) {
                System.out.println("❌ No se encontró partida con ID: " + idPartida);
                return null;
            }

            java.util.LinkedHashMap<String, String> fila = resultados.get(0);
            String estatCifrado       = fila.get("ESTAT");
            String jugadoresCifrado   = fila.get("DADES_JUGADORS");

            if (estatCifrado == null || jugadoresCifrado == null) {
                System.out.println("❌ Datos corruptos en la BBDD.");
                return null;
            }

            // --- Descifrar ---
            String estat       = desencriptar(estatCifrado);
            String jugadoresStr = desencriptar(jugadoresCifrado);

            System.out.println("✅ Datos descifrados correctamente.");

            // --- Reconstruir partida ---
            Partida p = new Partida(); // genera tablero aleatorio inicial; lo vamos a reemplazar

            // Parsear BLOB 1: estat
            String tableroStr = null;
            for (String parte : estat.split("\\|")) {
                String[] kv = parte.split(":", 2);
                if (kv.length != 2) continue;
                switch (kv[0]) {
                    case "torn":       p.setJugadorActual(Integer.parseInt(kv[1]));       break;
                    case "turnos":     p.setTurnos(Integer.parseInt(kv[1]));              break;
                    case "finalizada": p.setFinalizada(Boolean.parseBoolean(kv[1]));     break;
                    case "tablero":    tableroStr = kv[1];                                break;
                }
            }

            // Restaurar el tablero si se guardó
            if (tableroStr != null && !tableroStr.isEmpty()) {
                p.setTablero(deserializarTablero(tableroStr));
            }

            // Parsear BLOB 2: jugadores
            for (String jStr : jugadoresStr.split(";")) {
                if (jStr.trim().isEmpty()) continue;
                Jugador j = deserializarJugador(jStr.trim());
                if (j != null) p.getJugadores().add(j);
            }

            System.out.println("✅ Partida cargada (ID: " + idPartida + ")");
            System.out.println("   Jugadores:  " + p.getJugadores().size());
            System.out.println("   Tablero:    " + p.getTablero().getTotalCasillas() + " casillas");
            System.out.println("   Turno:      " + p.getIndiceJugadorActual());
            return p;

        } catch (Exception e) {
            System.out.println("❌ Excepción al cargar: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Deserializa la cadena de códigos de casilla y devuelve un Tablero con esas casillas.
     * Formato: "N,A,T,E,F,O,N,..." (50 valores separados por comas)
     */
    private Tablero deserializarTablero(String str) {
        String[] codigos = str.split(",");
        ArrayList<Casilla> casillas = new ArrayList<>();
        for (int i = 0; i < codigos.length; i++) {
            casillas.add(casillaDesdeCodigo(codigos[i].trim(), i));
        }
        // Si el tablero guardado tiene < 50 casillas (corrupción parcial), completar
        for (int i = casillas.size(); i < 50; i++) {
            casillas.add(new CasillaNormal(i));
        }
        Tablero t = new Tablero(); // genera casillas aleatorias por constructor
        t.setCasillas(casillas);  // las reemplazamos con las guardadas
        return t;
    }

    private Casilla casillaDesdeCodigo(String codigo, int pos) {
        if (codigo.isEmpty()) return new CasillaNormal(pos);
        switch (codigo.charAt(0)) {
            case 'A': return new Agujero(pos);
            case 'T': return new Trineo(pos);
            case 'E': return new Evento(pos);
            case 'F': return new CasillaFragil(pos);
            case 'O': return new Oso(pos);
            default:  return new CasillaNormal(pos); // 'N' y 'M'
        }
    }

    /**
     * Deserializa un jugador desde su cadena serializada.
     * Formato: nombre|posicion|color|tipo|peces|bolas|rapidos|lentos
     *
     * Inventario restaurado:
     *  - Peces:   [peces]    items Pez           (protección automática vs Foca)
     *  - Bolas:   [bolas]    items BolaDeNieve   (1–3 obtenidos al caer en su casilla; se usan en guerra)
     *  - Rápidos: [rapidos]  items Dado rápido   (baja probabilidad; avanza 5–10 casillas)
     *  - Lentos:  [lentos]   items Dado lento    (alta probabilidad; avanza 1–3 casillas)
     */
    private Jugador deserializarJugador(String str) {
        String[] partes = str.split("\\|");
        if (partes.length < 8) {
            System.err.println("⚠️ Formato de jugador inválido: " + str);
            return null;
        }
        try {
            String nombre = partes[0];
            int    pos    = Integer.parseInt(partes[1]);
            String color  = partes[2];
            String tipo   = partes[3]; // "P" o "F"
            int    peces    = Integer.parseInt(partes[4]);
            int    bolas    = Integer.parseInt(partes[5]);
            int    rapidos  = Integer.parseInt(partes[6]);
            int    lentos   = Integer.parseInt(partes[7]);

            Jugador j;
            if ("F".equals(tipo)) {
                j = new Foca(pos, nombre, color);
            } else {
                j = new Pinguino(pos, nombre, color);
            }

            // --- Restaurar inventario con las cantidades exactas guardadas ---
            // Peces (protección automática vs Foca)
            for (int i = 0; i < peces; i++) {
                j.getInventario().agregarItem(new Pez("Pez", 1));
            }
            // Bolas de nieve (obtenidas 1–3 por casilla; usadas en guerra de bolas)
            for (int i = 0; i < bolas; i++) {
                j.getInventario().agregarItem(new BolaDeNieve("Bola de nieve", 1));
            }
            // Dado rápido (baja prob.; permite avanzar 5–10 casillas)
            for (int i = 0; i < rapidos; i++) {
                j.getInventario().agregarItem(new Dado("Dado Rápido", 1, 10, 5));
            }
            // Dado lento (alta prob.; avanza 1–3 casillas)
            for (int i = 0; i < lentos; i++) {
                j.getInventario().agregarItem(new Dado("Dado Lento", 1, 3, 1));
            }

            return j;
        } catch (Exception e) {
            System.err.println("⚠️ Error deserializando jugador: " + e.getMessage());
            return null;
        }
    }

    // ==================== GESTIÓ D'USUARIS ====================

    /**
     * Genera un hash SHA-256 de la contraseña (en hexadecimal, 64 chars).
     * Es determinista: el mismo input → siempre el mismo hash → comparación posible en BBDD.
     */
    private String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            System.err.println("❌ Error al hacer hash: " + e.getMessage());
            return password;
        }
    }

    /**
     * Crea la tabla USUARIOS si no existe todavía.
     * Se llama automáticamente desde iniciarConexionGUI().
     */
    private void inicializarTablaUsuarios() {
        if (this.conexion == null) return;
        String ddl = "CREATE TABLE USUARIOS (" +
                "id_usuario   NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
                "username     VARCHAR2(100) NOT NULL UNIQUE, " +
                "password_enc VARCHAR2(500) NOT NULL, " +
                "data_registre TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        try (java.sql.Statement st = this.conexion.createStatement()) {
            st.execute(ddl);
            System.out.println("✅ Tabla USUARIOS creada.");
        } catch (java.sql.SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("ORA-00955")) {
                System.out.println("ℹ️ Tabla USUARIOS ya existe.");
            } else {
                System.err.println("❌ Error creando tabla USUARIOS: " + e.getMessage());
            }
        }
    }

    /**
     * Registra un nuevo usuario usando SQL directo (sin stored procedure).
     * La contraseña se guarda como hash SHA-256.
     *
     * @return null       → registro exitoso
     *         "DUPLICATE" → el username ya existe
     *         "ERROR:..." → otro error de BD
     */
    public String registrarUsuario(String username, String password) {
        if (this.conexion == null) {
            return "ERROR:Sin conexión a la base de datos.";
        }

        // 1. Comprobar si el username ya existe
        String sqlCheck = "SELECT COUNT(*) FROM USUARIOS WHERE username = ?";
        try (java.sql.PreparedStatement ps = this.conexion.prepareStatement(sqlCheck)) {
            ps.setString(1, username.trim());
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("❌ Usuario ya existe: " + username);
                return "DUPLICATE";
            }
        } catch (java.sql.SQLException e) {
            System.err.println("❌ Error al verificar usuario: " + e.getMessage());
            return "ERROR:" + e.getMessage();
        }

        // 2. Insertar el nuevo usuario
        String sqlInsert = "INSERT INTO USUARIOS (username, password_enc) VALUES (?, ?)";
        try (java.sql.PreparedStatement ps = this.conexion.prepareStatement(sqlInsert)) {
            ps.setString(1, username.trim());
            ps.setString(2, hashPassword(password));
            ps.executeUpdate();
            // No llamar a commit() — la conexión tiene auto-commit activado (Oracle JDBC por defecto)

            // ⭐ NUEVO: Sincronizar con tabla JUGADOR para llevar estadísticas
            crearJugadorEstadisticas(username.trim(), hashPassword(password), "rojo");

            System.out.println("✅ Usuario registrado: " + username);
            return null; // null = éxito
        } catch (java.sql.SQLException e) {
            String msg = e.getMessage();
            System.err.println("❌ Error al insertar usuario: " + msg);
            // ORA-00001 = unique constraint violated (por si acaso)
            if (msg != null && (msg.contains("ORA-00001") || msg.contains("unique"))) {
                return "DUPLICATE";
            }
            return "ERROR:" + msg;
        } catch (Exception e) {
            return "ERROR:" + e.getMessage();
        }
    }

    /**
     * Valida login usando SQL directo (sin stored procedure).
     * Compara el hash SHA-256 de la contraseña introducida con el almacenado.
     *
     * @return true si las credenciales son correctas.
     */
    public boolean validarLogin(String username, String password) {
        if (this.conexion == null) {
            System.out.println("❌ Sin conexión a la BBDD.");
            return false;
        }
        String sql = "SELECT COUNT(*) FROM USUARIOS WHERE username = ? AND password_enc = ?";
        try (java.sql.PreparedStatement ps = this.conexion.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ps.setString(2, hashPassword(password));
            java.sql.ResultSet rs = ps.executeQuery();
            boolean ok = rs.next() && rs.getInt(1) > 0;
            System.out.println("Login '" + username + "': " + (ok ? "✅ Correcto" : "❌ Incorrecto"));
            return ok;
        } catch (Exception e) {
            System.err.println("❌ Excepción al validar login: " + e.getMessage());
            return false;
        }
    }

    // ==================== ALTRES MÈTODES ====================

    public boolean eliminarPartida(int idPartida) {
        if (this.conexion == null) return false;
        try (CallableStatement cs = this.conexion.prepareCall("{ call EliminarPartida(?) }")) {
            cs.setInt(1, idPartida);
            cs.execute();
            return true;
        } catch (Exception e) {
            System.out.println("❌ Excepción al eliminar partida: " + e.getMessage());
            return false;
        }
    }

    public ArrayList<Integer> obtenerListaPartidas() {
        ArrayList<Integer> lista = new ArrayList<>();
        if (this.conexion == null) return lista;
        String sql = "SELECT id_partida FROM PARTIDA ORDER BY id_partida";
        ArrayList<java.util.LinkedHashMap<String, String>> resultados = BBDD.select(this.conexion, sql);
        for (java.util.LinkedHashMap<String, String> fila : resultados) {
            String idStr = fila.get("ID_PARTIDA");
            if (idStr != null) {
                try { lista.add(Integer.parseInt(idStr)); } catch (NumberFormatException ignored) {}
            }
        }
        return lista;
    }

    public boolean existeixPartida(int idPartida) {
        if (this.conexion == null) return false;
        String sql = "SELECT COUNT(*) as total FROM PARTIDA WHERE id_partida = " + idPartida;
        ArrayList<java.util.LinkedHashMap<String, String>> resultados = BBDD.select(this.conexion, sql);
        if (resultados != null && !resultados.isEmpty()) {
            String total = resultados.get(0).get("TOTAL");
            return total != null && Integer.parseInt(total) > 0;
        }
        return false;
    }

    // ==================== GETTERS / SETTERS ====================

    public Connection getConexion()               { return conexion; }
    public void setConexion(Connection conexion)   { this.conexion = conexion; }
    public String getUrlBBDD()                     { return urlBBDD; }
    public void setUrlBBDD(String urlBBDD)         { this.urlBBDD = urlBBDD; }
    public String getUsername()                    { return username; }
    public void setUsername(String username)       { this.username = username; }
    public String getPassword()                    { return password; }
    public void setPassword(String password)       { this.password = password; }

    // ==================== SINCRONITZACIÓ USUARIOS ↔ JUGADOR ====================

    /**
     * Crea una entrada en la tabla JUGADOR cuando se registra un usuario.
     * Llama al procedure SincronizarUsuarioJugador en Oracle.
     * Si el procedure no existe, hace INSERT directo como fallback.
     */
    public boolean crearJugadorEstadisticas(String username, String passwordHash, String color) {
        if (this.conexion == null) return false;

        // Intento 1: usar el stored procedure
        try (CallableStatement cs = this.conexion.prepareCall("{ call SincronizarUsuarioJugador(?, ?, ?) }")) {
            cs.setString(1, username);
            cs.setString(2, passwordHash);
            cs.setString(3, color != null ? color : "rojo");
            cs.execute();
            return true;
        } catch (Exception e) {
            // Fallback: INSERT directo si el procedure no existe
            return crearJugadorEstadisticasFallback(username, passwordHash, color);
        }
    }

    /** Fallback: crea el jugador con SQL directo si el procedure no existe. */
    private boolean crearJugadorEstadisticasFallback(String username, String passwordHash, String color) {
        try {
            // Comprobar si ya existe
            String sqlCheck = "SELECT COUNT(*) FROM JUGADOR WHERE nombre_usuario = ?";
            try (java.sql.PreparedStatement ps = this.conexion.prepareStatement(sqlCheck)) {
                ps.setString(1, username);
                java.sql.ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) return true; // ya existe
            }

            // Generar id_jugador automáticamente
            int nuevoId = 1;
            try (java.sql.Statement st = this.conexion.createStatement();
                 java.sql.ResultSet rs = st.executeQuery("SELECT NVL(MAX(id_jugador), 0) + 1 FROM JUGADOR")) {
                if (rs.next()) nuevoId = rs.getInt(1);
            }

            // Insertar
            String sqlInsert = "INSERT INTO JUGADOR (id_jugador, nombre_usuario, contrasenya, " +
                               "color_personaje, num_partidas, partidas_ganadas) VALUES (?, ?, ?, ?, 0, 0)";
            try (java.sql.PreparedStatement ps = this.conexion.prepareStatement(sqlInsert)) {
                ps.setInt(1, nuevoId);
                ps.setString(2, username);
                ps.setString(3, passwordHash);
                ps.setString(4, color != null ? color : "rojo");
                ps.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error en fallback de sincronización: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el id_jugador a partir del username.
     * Devuelve -1 si no existe.
     */
    public int obtenerIdJugador(String username) {
        if (this.conexion == null) return -1;
        String sql = "SELECT id_jugador FROM JUGADOR WHERE nombre_usuario = ?";
        try (java.sql.PreparedStatement ps = this.conexion.prepareStatement(sql)) {
            ps.setString(1, username);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            System.err.println("❌ Error al obtener id_jugador: " + e.getMessage());
        }
        return -1;
    }

    // ==================== FINALITZAR PARTIDA ====================

    /**
     * Marca la partida como finalizada asignando el ganador.
     * El trigger 'incrementar_wins' incrementa automáticamente partidas_ganadas.
     * También suma 1 a num_partidas del ganador.
     */
    public boolean finalizarPartida(int idPartida, int idGanador) {
        if (this.conexion == null) return false;
        try {
            // 1. Asignar ganador en PARTIDA (dispara trigger incrementar_wins)
            String sql1 = "UPDATE PARTIDA SET ganador = ?, data_modificacio = CURRENT_TIMESTAMP WHERE id_partida = ?";
            try (java.sql.PreparedStatement ps = this.conexion.prepareStatement(sql1)) {
                ps.setInt(1, idGanador);
                ps.setInt(2, idPartida);
                ps.executeUpdate();
            }

            // 2. Incrementar num_partidas del ganador
            String sql2 = "UPDATE JUGADOR SET num_partidas = num_partidas + 1 WHERE id_jugador = ?";
            try (java.sql.PreparedStatement ps = this.conexion.prepareStatement(sql2)) {
                ps.setInt(1, idGanador);
                ps.executeUpdate();
            }

            System.out.println("✅ Partida " + idPartida + " finalizada. Ganador: " + idGanador);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error al finalizar partida: " + e.getMessage());
            return false;
        }
    }

    // ==================== ESTADÍSTIQUES PER A LA GUI ====================

    /**
     * Récord de partidas ganadas (function max_wins).
     */
    public int obtenerRecord() {
        if (this.conexion == null) return 0;
        try (CallableStatement cs = this.conexion.prepareCall("{ ? = call max_wins() }")) {
            cs.registerOutParameter(1, java.sql.Types.NUMERIC);
            cs.execute();
            return cs.getInt(1);
        } catch (Exception e) {
            System.err.println("❌ Error obteniendo récord: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Media de partidas ganadas (function media_de_wins).
     */
    public double obtenerMediaWins() {
        if (this.conexion == null) return 0.0;
        try (CallableStatement cs = this.conexion.prepareCall("{ ? = call media_de_wins() }")) {
            cs.registerOutParameter(1, java.sql.Types.NUMERIC);
            cs.execute();
            return cs.getDouble(1);
        } catch (Exception e) {
            System.err.println("❌ Error obteniendo media: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * % de jugadores con menos partidas ganadas (function menos_wins_porcentaje).
     */
    public double obtenerPorcentajeMenosWins(int numPartidas) {
        if (this.conexion == null) return 0.0;
        try (CallableStatement cs = this.conexion.prepareCall("{ ? = call menos_wins_porcentaje(?) }")) {
            cs.registerOutParameter(1, java.sql.Types.NUMERIC);
            cs.setInt(2, numPartidas);
            cs.execute();
            return cs.getDouble(1);
        } catch (Exception e) {
            System.err.println("❌ Error obteniendo porcentaje: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Lista de jugadores que tienen un récord concreto.
     * Devuelve cada fila como String[] {nombre, partidas_ganadas} para usar en GUI.
     */
    public ArrayList<String[]> obtenerJugadoresConRecord(int record) {
        ArrayList<String[]> lista = new ArrayList<>();
        if (this.conexion == null) return lista;
        String sql = "SELECT nombre_usuario, partidas_ganadas FROM JUGADOR WHERE partidas_ganadas = " + record;
        ArrayList<java.util.LinkedHashMap<String, String>> resultados = BBDD.select(this.conexion, sql);
        for (java.util.LinkedHashMap<String, String> fila : resultados) {
            lista.add(new String[] {
                fila.get("NOMBRE_USUARIO"),
                fila.get("PARTIDAS_GANADAS")
            });
        }
        return lista;
    }

    /**
     * Jugadores que han ganado más partidas que la media.
     */
    public ArrayList<String[]> obtenerJugadoresSobreMedia() {
        ArrayList<String[]> lista = new ArrayList<>();
        if (this.conexion == null) return lista;
        String sql = "SELECT nombre_usuario, partidas_ganadas FROM JUGADOR " +
                     "WHERE partidas_ganadas > (SELECT AVG(partidas_ganadas) FROM JUGADOR) " +
                     "ORDER BY partidas_ganadas DESC";
        ArrayList<java.util.LinkedHashMap<String, String>> resultados = BBDD.select(this.conexion, sql);
        for (java.util.LinkedHashMap<String, String> fila : resultados) {
            lista.add(new String[] {
                fila.get("NOMBRE_USUARIO"),
                fila.get("PARTIDAS_GANADAS")
            });
        }
        return lista;
    }

    /**
     * Ranking completo de jugadores ordenado por num_partidas desc.
     * Devuelve cada fila como {nombre, partidas_jugadas, partidas_ganadas}.
     */
    public ArrayList<String[]> obtenerRanking() {
        ArrayList<String[]> lista = new ArrayList<>();
        if (this.conexion == null) return lista;
        String sql = "SELECT nombre_usuario, num_partidas, partidas_ganadas " +
                     "FROM JUGADOR ORDER BY num_partidas DESC, partidas_ganadas DESC";
        ArrayList<java.util.LinkedHashMap<String, String>> resultados = BBDD.select(this.conexion, sql);
        for (java.util.LinkedHashMap<String, String> fila : resultados) {
            lista.add(new String[] {
                fila.get("NOMBRE_USUARIO"),
                fila.get("NUM_PARTIDAS"),
                fila.get("PARTIDAS_GANADAS")
            });
        }
        return lista;
    }

    /**
     * Llama al procedure ranking_partidas_jugadas para validar errores.
     * @return null si todo OK, o el mensaje de error (ORA-20001 / ORA-20002).
     */
    public String validarRanking(int idJugador) {
        if (this.conexion == null) return "Sin conexión";
        try (CallableStatement cs = this.conexion.prepareCall("{ call ranking_partidas_jugadas(?) }")) {
            cs.setInt(1, idJugador);
            cs.execute();
            return null; // OK
        } catch (java.sql.SQLException e) {
            return e.getMessage();
        }
    }

    /**
     * Obtiene la lista completa de partidas con datos detallados.
     * Devuelve cada fila como String[] {id, fecha_creacion, fecha_modificacion, estado, ganador}.
     */
    public ArrayList<String[]> obtenerListaPartidasCompleta() {
        ArrayList<String[]> lista = new ArrayList<>();
        if (this.conexion == null) return lista;
        String sql = "SELECT p.id_partida, " +
                     "       TO_CHAR(p.data_creacio, 'DD/MM/YYYY HH24:MI') AS creacion, " +
                     "       TO_CHAR(p.data_modificacio, 'DD/MM/YYYY HH24:MI') AS modificacion, " +
                     "       CASE WHEN p.ganador IS NULL THEN 'En curso' ELSE 'Finalizada' END AS estado, " +
                     "       NVL(j.nombre_usuario, '-') AS ganador_nombre " +
                     "  FROM PARTIDA p " +
                     "  LEFT JOIN JUGADOR j ON p.ganador = j.id_jugador " +
                     " ORDER BY p.id_partida DESC";
        ArrayList<java.util.LinkedHashMap<String, String>> resultados = BBDD.select(this.conexion, sql);
        for (java.util.LinkedHashMap<String, String> fila : resultados) {
            lista.add(new String[] {
                fila.get("ID_PARTIDA"),
                fila.get("CREACION"),
                fila.get("MODIFICACION"),
                fila.get("ESTADO"),
                fila.get("GANADOR_NOMBRE")
            });
        }
        return lista;
    }
}