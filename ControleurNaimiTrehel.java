import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.BlockingQueue;

public class ControleurNaimiTrehel extends UnicastRemoteObject implements ControleurInterface {

    int id;
    int dernier, suivant = 0;
    boolean demande, jeton = false;
    BlockingQueue<StateQueue> queue;
    private Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
    String color;

    public ControleurNaimiTrehel(int id, int idPere, BlockingQueue<StateQueue> queue, String color) throws Exception {
        this.id = id;
        this.dernier = idPere;
        this.queue = queue;
        this.color = color;

        if (id == 1) {
            jeton = true;
        }

        Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
        registry.bind("rmi://localhost:1099/P" + id, this);
    }

    @Override
    public void run() throws RemoteException {
        while (true) {
            try {
                System.out.println("P" + id + " : Attend un évenement");
                StateQueue state = queue.take();
                System.out.println("P" + id + " : Analyse de l'évenement");
                switch (state) {
                case Demander:
                    demanderSectionCritique();
                    break;

                case Quitter:
                    quitterSectionCritique();
                    break;
                default:
                    //queue.put(state);
                    //TODO FIX
                    System.out.println(color + "P" + id + " : " + state + ConsoleColors.RESET);
                    throw new Exception("On ne peut pas utiliser son propre message");
                }
            } catch (Exception e) {
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
            }
            signalerAutorisation();
        } else if (dernier == 0 && !jeton) {
            // Rien faire
        }
        demande = true;
    }

    @Override
    public void signalerAutorisation() throws RemoteException {
        // Remplir BlockingQueue
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
                get(suivant).jeton();
                suivant = 0;
                jeton = false;
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
                get(j).jeton();
                jeton = false;
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
        System.out.println(this.id + " : J'ai le jeton !!");
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
        ControleurInterface P;
        try {
            P = (ControleurInterface) registry.lookup("rmi://localhost:1099/P" + id);
        } catch (NotBoundException e) {
            return null;
        }
        return P;
    }
}