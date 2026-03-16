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

        // 1. Llamamos a tu clase BBDD para intentar conectar
        conexion = BBDD.conectarBaseDatos(scan);

        // 2. Comprobamos si la conexión ha sido exitosa
        if (conexion != null) {
            System.out.println("Arrancando el juego...");
            jugar();
        } else {
            System.out.println("No se ha podido conectar a la BBDD.");
        }

        // COMENTADO: No cerramos el scanner para que el juego pueda seguir leyendo por consola
        // scan.close(); 
        
        // 4. Cerramos la conexión cuando el programa acabe (después de que termine jugar())
        // (Asegúrate de que tu archivo bbdd.java tenga un método cerrar, si no, usa conexion.close() dentro de un try-catch)
        // BBDD.cerrar(conexion);
    }

    public static void jugar() {
    	
        // Según tu diagrama, el Main (Controlador) se comunica directamente con PantallaMenu (Vista)
        System.out.println("Cargando el menú principal...");
        
        // --- PRUEBA DEL GESTOR ---
        // Como ahora la conexión la has hecho en el Main, para usar el GestorBBDD tendrías
        // que pasarle esta conexión de alguna forma. Por ejemplo, si creas un constructor en el gestor:
        // GestorBBDD gestor = new GestorBBDD(conexion);
        
        // Aquí instanciarías tu vista inicial para que el usuario empiece a interactuar. 
        // PantallaMenu menu = new PantallaMenu();
        // menu.menu();
    }
}

