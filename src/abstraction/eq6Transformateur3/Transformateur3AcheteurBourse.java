package abstraction.eq6Transformateur3;
package abstraction.eqXRomu.bourseCacao;
import abstraction.eq6Transformateur3.StockChocoMarque;
import abstraction.eq6Transformateur3.StockChocolat;
import abstraction.eq6Transformateur3.StockFeve;
import abstraction.eq6Transformateur3.Transformateur3Acteur;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Feve;


public class Transformateur3AcheteurBourse {


	private double achatMaxParStep;

	public Transformateur3AcheteurBourse() {
		stock;
	}

	public double demande(Feve f, double cours) {
		if (this.getFeve().equals(f)) {
			BourseCacao bourse = (BourseCacao)(Filiere.LA_FILIERE.getActeur("BourseCacao"));
			double pourcentage = (bourse.getCours(getFeve()).getMax()-bourse.getCours(getFeve()).getValeur())/(bourse.getCours(getFeve()).getMax()-bourse.getCours(getFeve()).getMin());
			return achatMaxParStep*pourcentage;
		} else {
			return 0.0;
		}
	}

	public void notificationAchat(Feve f, double quantiteEnT, double coursEnEuroParT) {
		this.stockFeve.setValeur(this, this.stockFeve.getValeur()+quantiteEnT);
	}

	public void notificationBlackList(int dureeEnStep) {
		this.journal.ajouter("Aie... je suis blackliste... j'aurais du verifier que j'avais assez d'argent avant de passer une trop grosse commande en bourse...");
	}


}
