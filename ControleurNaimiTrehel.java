import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class ControleurNaimiTrehel extends UnicastRemoteObject implements ControleurInterface {

    int id;
    int dernier, suivant = 0;
    boolean demande, jeton = false;
    List<ControleurInterface> P;

    public ControleurNaimiTrehel(int id, List<ControleurInterface> Processus) throws Exception {
        this.id = id;
        this.P = Processus;
        if(id == 1) {
            dernier = 0;
            jeton = true;
        } else {
            dernier = 1;
            jeton = false;
        }
    }

    @Override
    public void demanderSectionCritique() {
        if(!demande){
            if(dernier != 0) {
                P.get(dernier).demanderSectionCritique();
                dernier = 0;
            } else if (dernier == 0 && jeton) {
                A[id].signalerAutorisation();
            } else if (dernier == 0 && !jeton) {
                // Rien faire
            }
            demande = true;
        }
    }

    @Override
    public void signalerAutorisation() {
        // TODO Auto-generated method stub
        System.out.println("J'ai le jeton !!");
    }

    @Override
    public void quitterSectionCritique() {
        if(demande && jeton) {
            if(suivant != 0){
                P.get(suivant).jeton();
                suivant = 0;
                jeton = false;
            } else if (suivant == 0) {
                // Rien Faire
            }
            demande = false;
        }
    }

    @Override
    public void dem_SC(int j) {
        switch (dernier) {
            case 0:
                if(!demande) {
                    P.get(suivant).jeton();
                    jeton = false;
                } else if (demande) {
                    suivant = j;
                }
                break;
        
            default:
                P.get(dernier).dem_SC(j);
                break;
        }
        dernier = j;
    }

    @Override
    public void jeton() {
        jeton = true;
        A[id].signalerAutorisation();
    }

    @Override
    public void enregistrerControleur(String urlDistant) {
        // TODO Auto-generated method stub

    }

    @Override
    public void oublierControleur(String urlDistant) {
        // TODO Auto-generated method stub

    }
}