package abstraction.eqXRomu.acteurs;


import abstraction.eqXRomu.bourseCacao.IAcheteurBourse;
import abstraction.eqXRomu.produits.Feve;

public class TransformateurXAcheteurBourse extends TransformateurXActeur implements IAcheteurBourse {

	public double demande(Feve f, double cours) {
		if (this.stockFeves.get(f)<20000) {
			return Math.max(20000-this.stockFeves.get(f),  10); // on n'achete jamais moins de 10T
		}
		return 0;
	}

	public void notificationAchat(Feve f, double quantiteEnT, double coursEnEuroParT) {
		this.stockFeves.put(f, this.stockFeves.get(f)+quantiteEnT);
		this.totalStocksFeves.ajouter(this, quantiteEnT, cryptogramme);
	}


	public void notificationBlackList(int dureeEnStep) {
	}

}
