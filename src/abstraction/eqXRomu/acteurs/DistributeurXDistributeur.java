package abstraction.eqXRomu.acteurs;

import abstraction.eqXRomu.clients.ClientFinal;
import abstraction.eqXRomu.filiere.IDistributeurChocolatDeMarque;
import abstraction.eqXRomu.produits.ChocolatDeMarque;

public class DistributeurXDistributeur extends DistributeurXActeur implements IDistributeurChocolatDeMarque {

	public double prix(ChocolatDeMarque choco) {
		switch (choco.getChocolat()) {
		case C_HQ_E: return 26000;
		case C_HQ : return 22000;
		case C_MQ_E:return 18000;
		case C_MQ :return 16000;
		case C_BQ_E :return 14000;
		case C_BQ : return 12000;
		default:
			return 0.0;
		}

	}

	public double quantiteEnVente(ChocolatDeMarque choco, int crypto) {
		if (this.cryptogramme==crypto && this.stockChocoMarque.keySet().contains(choco)) {
			return this.stockChocoMarque.get(choco)/10;
		} else {
			return 0.0;
		}
	}

	public double quantiteEnVenteTG(ChocolatDeMarque choco, int crypto) {
		return quantiteEnVente(choco,crypto)/10.0;
	}

	public void vendre(ClientFinal client, ChocolatDeMarque choco, double quantite, double montant, int crypto) {
		this.stockChocoMarque.put(choco, this.stockChocoMarque.get(choco)-quantite);
		this.totalStocksChocoMarque.retirer(this,  quantite, cryptogramme);
		
	}

	public void notificationRayonVide(ChocolatDeMarque choco, int crypto) {
	}

}
