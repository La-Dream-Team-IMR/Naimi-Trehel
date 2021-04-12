/**
 * Interface definissant les fonctionnalites attendues d'un controleur pour le projet de repartiteur laser
 */
public interface ControleurInterface {
	
	/**
	 * Receptionne du demande d'entree en section critique de la part du processus metier
	 */
	void demanderSectionCritique();
	
	/**
	 * Signale l'autorisation d'entrer en section critique aupres du processus metier
	 */
	void signalerAutorisation();
	
	/**
	 * Receptionne la notification du processus metier a sa sortie de la section critique
	 */
	void quitterSectionCritique();

	/**
	 * Receptionne la notification d'un autre controleur de son besoin de la ressource partagée
	 */

	void dem_SC(int j);
	/**
	 * Receptionne le jeton d'accès à la ressource partagée
	 */
	void jeton();
	
	/**
	 * Enregistre l'URL d'un controleur distant
	 * @param urlDistant l'URL a memoriser
	 */
	void enregistrerControleur(String urlDistant);
	
	/**
	 * Oublie l'URL d'un controleur distant
	 * @param urlDistant l'URL a oublier
	 */
	void oublierControleur(String urlDistant);
	
}
