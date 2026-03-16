package JOC_DEL_PINGU;

import java.sql.Connection;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class GestorBBDD {
    // Atributos
    private String urlBBDD;
    private String username;
    private String password;
    
    // Guardamos la conexión aquí para usarla en todos los métodos de esta clase
    private Connection conexion; 

    // Constructor vacío
    public GestorBBDD() {
    }

    // Método para iniciar la conexión
    public void iniciarConexion(Scanner scan) {
      
        this.conexion = BBDD.conectarBaseDatos(scan);
    }
    

    public void cerrarConexion() {
        BBDD.cerrar(this.conexion);
    }

  
    public void guardarBBDD(Partida p) {
      //Comprobamos si hay conexión antes de hacer nada
        if (this.conexion == null) {
            System.out.println("No hay conexión a la BBDD. Imposible guardar la partida.");
            return; 
        }
        
     
        

        String sql = "INSERT INTO PARTIDA (id_partida, estado, id_tablero) VALUES (1, 'En curso', 1)";
        
        // 3. Usamos tu archivo bbdd.java para ejecutar el insert en Oracle
        int filasAfectadas = BBDD.insert(this.conexion, sql);
        
        if (filasAfectadas > 0) {
            System.out.println("¡Partida guardada con éxito en Oracle!");
        } else {
            System.out.println("Algo ha fallado al hacer el INSERT en la BBDD.");
        }
    }
    
    // Método de UML
    public Tablero cargarBBDD(int id) {
        if (this.conexion == null) {
            System.out.println("No hay conexión, no se puede cargar el tablero.");
            return null; 
        }

      
      
        
        return null;
    }
}