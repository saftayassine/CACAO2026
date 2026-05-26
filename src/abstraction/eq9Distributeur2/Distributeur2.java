package abstraction.eq9Distributeur2;

import abstraction.eq9Distributeur2.Achats.Distributeur2AcheteurCC;
import abstraction.eqXRomu.appelDOffre.IAcheteurAO;

/**
 * Classe principale du Distributeur2
 * @author Paul Rossignol
 */
public class Distributeur2 extends Distributeur2AcheteurCC implements IAcheteurAO {

	public Distributeur2() {
		super();
	}

	/**
	 * Étape du distributeur : gestion des stocks, CC, AO
	 */
	@Override
	public void next() {
		super.next();

		faireUnAppelDOffre();

		journal.ajouter("Distributeur2 - Next complet CC+AO");
	}
}
