package abstraction.eqXRomu.acteurs;
import abstraction.eqXRomu.appelDOffre.IAcheteurAO;
import abstraction.eqXRomu.appelDOffre.OffreVente;
import abstraction.eqXRomu.appelDOffre.SuperviseurVentesAO;
import abstraction.eqXRomu.bourseCacao.BourseCacao;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Feve;
import java.util.List;
public class TransformateurXAcheteurAppelDOffre extends TransformateurXVendeurAppelDOffre implements IAcheteurAO {
	private SuperviseurVentesAO supAO;

	public TransformateurXAcheteurAppelDOffre() {
		super();
	}
	public void initialiser() {
		super.initialiser();
		this.supAO = (SuperviseurVentesAO)(Filiere.LA_FILIERE.getActeur("Sup.AO"));
	}

	public void next() {
		super.next();
		this.journalAO.ajouter("=== STEP "+Filiere.LA_FILIERE.getEtape()+" ====================");
		for (Feve f : this.stockFeves.keySet()) {
			if (!f.isEquitable() && this.stockFeves.get(f)<95000) { // pas top...
				int quantite = 5000 + Filiere.random.nextInt((int)(100001-this.stockFeves.get(f))); 
				OffreVente ov = supAO.acheterParAO(this,  cryptogramme, f, quantite);
				journalAO.ajouter("   Je lance un appel d'offre de "+quantite+" T de "+f);
				if (ov!=null) { // on a retenu l'une des offres de vente
					journalAO.ajouter("   AO finalise : on ajoute "+quantite+" T de "+f+" au stock");
					stockFeves.put(f, stockFeves.get(f)+quantite);
					totalStocksFeves.ajouter(this, quantite, cryptogramme);
				}
			}
		}

		// On archive les contrats termines
		this.journalAO.ajouter("=================================");
	}

	public OffreVente choisirOV(List<OffreVente> propositions) {
		// TODO Auto-generated method stub
		BourseCacao bourse = (BourseCacao)(Filiere.LA_FILIERE.getActeur("BourseCacao"));
		double cours = ( bourse.getCours((Feve)propositions.get(0).getProduit())).getValeur();
		if (propositions.get(0).getPrixT()<=cours) {
			return propositions.get(0);
		} else {
			return null;
		}
	}

}
