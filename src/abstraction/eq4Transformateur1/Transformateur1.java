package abstraction.eq4Transformateur1;

import abstraction.eqXRomu.produits.IProduit;

/**@author Safta Yassine */ 
public class Transformateur1 extends Transformateur1AcheteurBourse  {
	
	public Transformateur1() {
		super();
	}

	/**@author Ewan Lefort */

	public double getQuantiteEnStock(IProduit p, int cryptogramme) {
		if (this.cryptogramme==cryptogramme) { // c'est donc bien un acteur assermente qui demande a consulter la quantite en stock
			return this.getStocksProduit(p); // A modifier
		} else {
			return 0; // Les acteurs non assermentes n'ont pas a connaitre notre stock
		}
	}
}
