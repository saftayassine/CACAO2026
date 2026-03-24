package abstraction.eq9Distributeur2;

public class Distributeur2 extends Distributeur2Acteur  {

	public Distributeur2() {
		super();
	}

	/** @author Paul Juhel */
	public void next() {
		super.next();
		this.getJournaux().get(0).ajouter("+100 t en rayon");
	}
}
