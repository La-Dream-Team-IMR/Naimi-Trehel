import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

public class ControleurNaimiTrehel extends UnicastRemoteObject implements ControleurInterface {

    int id;
    int dernier, suivant = 0;
    boolean demande, jeton = false;
    BlockingQueue<StateQueue> queue;
    private Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
    HashMap<Integer, ControleurInterface> map;
    String color;

    public ControleurNaimiTrehel(int id, int idPere, BlockingQueue<StateQueue> queue, String color) throws Exception {
        this.id = id;
        this.dernier = idPere;
        this.queue = queue;
        this.color = color;
        map = new HashMap<Integer, ControleurInterface>();

        if (id == 1) {
            jeton = true;
        }

        Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
        registry.rebind("rmi://localhost:1099/P" + id, this);
    }

    @Override
    public void run() throws RemoteException {
        while (true) {
            try {
                // ConsoleUtils.debug("P" + id + " : Attend un évenement", color);
                StateQueue state = queue.take();
                ConsoleUtils.debug("P" + id + " : Analyse de l'évenement : " + state, color);
                switch (state) {
                    case Demander:
                        demanderSectionCritique();
                        break;

                    case Quitter:
                        quitterSectionCritique();
                        break;

                    default:
                        queue.put(state);
                        // TODO FIX
                        ConsoleUtils.debug("P" + id + " : " + state, color);
                        // throw new Exception("P" + id + " : On ne peut pas utiliser son propre
                        // message");
                }
            } catch (Exception e) {
                ConsoleUtils.debug("P" + id +" : ",color);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void demanderSectionCritique() throws RemoteException {
        if (!demande) {
            if (dernier != 0) {
                get(dernier).dem_SC(id);
                dernier = 0;
            } else if (dernier == 0 && jeton) {
                signalerAutorisation();
            }

        } else if (dernier == 0 && !jeton) {
            // Rien faire
        }
        demande = true;
    }

    @Override
    public void signalerAutorisation() throws RemoteException {
        try {
            queue.put(StateQueue.Autoriser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
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
                queue.put(StateQueue.Ack);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            demande = false;
        }
    }

    @Override
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

    @Override
    public void jeton() throws RemoteException {
        jeton = true;
        ConsoleUtils.debug("P" + this.id + " : J'ai le jeton !!", color);
        signalerAutorisation();
    }

    @Override
    public void enregistrerControleur(String urlDistant) throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public void oublierControleur(String urlDistant) throws RemoteException {
        // TODO Auto-generated method stub

    }

    private ControleurInterface get(int id) throws RemoteException {
        if (!map.containsKey(id)) {
            try {
                ControleurInterface P = (ControleurInterface) registry.lookup("rmi://localhost:1099/P" + id);
                map.put(id, P);
            } catch (NotBoundException e) {
                return null;
            }
        }
        return map.get(id);
    }
}