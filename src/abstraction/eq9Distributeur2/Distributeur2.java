package abstraction.eq9Distributeur2;

/**
 * Classe principale du Distributeur2
 * @author Paul Juhel
 */
public class Distributeur2 extends Distributeur2AcheteurAO {

	public Distributeur2() {
		super();
	}

	/**
	 * Étape du distributeur : gestion des stocks et achats automatiques
	 * @author Paul Juhel
	 */
	@Override
	public void next() {
		super.next();

		// Effectuer les achats via appels d'offres automatiquement
		this.faireUnAppelDOffre();

		journal.ajouter("Distributeur2 - Gestion des rayons et achats terminés");
	}
}
