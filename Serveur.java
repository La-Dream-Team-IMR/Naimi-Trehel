import java.rmi.registry.*;
import java.util.ArrayList;
import java.util.List;

public class Serveur {
    public static void main(String[] args) throws Exception{
        Registry registry = LocateRegistry.createRegistry(1099);
        List<ControleurInterface> Processus = new ArrayList<ControleurInterface>();
        ControleurNaimiTrehel P0 = new ControleurNaimiTrehel(0, Processus);
        ControleurNaimiTrehel P1 = new ControleurNaimiTrehel(1, Processus);
        Processus.add(P0);
        Processus.add(P1);
        registry.bind("rmi://localhost:1099/P0", P0);
        registry.bind("rmi://localhost:1099/P1", P1);
    }
}
