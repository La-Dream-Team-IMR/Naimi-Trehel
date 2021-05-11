import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

public class GestionAction extends Thread {

    int id;
    int dernier, suivant = 0;
    boolean demande, jeton = false;
    String color;
    Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
    HashMap<Integer, ControleurInterface> map;
    ControleurNaimiTrehel controleur;
    BlockingQueue<ActionEnum> actions;
    BlockingQueue<Integer> anciens;

    public GestionAction(ControleurNaimiTrehel P, int id, int idPere, String color, BlockingQueue<ActionEnum> actions,
            BlockingQueue<Integer> anciens) throws Exception {
        this.id = id;
        this.dernier = idPere;
        this.color = color;
        map = new HashMap<Integer, ControleurInterface>();
        this.actions = actions;
        this.anciens = anciens;
        this.controleur = P;

        if (id == 1) {
            jeton = true;
        }
    }

    @Override
    public void run() {
        while (true) {
            ActionEnum action;
            try {
                action = actions.take();

                switch (action) {
                    case dem_SC:
                        int ancien;
                        ancien = anciens.take();
                        dem_SC(ancien);
                        break;
                    case demanderSectionCritique:
                        demanderSectionCritique();
                        break;
                    case jeton:
                        jeton();
                        break;
                    case quitterSectionCritique:
                        quitterSectionCritique();
                        break;
                    default:
                        break;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void demanderSectionCritique() throws RemoteException {
        if (!demande) {
            if (dernier != 0) {
                get(dernier).dem_SC(id);
                dernier = 0;
            } else if (dernier == 0 && jeton) {
                controleur.signalerAutorisation();
            } else if (dernier == 0 && !jeton) {
                // Rien faire
            }
            demande = true;
        }
    }

    /*
     * public void signalerAutorisation() { try { queue.put(StateQueue.Autoriser); }
     * catch (Exception e) { e.printStackTrace(); } }
     */

    public void quitterSectionCritique() throws RemoteException {
        if (demande && jeton) {
            if (suivant != 0) {
                jeton = false;
                get(suivant).jeton();
                suivant = 0;
            } else if (suivant == 0) {
                // Rien Faire
            }
            try {
                controleur.queue.put(StateQueue.Ack);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            demande = false;
        }
    }

    public void dem_SC(int j) throws RemoteException {
        switch (dernier) {
            case 0:
                if (!demande) {
                    jeton = false;
                    get(j).jeton();
                } else if (demande) {
                    suivant = j;
                }
                break;

            default:
                get(dernier).dem_SC(j);
                break;
        }
        dernier = j;
    }

    public void jeton() throws RemoteException {
        jeton = true;
        ConsoleUtils.debug("P" + this.id + " : J'ai le jeton !!", color);
        controleur.signalerAutorisation();
    }

    private ControleurInterface get(int id) {
        if (!map.containsKey(id)) {
            try {
                ControleurInterface P = (ControleurInterface) registry.lookup("rmi://localhost:1099/P" + id);
                map.put(id, P);
            } catch (Exception e) {
                return null;
            }
        }
        return map.get(id);
    }
}
