package JOC_DEL_PINGU;

public class main {
    public static void main(String[] args) {
        main controladorPrincipal = new main();
        
        //LLAMAMOS A JUGAR
        controladorPrincipal.jugar();
    }

    public void jugar() {
        //INICIALIZAMOS LA PANTALLA DEL MENÚ
        PantallaMenu pantallaMenu = new PantallaMenu();
        
        //MENÚ PARA MOSTRAR SI QUEREMOS JUGAR UNA NUEVA PARTIDA, CARGAR UNA O SALIRNOS
        pantallaMenu.menu(); 
    }
}
