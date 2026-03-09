package JOC_DEL_PINGU;

import java.sql.Connection;

public class GestorBBDD {
    private String urlBBDD;
    private String username;
    private String password;
    
    // Guardamos la conexión aquí para usarla en todos los métodos
    private Connection conexion; 

    // Constructor temporal (o método para iniciar la conexión)
    public void iniciarConexion(java.util.Scanner scan) {
        // Llamamos a tu archivo bbdd.java para conectarnos
        this.conexion = BBDD.conectarBaseDatos(scan);
    }

    // Método de tu UML
    public void guardarBBDD(Partida p) {
        
        // 1. Extraemos los datos del objeto Partida (y sus jugadores, tablero...)
        // int idTablero = p.getTablero().getId(); // Ejemplo inventado
        
        // 2. Preparamos la sentencia SQL
        String sql = "INSERT INTO PARTIDA (id_partida, estado, id_tablero) VALUES (1, 'En curso', 1)";
        
        // 3. Usamos tu archivo bbdd.java para ejecutar el insert en Oracle
        int filasAfectadas = BBDD.insert(this.conexion, sql);
        
        if (filasAfectadas > 0) {
            System.out.println("¡Partida guardada con éxito!");
        }
    }
    
    // Método de tu UML
    public Tablero cargarBBDD(int id) {
        // Aquí harías un BBDD.select(...) para buscar la partida
        // Leerías los datos que te devuelve Oracle
        // Y construirías un objeto Tablero nuevo para devolverlo
        return null; // Devolvemos null temporalmente
    }
}