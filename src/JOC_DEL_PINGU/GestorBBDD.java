package JOC_DEL_PINGU;

import java.sql.Connection;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class GestorBBDD {
    // Atributos de tu UML. (Nota: bbdd.java los pide por consola, los dejamos para cumplir con el diagrama)
    private String urlBBDD;
    private String username;
    private String password;
    
    // Guardamos la conexión aquí para usarla en todos los métodos de esta clase
    private Connection conexion; 

    // Constructor vacío (opcional, pero buena práctica)
    public GestorBBDD() {
    }

    // Método para iniciar la conexión
    public void iniciarConexion(Scanner scan) {
        // Llamamos a tu archivo bbdd.java para conectarnos y guardamos la sesión
        this.conexion = BBDD.conectarBaseDatos(scan);
    }
    
    // Método extra para cerrar la conexión limpiamente
    public void cerrarConexion() {
        BBDD.cerrar(this.conexion);
    }

    // Método de tu UML
    public void guardarBBDD(Partida p) {
        // 0. ¡SEGURIDAD! Comprobamos si hay conexión antes de hacer nada
        if (this.conexion == null) {
            System.out.println("Cuidado tt, no hay conexión a la BBDD. Imposible guardar la partida.");
            return; // Salimos del método para que no pete
        }
        
        // 1. Extraemos los datos del objeto Partida (y sus jugadores, tablero...)
        // Ejemplo: int idTablero = p.getTablero().getId(); 
        
        // 2. Preparamos la sentencia SQL (con datos a fuego de momento para probar)
        String sql = "INSERT INTO PARTIDA (id_partida, estado, id_tablero) VALUES (1, 'En curso', 1)";
        
        // 3. Usamos tu archivo bbdd.java para ejecutar el insert en Oracle
        int filasAfectadas = BBDD.insert(this.conexion, sql);
        
        if (filasAfectadas > 0) {
            System.out.println("¡Partida guardada con éxito en Oracle!");
        } else {
            System.out.println("Bro, algo ha fallado al hacer el INSERT en la BBDD.");
        }
    }
    
    // Método de tu UML
    public Tablero cargarBBDD(int id) {
        if (this.conexion == null) {
            System.out.println("No hay conexión, no se puede cargar el tablero.");
            return null; 
        }

        // Aquí tienes el ejemplo de cómo se usaría el select de tu BBDD
        // String sql = "SELECT * FROM TABLERO WHERE id_tablero = " + id;
        
        // Guardamos el resultado en la lista de HashMaps como pide tu clase BBDD
        // ArrayList<LinkedHashMap<String, String>> datos = BBDD.select(this.conexion, sql);
        
        // Iterarías sobre 'datos' para crear un nuevo objeto Tablero y devolverlo
        
        return null; // Devolvemos null temporalmente hasta que programes la lógica
    }
}