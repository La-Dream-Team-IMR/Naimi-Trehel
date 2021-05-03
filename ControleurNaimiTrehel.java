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
    private String hostname = "127.0.0.1";
    private int port = 1099;
    private String url;
    private Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
    String color;
    HashMap<Integer, String> mapIdToURl = new HashMap<Integer, String>();

    public ControleurNaimiTrehel(int id, String hostname, int port, String urlPere, BlockingQueue<StateQueue> queue,
            String color) throws Exception {

        this.id = id;
        this.queue = queue;
        this.color = color;
        this.hostname = hostname;
        this.port = port;

        if (id == 1) {
            jeton = true;
        }

        this.url = "rmi://" + hostname + ":" + port + "/P" + id;
        registry = LocateRegistry.getRegistry(this.hostname, this.port);
        registry.rebind(this.url, this);

        if (!urlPere.equals("0")) {
            int startIndex = urlPere.lastIndexOf('P') + 1;
            int lastIndex = urlPere.length();
            this.dernier = Integer.parseInt(urlPere.substring(startIndex, lastIndex));
            mapIdToURl.put(dernier, urlPere);
            get(dernier).enregistrerControleur(this.url);
        } else {
            this.dernier = 0;
        }
    }

    public ControleurNaimiTrehel(int id, int idPere, BlockingQueue<StateQueue> queue, String color) throws Exception {
        this.id = id;
        this.dernier = idPere;
        this.queue = queue;
        this.color = color;

        if (id == 1) {
            jeton = true;
        }

        registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
        registry.bind("rmi://localhost:1099/P" + id, this);
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
                        ConsoleUtils.debug(color + "P" + id + " : " + state, color);
                        // throw new Exception("P" + id + " : On ne peut pas utiliser son propre
                        // message");
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
                ConsoleUtils.debug(mapIdToURl +"", color);
                ConsoleUtils.debug(dernier + " : " + id, color);
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
        ConsoleUtils.debug("DemSC : " + dernier + " " + j, color);
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
        ConsoleUtils.debug("Ajout d'un nouveau controlleur : " + urlDistant, color);
        int id = Integer.parseInt(urlDistant.substring(urlDistant.lastIndexOf('P') + 1, urlDistant.length()));
        if (!this.mapIdToURl.containsKey(id)) {
            this.mapIdToURl.put(id, urlDistant);
            int last = dernier;
            if (last != 0) {
                get(last).enregistrerControleur(urlDistant);
            } else {
                for (int i : mapIdToURl.keySet()) {
                    get(i).enregistrerControleur(urlDistant);
                }
            }
        }

    }

    @Override
    public void oublierControleur(String urlDistant) throws RemoteException {
        // TODO Auto-generated method stub

    }

    private ControleurInterface get(int id) throws RemoteException {
        ControleurInterface P;
        String url = mapIdToURl.get(id);
        try {
            P = (ControleurInterface) registry.lookup(url);
        } catch (NotBoundException e) {
            return null;
        }
        return P;
    }
}