/** @author Yassine Safta */
package abstraction.eq4Transformateur1;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import abstraction.eqXRomu.bourseCacao.BourseCacao;
import abstraction.eqXRomu.encheres.Enchere;
import abstraction.eqXRomu.encheres.IVendeurAuxEncheres;
import abstraction.eqXRomu.encheres.SuperviseurVentesAuxEncheres;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;

public class Transformateur1VendeurEnchere extends Transformateur1VendeurCC implements IVendeurAuxEncheres{
    private HashMap<ChocolatDeMarque, List<Double>> prixRetenus;
	private SuperviseurVentesAuxEncheres supEncheres;
	protected Journal journalVendeurEncheres;

	public Transformateur1VendeurEnchere() {
		super();
		this.journalVendeurEncheres = new Journal(this.getNom()+" journal Vendeur Encheres", this);
	}

	public void initialiser() {
		super.initialiser();
		this.supEncheres = (SuperviseurVentesAuxEncheres)(Filiere.LA_FILIERE.getActeur("Sup.Encheres"));
		this.prixRetenus = new HashMap<ChocolatDeMarque, List<Double>>();
		for (ChocolatDeMarque cm : this.getChocolatsProduits()) {
			this.prixRetenus.put(cm, new LinkedList<Double>());
		}		
	}

	public void next() {
		super.next();
		this.journalVendeurEncheres.ajouter("=== STEP "+Filiere.LA_FILIERE.getEtape()+" ====================");
		for (ChocolatDeMarque cm : this.getChocolatsProduits()) {
			if (this.getStock().get(cm)>5000) { // on ne lance pas une enchere pour moins de 5000 T
				int quantite = 5000 + Filiere.random.nextInt((int)(this.getStock().get(cm)-4990)); // il faudrait aussi tenir compte des contrats cadres en cours afin de ne pas vendre ce qu'on s'est engage a livrer
				Enchere enchere = supEncheres.vendreAuxEncheres(this, cryptogramme, cm, quantite);
				journalVendeurEncheres.ajouter("   Je lance une enchere de "+quantite+" T de "+cm);
				if (enchere!=null) { // on a retenu l'une des encheres faites
					journalVendeurEncheres.ajouter("   Enchere finalisee : on retire "+quantite+" T de "+cm+" du stock");
					this.getStock().put(cm, this.getStock().get(cm)-quantite);
					prixRetenus.get(cm).add(enchere.getPrixTonne());
					if (prixRetenus.get(cm).size()>10) {
						prixRetenus.get(cm).remove(0); // on ne garde que les dix derniers prix
						journalVendeurEncheres.ajouter("   Les derniers prix pour "+cm+" sont "+prixRetenus.get(cm));
					}
				}
			}
		}

		// On archive les contrats termines
		this.journalVendeurEncheres.ajouter("=================================");
	}

	public double prixMoyen(ChocolatDeMarque cm) {
		List<Double> prix=prixRetenus.get(cm);
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

	
	public Enchere choisir(List<Enchere> propositions) {
		double prix = propositions.get(0).getPrixTonne();
		double prixMoyen = prixMoyen((ChocolatDeMarque)(propositions.get(0).getMiseAuxEncheres().getProduit()));
		BourseCacao bourse = (BourseCacao)(Filiere.LA_FILIERE.getActeur("BourseCacao"));
		double cours = bourse.getCours(Feve.F_MQ).getValeur();
		if (prixMoyen==0) {
			if (prix>=cours*2.5) {
				return propositions.get(0);
			}
		} else {
			if (prix>=0.95*prixMoyen && prix>cours*1.5) {
				return propositions.get(0);
			}
		}
		return null;
	}
	
	public List<Journal> getJournaux() {
		List<Journal> jx=super.getJournaux();
		jx.add(journalVendeurEncheres);
		return jx;
	}
}