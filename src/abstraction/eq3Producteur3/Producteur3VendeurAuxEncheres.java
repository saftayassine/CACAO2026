package abstraction.eq3Producteur3;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import abstraction.eqXRomu.bourseCacao.BourseCacao;
import abstraction.eqXRomu.encheres.Enchere;
import abstraction.eqXRomu.encheres.IVendeurAuxEncheres;
import abstraction.eqXRomu.encheres.SuperviseurVentesAuxEncheres;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.Feve;

public class Producteur3VendeurAuxEncheres extends Producteur3VendeurCC implements IVendeurAuxEncheres{
	private HashMap<Feve, List<Double>> prixRetenus;
	private SuperviseurVentesAuxEncheres supEncheres;
	protected Journal journalEncheres;

	public Producteur3VendeurAuxEncheres() {
		super();
		this.journalEncheres = new Journal(" journal Encheres EQ3", this);
	}

	public void initialiser() {
		super.initialiser();
		this.supEncheres = (SuperviseurVentesAuxEncheres)(Filiere.LA_FILIERE.getActeur("Sup.Encheres"));
		this.prixRetenus = new HashMap<Feve, List<Double>>();
		for (Feve f : this.stock.getStockMap().keySet()) {
			this.prixRetenus.put(f, new LinkedList<Double>());
		}		
	}

	public void next() {
		super.next();
		this.journalEncheres.ajouter("=== STEP "+Filiere.LA_FILIERE.getEtape()+" ====================");
		for (Feve f : this.stock.getStockMap().keySet()) {
			if (this.stock.getStock(f)>5000) { // on ne lance pas une enchere pour moins de 5000 T
				Double quantite = 5000.0  ; // il faudrait aussi tenir compte des contrats cadres en cours afin de ne pas vendre ce qu'on s'est engage a livrer
				Enchere enchere = supEncheres.vendreAuxEncheres(this, cryptogramme, f, quantite);
				journalEncheres.ajouter("   Je lance une enchere de "+quantite+" T de "+f);
				if (enchere!=null) { // on a retenu l'une des encheres faites
					journalEncheres.ajouter("   Enchere finalisee : on retire "+quantite+" T de "+f+" du stock");
					this.stock.retireStock(f, quantite);
					prixRetenus.get(f).add(enchere.getPrixTonne());
					if (prixRetenus.get(f).size()>10) {
						prixRetenus.get(f).remove(0); // on ne garde que les dix derniers prix
						journalEncheres.ajouter("   Les derniers prix pour "+f+" sont "+prixRetenus.get(f));
					}
				}
			}
		}

		// On archive les contrats termines
		this.journalEncheres.ajouter("=================================");
	}

	public double prixMoyen(Feve f) {
		List<Double> prix=prixRetenus.get(f);
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
		double prixMoyen = prixMoyen((Feve)(propositions.get(0).getMiseAuxEncheres().getProduit()));
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
		jx.add(journalEncheres);
		return jx;
	}

}
