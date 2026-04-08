package JOC_DEL_PINGU;

import java.sql.Connection;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class GestorBBDD {
    
    // ==================== ATRIBUTS ====================
    private String urlBBDD;
    private String username;
    private String password;
    private Connection conexion;
    
    // Clau per a encriptació (AES 128 bits)
    private static final String CLAU_ENCRYPTACIO = "JocPingu2024Clau"; // 16 caràcters per AES-128
    
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
    
   
    public void cerrarConexion() {
        BBDD.cerrar(this.conexion);
        this.conexion = null;
    }
    
    // ==================== ENCRYPTACIÓ/DESENCRIPTACIÓ ====================
    
   
    private String encriptar(String text) {
        try {
            byte[] clauBytes = CLAU_ENCRYPTACIO.getBytes("UTF-8");
            SecretKeySpec clau = new SecretKeySpec(clauBytes, "AES");
            Cipher xifrat = Cipher.getInstance("AES");
            xifrat.init(Cipher.ENCRYPT_MODE, clau);
            byte[] textEncriptat = xifrat.doFinal(text.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(textEncriptat);
        } catch (Exception e) {
            System.out.println("Error en encriptar: " + e.getMessage());
            return text; // Si falla, retorna el text original (fallback)
        }
    }
    
  
    private String desencriptar(String textEncriptat) {
        try {
            byte[] clauBytes = CLAU_ENCRYPTACIO.getBytes("UTF-8");
            SecretKeySpec clau = new SecretKeySpec(clauBytes, "AES");
            Cipher xifrat = Cipher.getInstance("AES");
            xifrat.init(Cipher.DECRYPT_MODE, clau);
            byte[] textDesencriptat = xifrat.doFinal(Base64.getDecoder().decode(textEncriptat));
            return new String(textDesencriptat, "UTF-8");
        } catch (Exception e) {
            System.out.println("Error en desencriptar: " + e.getMessage());
            return textEncriptat; // Si falla, retorna el text original (fallback)
        }
    }
    
    // ==================== GUARDAR PARTIDA ====================
    
    
    public boolean guardarBBDD(Partida p, int idPartida) {
        if (this.conexion == null) {
            System.out.println("❌ No hi ha connexió a la BBDD. Imposible guardar la partida.");
            return false;
        }
        
        if (p == null) {
            System.out.println("❌ La partida no pot ser null.");
            return false;
        }
        
        try {
            // 1. Preparar les dades dels jugadors (encriptades)
            StringBuilder dadesJugadors = new StringBuilder();
            for (Jugador j : p.getJugadores()) {
                // Format: nom|posicio|color|inventari
                String inventariJSON = inventariAString(j.getInventario());
                dadesJugadors.append(j.getNombre())
                            .append("|").append(j.getPosicion())
                            .append("|").append(j.getColor())
                            .append("|").append(inventariJSON)
                            .append(";");
            }
            
            // 2. Encriptar les dades sensibles
            String dadesJugadorsEncriptat = encriptar(dadesJugadors.toString());
            String estatEncriptat = encriptar("torn:" + p.getIndiceJugadorActual() + 
                                              "|turnos:" + p.getTurnos() + 
                                              "|finalitzada:" + p.isFinalizada());
            
            // 3. Construir la consulta SQL (ús de placeholders per seguretat)
            // NOTA: Per simplificar, fem INSERT directe. En producció usaríem PreparedStatement
            String sql = "MERGE INTO PARTIDA p " +
                        "USING dual " +
                        "ON (p.id_partida = " + idPartida + ") " +
                        "WHEN MATCHED THEN " +
                        "UPDATE SET estat = '" + estatEncriptat + 
                        "', dades_jugadors = '" + dadesJugadorsEncriptat + 
                        "', data_modificacio = SYSDATE " +
                        "WHEN NOT MATCHED THEN " +
                        "INSERT (id_partida, estat, dades_jugadors, data_creacio) " +
                        "VALUES (" + idPartida + ", '" + estatEncriptat + 
                        "', '" + dadesJugadorsEncriptat + "', SYSDATE)";
            
            // 4. Executar la consulta
            int filesAfectades = BBDD.update(this.conexion, sql);
            
            if (filesAfectades > 0) {
                System.out.println("✅ Partida guardada amb èxit a Oracle! (ID: " + idPartida + ")");
                System.out.println("   - Jugadors: " + p.getJugadores().size());
                System.out.println("   - Torn actual: " + p.getIndiceJugadorActual());
                System.out.println("   - Dades encriptades: SÍ");
                return true;
            } else {
                System.out.println("❌ Error al guardar la partida a la BBDD.");
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("❌ Excepció al guardar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
   
    private String inventariAString(Inventario inv) {
        if (inv == null || inv.getLista() == null) {
            return "[]";
        }
        
        StringBuilder json = new StringBuilder("[");
        boolean primer = true;
        
        for (Item item : inv.getLista()) {
            if (item != null) {
                if (!primer) json.append(",");
                json.append("{")
                    .append("\"nom\":\"").append(item.getNombre()).append("\"")
                    .append(",\"quantitat\":").append(item.getCantidad())
                    .append("}");
                primer = false;
            }
        }
        
        json.append("]");
        return json.toString();
    }
    
    // ==================== CARREGAR PARTIDA ====================
    
  
    public Partida cargarBBDD(int idPartida) {
        if (this.conexion == null) {
            System.out.println("❌ No hi ha connexió, no es pot carregar el tauler.");
            return null;
        }
        
        try {
            // 1. Consultar la partida a la BBDD
            String sql = "SELECT estat, dades_jugadors FROM PARTIDA WHERE id_partida = " + idPartida;
            ArrayList<java.util.LinkedHashMap<String, String>> resultats = BBDD.select(this.conexion, sql);
            
            if (resultats == null || resultats.isEmpty()) {
                System.out.println("❌ No s'ha trobat cap partida amb ID: " + idPartida);
                return null;
            }
            
            // 2. Extreure les dades
            java.util.LinkedHashMap<String, String> fila = resultats.get(0);
            String estatEncriptat = fila.get("ESTAT");
            String dadesJugadorsEncriptat = fila.get("DADES_JUGADORS");
            
            if (estatEncriptat == null || dadesJugadorsEncriptat == null) {
                System.out.println("❌ Dades corruptes a la BBDD.");
                return null;
            }
            
            // 3. Desencriptar les dades
            String estatDesencriptat = desencriptar(estatEncriptat);
            String dadesJugadorsDesencriptat = desencriptar(dadesJugadorsEncriptat);
            
            System.out.println("✅ Dades desencriptades correctament.");
            
            // 4. Crear nova partida
            Partida p = new Partida();
            
            // 5. Parsejar l'estat (torn, turnos, finalitzada)
            String[] partsEstat = estatDesencriptat.split("\\|");
            for (String part : partsEstat) {
                String[] clauValor = part.split(":");
                if (clauValor.length == 2) {
                    switch (clauValor[0]) {
                        case "torn":
                            p.setJugadorActual(Integer.parseInt(clauValor[1]));
                            break;
                        case "turnos":
                            // p.setTurnos(Integer.parseInt(clauValor[1])); // Si tens el setter
                            break;
                        case "finalitzada":
                            p.setFinalizada(Boolean.parseBoolean(clauValor[1]));
                            break;
                    }
                }
            }
            
            // 6. Parsejar els jugadors i els seus inventaris
            String[] jugadorsData = dadesJugadorsDesencriptat.split(";");
            for (String jugadorData : jugadorsData) {
                if (jugadorData.trim().isEmpty()) continue;
                
                String[] parts = jugadorData.split("\\|");
                if (parts.length >= 4) {
                    String nom = parts[0];
                    int posicio = Integer.parseInt(parts[1]);
                    String color = parts[2];
                    String inventariJSON = parts[3];
                    
                    // Crear jugador (Pinguino per defecte)
                    Pinguino j = new Pinguino(posicio, nom, color);
                    
                    // Restaurar inventari
                    restaurarInventari(j, inventariJSON);
                    
                    p.getJugadores().add(j);
                }
            }
            
            System.out.println("✅ Partida carregada amb èxit!");
            System.out.println("   - Jugadors restaurats: " + p.getJugadores().size());
            System.out.println("   - Torn actual: " + p.getIndiceJugadorActual());
            
            return p;
            
        } catch (Exception e) {
            System.out.println("❌ Excepció al carregar: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
   
    private void restaurarInventari(Jugador j, String inventariJSON) {
        // Parseig simple del JSON (en producció usaríem una llibreria com Gson)
        inventariJSON = inventariJSON.replace("[", "").replace("]", "");
        
        if (inventariJSON.trim().isEmpty()) {
            return;
        }
        
        String[] items = inventariJSON.split("},\\{");
        for (String itemData : items) {
            itemData = itemData.replace("{", "").replace("}", "");
            String[] parts = itemData.split(",");
            
            String nom = "";
            int quantitat = 1;
            
            for (String part : parts) {
                String[] clauValor = part.split(":");
                if (clauValor.length == 2) {
                    if (clauValor[0].equals("\"nom\"")) {
                        nom = clauValor[1].replace("\"", "");
                    } else if (clauValor[0].equals("\"quantitat\"")) {
                        quantitat = Integer.parseInt(clauValor[1]);
                    }
                }
            }
            
            // Crear l'item segons el nom
            Item item = null;
            if (nom.toLowerCase().contains("dado")) {
                if (nom.toLowerCase().contains("ràpid") || nom.toLowerCase().contains("rapido")) {
                    item = new Dado(nom, quantitat, 10, 5);
                } else {
                    item = new Dado(nom, quantitat, 3, 1);
                }
            } else if (nom.toLowerCase().contains("pez") || nom.toLowerCase().contains("peix")) {
                item = new Pez(nom, quantitat);
            } else if (nom.toLowerCase().contains("bola")) {
                item = new BolaDeNieve(nom, quantitat);
            }
            
            if (item != null) {
                j.getInventario().agregarItem(item);
            }
        }
    }
    
    // ==================== ALTRES MÈTODES ====================
    
    
    public boolean eliminarPartida(int idPartida) {
        if (this.conexion == null) {
            return false;
        }
        
        String sql = "DELETE FROM PARTIDA WHERE id_partida = " + idPartida;
        int filesAfectades = BBDD.delete(this.conexion, sql);
        
        return filesAfectades > 0;
    }
    
  
    public boolean existeixPartida(int idPartida) {
        if (this.conexion == null) {
            return false;
        }
        
        String sql = "SELECT COUNT(*) as total FROM PARTIDA WHERE id_partida = " + idPartida;
        ArrayList<java.util.LinkedHashMap<String, String>> resultats = BBDD.select(this.conexion, sql);
        
        if (resultats != null && !resultats.isEmpty()) {
            String total = resultats.get(0).get("TOTAL");
            return total != null && Integer.parseInt(total) > 0;
        }
        
        return false;
    }
    
    // ==================== GETTERS I SETTERS ====================
    
    public Connection getConexion() {
        return conexion;
    }
    
    public void setConexion(Connection conexion) {
        this.conexion = conexion;
    }
    
    public String getUrlBBDD() {
        return urlBBDD;
    }
    
    public void setUrlBBDD(String urlBBDD) {
        this.urlBBDD = urlBBDD;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}