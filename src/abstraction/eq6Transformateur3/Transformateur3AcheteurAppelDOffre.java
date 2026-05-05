package abstraction.eq6Transformateur3;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import abstraction.eqXRomu.acteurs.TransformateurXAcheteurBourse;
import abstraction.eqXRomu.bourseCacao.BourseCacao;
import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IAcheteurContratCadre;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre;
import abstraction.eqXRomu.contratsCadres.SuperviseurVentesContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;

import abstraction.eqXRomu.appelDOffre.IAcheteurAO;
import abstraction.eqXRomu.appelDOffre.OffreVente;
import abstraction.eqXRomu.appelDOffre.SuperviseurVentesAO;


import abstraction.eq6Transformateur3.StockFeve;
import abstraction.eq6Transformateur3.StockChocolat;


public class Transformateur3AcheteurAppelDOffre extends Transformateur3VendeurAppelDOffre implements IAcheteurAO {
	private SuperviseurVentesAO supAO;
    private Journal journalAOVente;

	public Transformateur3AcheteurAppelDOffre() {
		super();
		this.journalAOVente = new Journal(" journal Achat Appel d'Offre EQ6", this);
	}
	public void initialiser() {
		super.initialiser();
		this.supAO = (SuperviseurVentesAO)(Filiere.LA_FILIERE.getActeur("Sup.AO"));
	}



	public void next() {
		super.next();
		this.journalAOVente.ajouter("=== STEP "+Filiere.LA_FILIERE.getEtape()+" ====================");
		for (Feve f : this.stockFeve.getFeves()) {
			if (!f.isEquitable() && this.stockFeve.getQuantite(f)<95000) { // pas top...
				int quantite = 5000 + Filiere.random.nextInt((int)(100001-this.stockFeve.getQuantite(f))); 
				OffreVente ov = supAO.acheterParAO(this,  cryptogramme, f, quantite);
				journalAOVente.ajouter("   Je lance un appel d'offre de "+quantite+" T de "+f);
				if (ov!=null) { // on a retenu l'une des offres de vente
					journalAOVente.ajouter("   AO finalise : on ajoute "+quantite+" T de "+f+" au stock");
					stockFeve.ajouterQuantite(f, quantite);
				}
			}
		}

		// On archive les contrats termines
		this.journalAOVente.ajouter("=================================");
	}

	public OffreVente choisirOV(List<OffreVente> propositions) {
		BourseCacao bourse = (BourseCacao)(Filiere.LA_FILIERE.getActeur("BourseCacao"));
		double cours = ( bourse.getCours((Feve)propositions.get(0).getProduit())).getValeur();
		if (propositions.get(0).getPrixT()<=cours) {
			return propositions.get(0);
		} else {
			return null;
		}
	}

    public List<Journal> getJournaux() {
        List<Journal> res = super.getJournaux();
        res.add(this.journalAOVente);
    return res;
    }


}
