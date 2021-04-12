public class ControleurNaimiTrehel extends Thread implements ControleurInterface {

    int id;
    int dernier, suivant = 0;
    boolean demande, jeton = false;

    public ControleurNaimiTrehel(int id) {
        this.id = id;
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
                P[dernier].demanderSectionCritique();
                dernier = 0;
            } else if (dernier == 0 && jeton) {
                A[i].signalerAutorisation();
            } else if (dernier == 0 && !jeton) {
                // Rien faire
            }
            demande = true;
        }
    }

    @Override
    public void signalerAutorisation() {
        // TODO Auto-generated method stub
        System.out.println("J'ai le jeton");
    }

    @Override
    public void quitterSectionCritique() {
        if(demande && jeton) {
            if(suivant != 0){
                P[suivant].jeton();
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
                    P[j].jeton();
                    jeton = false;
                } else if (demande) {
                    suivant = j;
                }
                break;
        
            default:
                P[dernier].dem_SC(j);
                break;
        }
        dernier = j;
    }

    @Override
    public void jeton() {
        jeton = vrai;
        A[i].signalerAutorisation();
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