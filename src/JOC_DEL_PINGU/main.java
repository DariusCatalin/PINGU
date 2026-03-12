package JOC_DEL_PINGU;

import java.sql.Connection;
import java.util.Scanner;

public class main {

    // Variable estática para guardar la conexión y poder pasarla al resto del programa
    public static Connection conexion;

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);

        System.out.println("--- INICIANDO PINGÜINOS VS FOCAS ---");

        // 1. Llamamos a tu clase BBDD para intentar conectar
        conexion = BBDD.conectarBaseDatos(scan);

        // 2. Comprobamos si la conexión ha sido exitosa
        if (conexion != null) {
            System.out.println("¡Conexión de locos, tt! Arrancando el juego...");
            jugar();
        } else {
            System.out.println("Bro, no se ha podido conectar a la BBDD. Revisa los datos o si la VPN está puesta.");
        }

        // 3. Cerramos el scanner para que no haya fugas de memoria
        scan.close();
        
        // 4. Cerramos la conexión cuando el programa acabe
        BBDD.cerrar(conexion);
    }

    public static void jugar() {
        // Según tu diagrama, el Main (Controlador) se comunica directamente con PantallaMenu (Vista)
        System.out.println("Cargando el menú principal...");
        
        // Aquí instanciarías tu vista inicial para que el usuario empiece a interactuar. 
        // Sería algo así cuando la tengas creada:
        // PantallaMenu menu = new PantallaMenu();
        // menu.menu();
    }
}