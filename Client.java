import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    public static void main(String[] args) throws Exception {
        Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
        ControleurInterface P = (ControleurInterface) registry.lookup("rmi://localhost:1099/P0");
        P.demanderSectionCritique();
    }
}
