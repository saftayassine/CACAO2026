package abstraction.eq3Producteur3;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import abstraction.eqXRomu.bourseCacao.BourseCacao;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.encheres.Enchere;
import abstraction.eqXRomu.encheres.IVendeurAuxEncheres;
import abstraction.eqXRomu.encheres.SuperviseurVentesAuxEncheres;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.Feve;

/** @author Guillaume Leroy */
public class Producteur3VendeurAuxEncheres extends Producteur3VendeurCC implements IVendeurAuxEncheres{
	private HashMap<Feve, List<Double>> prixRetenus;
	private double prixMin;

	public Producteur3VendeurAuxEncheres() {
		super();
		this.prixMin=0;
	}

	public void initialiser() {
		super.initialiser();
		this.prixRetenus = new HashMap<Feve, List<Double>>();
		for (Feve f : this.stock.getStockMap().keySet()) {
			this.prixRetenus.put(f, new LinkedList<Double>());
		}		
	}

	public void next() {
		super.next();
		/*int stepActuel = Filiere.LA_FILIERE.getEtape();
		this.journalEncheres.ajouter("=== STEP "+stepActuel+" ====================");
		for (Feve f : this.stock.getStockMap().keySet()) {
			if (f.isEquitable()) continue; // à modifier si les autres équipes peuvent acheter de l'équitable

			double stockActuel = this.stock.getStock(f);
			
			// Calcul des livraisons obligatoires pour le PROCHAIN STEP (stepActuel + 1)
			double livraisonsCCProchainStep = 0;
			for (ExemplaireContratCadre c : this.contratsEnCours) {
				if (c.getProduit().equals(f)) {
					// On regarde ce que l'échéancier impose pour le prochain step
					livraisonsCCProchainStep += c.getEcheancier().getQuantite(stepActuel + 1);
				}
			}
			
			// Récupération de la production estimée pour le prochain step
			double productionEstimeeProchainStep = this.plantationeq3.getProductionFeve(f); 
			
			// Définir un stock de sécurité
			double stockDeSecurite = 1000.0;
			
			// Calcul de la quantité maximale théorique vendable sans risque
			double quantiteMaxVendable = stockActuel + productionEstimeeProchainStep - livraisonsCCProchainStep - stockDeSecurite;
			
			// Décision de lancement de l'enchère
			// On ne lance une enchère que si on a un surplus réel et conséquent à évacuer
			if (quantiteMaxVendable > 5000.0) {
				double quantiteAVendre = 5000.0; // On vend par blocs fixes sûrs
				
				double coutParTonne = 0;
				if (productionEstimeeProchainStep > 0) {
					double coutGlobalFeve = this.gestionCouts.getCoutFeve(f, this);
					coutParTonne = coutGlobalFeve / productionEstimeeProchainStep;
				}
				
				if (coutParTonne > 0) {
					double ratioRemplissage = stockActuel / this.gestionCouts.getSeuilDefenseParFeve(); // Proche de 1.0 si stock critique
					double marge = 1.25 - (ratioRemplissage * 0.15); // La marge varie entre +25% (stock vide) et +10% (stock plein)
					this.prixMin = coutParTonne * marge;
				} else {
					this.prixMin = 1500.0; // Prix de secours
				}
				Enchere enchere = superviseur.vendreAuxEncheres(this, cryptogramme, f, quantiteAVendre);
				journalEncheres.ajouter("   Je lance une enchere de "+quantiteAVendre+" T de "+f);
				if (enchere!=null) { // on a retenu l'une des encheres faites
					journalEncheres.ajouter("   Enchere finalisee : on retire "+quantiteAVendre+" T de "+f+" du stock");
					this.stock.retireStock(f, quantiteAVendre);
					prixRetenus.get(f).add(enchere.getPrixTonne());
					if (prixRetenus.get(f).size()>10) {
						prixRetenus.get(f).remove(0); // on ne garde que les dix derniers prix
						journalEncheres.ajouter("   Les derniers prix pour "+f+" sont "+prixRetenus.get(f));
					}
				}
			}else {
				journalEncheres.ajouter("   Pas assez de marge sur la fève " + f + " pour risquer une enchère (Max vendable calculé : " + quantiteMaxVendable + "t)");
			}
		}*/
		// On archive les contrats termines
		this.journalEncheres.ajouter("=================================");
	}


	
	public Enchere choisir(List<Enchere> encheres) {
		this.journalEncheres.ajouter("encheres : "+encheres);
		if (encheres==null) {
			return null;
		} else {
			Enchere retenue = encheres.get(0);
			if (retenue.getPrixTonne()>this.prixMin) {
				this.journalEncheres.ajouter("  --> je choisis "+retenue);
				return retenue;
			} else {
				this.journalEncheres.ajouter("  --> je ne retiens rien");
				return null;
			}
		}
	}

}
