package abstraction.eq6Transformateur3;
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

/** @author : Pol Bailleul */
public class Transformateur3VendeurAppelDOffre extends Transformateur3VendeurAuxEncheres implements IVendeurAO{
	private HashMap<ChocolatDeMarque, List<Double>> prixAO;
	protected Journal journalAO;

	public Transformateur3VendeurAppelDOffre() {
		super();
		this.journalAO = new Journal(" Journal Vente Appel d'Offre", this);
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

	public void next() {
    	super.next();
    	this.journalAO.ajouter("=== Étape " + Filiere.LA_FILIERE.getEtape() + " ===");
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
			double quantite= Double.min(Double.min(offre.getQuantiteT(),this.getStockPrevuProduit(cm)),this.getStockProduit(cm));
			if (quantite>100){
				AppelDOffre newoffre= new AppelDOffre(offre.getAcheteur(), cm, quantite,offre.getTeteGondole());
				return new OffreVente(newoffre, this, cm, px);
			}
			else{
				return null;
			}
		} else {
			double quantite= Double.min(Double.min(offre.getQuantiteT(),this.getStockPrevuProduit(cm)),this.getStockProduit(cm));
			if (quantite>100){
				AppelDOffre newoffre= new AppelDOffre(offre.getAcheteur(), cm, quantite,offre.getTeteGondole());
				return new OffreVente(newoffre, this, cm, prixMoyen(cm)*1.05);
			}
			else{
				return null;
			}
		}
	}

	public void notifierVenteAO(OffreVente propositionRetenue) {
		ChocolatDeMarque cm = (ChocolatDeMarque)(propositionRetenue.getProduit());
		double px = propositionRetenue.getPrixT();
		double quantite = propositionRetenue.getQuantiteT();
		prixAO.get(cm).add(px);
		journalAO.ajouter("   Vente par AO de "+quantite+" T de "+cm+" au prix de  "+px);
		if (prixAO.get(cm).size()>10) {
			prixAO.get(cm).remove(0); 
		}
		this.setStockProduit(cm, this.getStockProduit(cm)-quantite);
		this.setStockPrevuProduit(cm, this.getStockPrevuProduit(cm)-quantite);
	}


	public void notifierPropositionNonRetenueAO(OffreVente propositionRefusee) {
		ChocolatDeMarque cm = (ChocolatDeMarque)(propositionRefusee.getProduit());
		double px = propositionRefusee.getPrixT();
		double quantite = propositionRefusee.getQuantiteT();
		prixAO.get(cm).add(px*0.95);
		journalAO.ajouter("   Echec de vente par AO de "+quantite+" T de "+cm+" au prix de  "+px);
		if (prixAO.get(cm).size()>10) {
			prixAO.get(cm).remove(0);
		}
	}

}
