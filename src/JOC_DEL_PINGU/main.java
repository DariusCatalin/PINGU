package JOC_DEL_PINGU;

import java.sql.Connection;
import java.util.Scanner;

public class main {

	// Variable estática para guardar la conexión y poder pasarla al resto del programa
    public static Connection conexion;

    public static void main(String[] args) {
        // Creamos el Scanner una única vez para todo el programa
        Scanner scan = new Scanner(System.in);

        System.out.println("--- INICIANDO PINGÜINOS VS FOCAS ---");

        // 1. Llamamos a la clase BBDD para intentar conectar
        conexion = BBDD.conectarBaseDatos(scan);

        // 2. Comprobamos si la conexión ha sido exitosa
        if (conexion != null) {
            System.out.println("Arrancando el juego...");
            jugar();
        } else {
            System.out.println("No se ha podido conectar a la BBDD.");
        }

   
    }

    public static void jugar() {
    
        System.out.println("Cargando el menú principal...");
      
        
    }
}

