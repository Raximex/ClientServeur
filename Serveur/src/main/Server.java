package Serveur;

import java.rmi.RMISecurityManager;

public class Server {
    public static void main(String[] args) {
        try {
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new RMISecurityManager());
            }

            InformationImpl informationImpl = new InformationImpl();
        } catch (Exception e) {
            e.printStrackTrace();
        }
    }
}
