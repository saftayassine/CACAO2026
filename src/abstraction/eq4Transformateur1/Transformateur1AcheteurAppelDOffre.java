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
			if (f instanceof Feve && !((Feve) f).isEquitable()) { // pas top...
				int quantite = 5000 + Filiere.random.nextInt((int)(100001-this.getStock().get(f))); 
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
