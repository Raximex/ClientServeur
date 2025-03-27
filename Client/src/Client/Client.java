package Client;
import java.rmi.registry.LocateRegistry; 
import java.rmi.registry.Registry;  

public class Client {
    
    private Client(){}

    public static void main(String[] args) {
        try {  
            // Récupérer le registre
            Registry reg = LocateRegistry.getRegistry(null); 
       
            // Recherche dans le registre de l'objet distant
            IBricoMerlinServices stub = (IBricoMerlinServices) reg.lookup("IBricoMerlinServices"); 
       
            // Front client pour lancer les appels des méthodes distantes à l'aide de l'objet obtenu
            //stub. 

         } catch (Exception e) {
            System.err.println(e.toString()); 
            e.printStackTrace(); 
         } 
    }
}
