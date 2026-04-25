/**@author Yassine Safta */
package abstraction.eq4Transformateur1;
import abstraction.eqXRomu.appelDOffre.AppelDOffre;
import abstraction.eqXRomu.appelDOffre.IVendeurAO;
import abstraction.eqXRomu.appelDOffre.OffreVente;
import abstraction.eqXRomu.appelDOffre.SuperviseurVentesAO;
import abstraction.eqXRomu.bourseCacao.BourseCacao;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;
import abstraction.eqXRomu.produits.IProduit;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Transformateur1VendeurAppelDOffre extends Transformateur1AcheteurCC implements IVendeurAO{
	private HashMap<ChocolatDeMarque, List<Double>> prixAO;
	protected Journal journalAO;

	public Transformateur1VendeurAppelDOffre() {
		super();
		this.journalAO = new Journal(this.getNom()+" journal A.O.", this);
	}

	public void initialiser() {
		super.initialiser();
		this.prixAO = new HashMap<ChocolatDeMarque, List<Double>>();
		for (ChocolatDeMarque cm : this.getChocolatsProduits()) {
			this.prixAO.put(cm, new LinkedList<Double>());
		}		
	}
	public double prixMoyen(ChocolatDeMarque cm) {
		List<Double> prix=prixAO.get(cm);
		if (prix.size()>0) {
			double somme =0.0;
			
			for (Double d : prix) {
				somme+=d;
			}
			return somme/prix.size();
		} else {
			return 0.0;
		}
	}

	public List<Journal> getJournaux() {
		List<Journal> jx=super.getJournaux();
		jx.add(journalAO);
		return jx;
	}


	public OffreVente proposerVente(AppelDOffre offre) {
		IProduit p = offre.getProduit();
		if (!(p instanceof ChocolatDeMarque)) {
			return null;
		}
		ChocolatDeMarque cm = (ChocolatDeMarque)p;
		if (!(this.getChocolatsProduits().contains(cm))) {
			return null;
		}
		if (prixAO.get(cm).size()==0) {
			BourseCacao bourse = (BourseCacao)(Filiere.LA_FILIERE.getActeur("BourseCacao"));
			double px = bourse.getCours(Feve.F_MQ).getMax()*1.75;
			if (cm.getChocolat().getGamme()==Gamme.HQ) {
				px = bourse.getCours(Feve.F_MQ).getMax()*2.5;
			} else if (cm.getChocolat().getGamme()==Gamme.BQ) {
				px = bourse.getCours(Feve.F_BQ).getMax()*1.75;
			}
			return new OffreVente(offre, this, cm, px);
		} else {
			return new OffreVente(offre, this, cm, prixMoyen(cm)*1.05);
		}
//		return null;
	}

	public void notifierVenteAO(OffreVente propositionRetenue) {
		ChocolatDeMarque cm = (ChocolatDeMarque)(propositionRetenue.getProduit());
		double px = propositionRetenue.getPrixT();
		double quantite = propositionRetenue.getQuantiteT();
		prixAO.get(cm).add(px); // on fait comme si on avait accepte avec 5% d'augmentation afin que lors des prochains echanges on accepte des prix un peu plus eleves
		journalAO.ajouter("   Vente par AO de "+quantite+" T de "+cm+" au prix de  "+px);
		if (prixAO.get(cm).size()>10) {
			prixAO.get(cm).remove(0); // on ne garde que les dix derniers prix
		}
		this.setStocksProduit(cm,this.getStocksProduit(cm)+quantite);
		this.setStocksPrevuProduit(cm,this.getStocksPrevuProduit(cm)+quantite);

	}


	public void notifierPropositionNonRetenueAO(OffreVente propositionRefusee) {
		ChocolatDeMarque cm = (ChocolatDeMarque)(propositionRefusee.getProduit());
		double px = propositionRefusee.getPrixT();
		double quantite = propositionRefusee.getQuantiteT();
		prixAO.get(cm).add(px*0.92); // on fait comme si on avait accepte avec 8% de baisse afin que lors des prochains echanges on fasse une meilleure offre
		journalAO.ajouter("   Echec de vente par AO de "+quantite+" T de "+cm+" au prix de  "+px);
		if (prixAO.get(cm).size()>10) {
			prixAO.get(cm).remove(0); // on ne garde que les dix derniers prix
		}
	}

}

