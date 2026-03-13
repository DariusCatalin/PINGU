package JOC_DEL_PINGU;

import java.sql.Connection;
import java.util.Scanner;

public class main_prueba {

    // Guardamos la conexión por si luego el GestorBBDD la necesita
    public static Connection conexion;

    public static void main(String[] args) {
        // 1. Abrimos el Scanner UNA SOLA VEZ para todo el programa
        Scanner scan = new Scanner(System.in);

        System.out.println("========================================");
        System.out.println("   🐧 PINGÜINOS VS FOCAS - EL JUEGO 🦭   ");
        System.out.println("========================================");

        // 2. Conectamos a la Base de Datos Oracle
        conexion = BBDD.conectarBaseDatos(scan);

        // 3. Comprobamos si la conexión ha ido bien
        if (conexion != null) {
            System.out.println("\n[OK] Conexión a Oracle establecida con éxito.");
            System.out.println("Arrancando el motor del juego...\n");
            
            // 4. Vamos al método jugar() para lanzar el menú
            jugar(scan);
            
        } else {
            System.out.println("\n[ERROR] Bro, no se ha podido conectar a la BBDD.");
            System.out.println("Comprueba la VPN o las credenciales e inténtalo de nuevo.");
        }

        // 5. Cuando salgamos del menú (opción 3), cerramos la conexión para ser limpios
        if (conexion != null) {
            System.out.println("Cerrando la conexión con la base de datos...");
            BBDD.cerrar(conexion);
        }
        
        System.out.println("Programa terminado. ¡Chao tt!");
    }

    // Le pasamos el scanner por parámetro para no crear uno nuevo
    public static void jugar(Scanner scan) {
        
        // Instanciamos la vista del menú que creamos antes
        PantallaMenu menuPrincipal = new PantallaMenu();
        
        // Llamamos al método que pinta el menú y tiene el bucle (switch)
        menuPrincipal.menu();
    }
}
