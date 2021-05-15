import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface definissant les fonctionnalites attendues d'un controleur pour le projet de repartiteur laser
 */
public interface ControleurInterface extends Remote {
	
	/**
	 * Receptionne du demande d'entree en section critique de la part du processus metier
	 */
	void demanderSectionCritique() throws RemoteException;
	
	/**
	 * Signale l'autorisation d'entrer en section critique aupres du processus metier
	 */
	void signalerAutorisation() throws RemoteException;
	
	/**
	 * Receptionne la notification du processus metier a sa sortie de la section critique
	 */
	void quitterSectionCritique() throws RemoteException;

	/**
	 * Receptionne la notification d'un autre controleur de son besoin de la ressource partagée
	 */
	void dem_SC(int j) throws RemoteException;

	/**
	 * Receptionne le jeton d'accès à la ressource partagée
	 */
	void jeton() throws RemoteException;
	
	/**
	 * Enregistre l'URL d'un controleur distant
	 * @param urlDistant l'URL a memoriser
	 */
	void enregistrerControleur(String urlDistant) throws RemoteException;
	
	/**
	 * Oublie l'URL d'un controleur distant
	 * @param urlDistant l'URL a oublier
	 */
	void oublierControleur(String urlDistant) throws RemoteException;

	void run() throws RemoteException;
	
}
