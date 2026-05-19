/**@author Yassine Safta */
package abstraction.eq4Transformateur1;
import abstraction.eqXRomu.appelDOffre.IAcheteurAO;
import abstraction.eqXRomu.appelDOffre.OffreVente;
import abstraction.eqXRomu.appelDOffre.SuperviseurVentesAO;
import abstraction.eqXRomu.bourseCacao.BourseCacao;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;

import java.util.List;
public class Transformateur1AcheteurAppelDOffre extends Transformateur1VendeurAppelDOffre implements IAcheteurAO {
	private SuperviseurVentesAO supAO;

	public Transformateur1AcheteurAppelDOffre() {
		super();
	}
	public void initialiser() {
		super.initialiser();
		this.supAO = (SuperviseurVentesAO)(Filiere.LA_FILIERE.getActeur("Sup.AO"));
	}

	public void next() {
		super.next();
		this.journalAO.ajouter("=== STEP "+Filiere.LA_FILIERE.getEtape()+" ====================");
		for (IProduit f : this.getStock().keySet()) {
			if (f instanceof Feve && f!=Feve.F_MQ_E && this.getStocksPrevuProduit(this.getChoco(f)) < 30000) { 
				double quantite = 30000-this.getStocksPrevuProduit(this.getChoco(f)); 
				OffreVente ov = supAO.acheterParAO(this,  cryptogramme, f, quantite);
				journalAO.ajouter("   Je lance un appel d'offre de "+quantite+" T de "+f);
				if (ov!=null) { // on a retenu l'une des offres de vente
					journalAO.ajouter("   AO finalise : on ajoute "+quantite+" T de "+f+" au stock");
					this.getStock().put(f, this.getStock().get(f)+quantite);
				}
			}
		}

		// On archive les contrats termines
		this.journalAO.ajouter("=================================");
	}


	/**@author Ewan Lefort */
	public OffreVente choisirOV(List<OffreVente> propositions) {
		BourseCacao bourse = (BourseCacao)(Filiere.LA_FILIERE.getActeur("BourseCacao"));
		for (OffreVente ov : propositions) {
			
		double cours = ( bourse.getCours((Feve)ov.getProduit())).getValeur();
		if (ov.getPrixT()<=0.9*cours) {
			return ov;
		}
		}
		return null;
	}

}