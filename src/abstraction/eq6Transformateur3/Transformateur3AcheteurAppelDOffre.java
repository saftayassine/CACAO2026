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

/** @author Le Clézio Brevael */

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
			if ((f == Feve.F_HQ_E || f == Feve.F_MQ_E) && this.stockFeve.getQuantite(f) < 95000) {
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
		OffreVente meilleureOffre = propositions.get(0);
		for (OffreVente ov : propositions) {
			if (ov.getPrixT() < meilleureOffre.getPrixT()) {
				meilleureOffre = ov;
			}
		}

		Feve f = (Feve) meilleureOffre.getProduit();
		double prixMaxAcceptable = 0.0;

		double coursDeBaseMQ = bourse.getCours(Feve.F_MQ).getValeur();
		if (f == Feve.F_MQ_E) {
			prixMaxAcceptable = coursDeBaseMQ * 1.20; 
		} else if (f == Feve.F_HQ_E) {
			// Pour le HQ_E, on accepte de payer jusqu'à 50% plus cher que le MQ classique
			prixMaxAcceptable = coursDeBaseMQ * 1.50; 
		}
		if (meilleureOffre.getPrixT() <= prixMaxAcceptable) {
			return meilleureOffre;
		} else {
			return null; // Trop cher, on refuse l'offre
		}
	}

    public List<Journal> getJournaux() {
        List<Journal> res = super.getJournaux();
        res.add(this.journalAOVente);
    return res;
    }


}
