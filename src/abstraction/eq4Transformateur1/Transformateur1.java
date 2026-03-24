package abstraction.eq4Transformateur1;

import java.util.ArrayList;
import java.util.List;

import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.general.VariablePrivee;
import abstraction.eqXRomu.produits.IProduit;

/**@author Safta Yassine */ 
public class Transformateur1 extends Transformateur1AcheteurBourse  {
	/** @author Ewan Lefort */
	VariablePrivee totalstocks= new VariablePrivee("EQ4T Total Stocks", "<html>Quantite totale de feves en stock</html>", this,0);

	/** @author Safta Yassine */
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
	public void next(){
		super.next();
		this.totalstocks.setValeur(this, this.getTotalStocks(), cryptogramme);

	}

	public List<Variable> getIndicateurs() {
		List<Variable> res = new ArrayList<Variable>();
		res.add(totalstocks);
		return res;
	}
}