package abstraction.eq1Producteur1;

import java.util.HashMap;
import java.util.List;

import abstraction.eqXRomu.bourseCacao.IVendeurBourse;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.general.Journal;
import java.awt.Color;

/** * @author Elise Dossal & Théophile Trillat
 */
public class Producteur1VendeurBourse extends Producteur1VendeurContratCadre implements IVendeurBourse{

    private int blacklist=0;
	protected Journal journalBourse;


    public Producteur1VendeurBourse(){
        super();
		this.journalBourse = new Journal("Journal " + this.getNom()+" journal Bourse", this);
    }


	/**
	 * Calcule combien de tonnes de f vont encore être livrées via les CC
	 * entre maintenant et la fin de l'année en cours.
	 *
	 * C'est crucial pour éviter de vendre en bourse du stock qu'on doit garder
	 * pour honorer les contrats cadres jusqu'à la fin de l'année.
	 */
	protected double getLivraisonsCCRestantesCetteAnnee(Feve f) {
		int etape = Filiere.LA_FILIERE.getEtape();
		int finAnnee = etape + 23 - (etape % 24);
		double total = 0;

		for (ExemplaireContratCadre contrat : this.contratsEnCours) {
			if (contrat.getProduit() == f) {
				// quantité prévue entre maintenant (étape actuelle) et la fin de l'année
				double qteFinAnnee = contrat.getEcheancier().getQuantiteJusquA(finAnnee);
				double qteDejaPassee = etape > 0 ? contrat.getEcheancier().getQuantiteJusquA(etape - 1) : 0;
				total += Math.max(0.0, qteFinAnnee - qteDejaPassee);
			}
		}
		return total;
	}


	/**
	 * Retourne la quantite en tonnes de feves de type f que le vendeur
	 * souhaite vendre a cette etape sachant que le cours actuel de
	 * la feve f est cours
	 */
	public double offre(Feve f, double cours){
		if (blacklist > 0){
			journalBourse.ajouter(Color.RED, Color.white, "Blacklist active ("+blacklist+" steps restants) → aucune vente");
			blacklist--;
			return 0;
		}

		int etape = Filiere.LA_FILIERE.getEtape();
		int stepDansAnnee = etape % 24;

		// On ne commence la vente en bourse qu'après la période de négociation des CC
		if (stepDansAnnee < this.periode) {
			return 0.;
		}

		double stockActuel = getStock(f);
		if (stockActuel <= 0){
			journalBourse.ajouter("Stock nul pour "+f+" → aucune vente");
			return 0;
		}

		if (cours < 600 && (f == Feve.F_BQ || f == Feve.F_MQ)){
			journalBourse.ajouter(Color.ORANGE, Color.white, "Prix trop bas ("+cours+" €/t) pour "+f+" → vente refusée");
			return 0;
		}

		// ===============================s==========================
		// NOUVELLE LOGIQUE : on calcule à partir du STOCK ACTUEL
		// (et non du stockRef) pour éviter le double comptage
		// avec les livraisons CC déjà en cours.
		// =========================================================

		double stockRef = this.stockDebutAnnee.getOrDefault(f, 0.0);

		// 1. Marge de sécurité : calculée via le pourcentage équivalent à 500t
		double plafondEngagement = this.getPlafondEngagement(f);
		double margeSecuritePct = 100.0 - plafondEngagement;
		double margeSecurite = stockRef * (margeSecuritePct / 100.0); 

		// 2. Stock réservé aux livraisons CC FUTURES de cette année
		//    (ce qui reste à livrer aux CC entre maintenant et fin d'année)
		double livraisonsCCFutures = this.getLivraisonsCCRestantesCetteAnnee(f);

		// 3. Stock "protégé" qu'on ne doit pas vendre en bourse
		double stockProtege = margeSecurite + livraisonsCCFutures;

		// 4. Ce qui est réellement disponible pour la bourse
		double stockDisponible = Math.max(0.0, stockActuel - stockProtege);

		if (stockDisponible <= 0.1) {
			journalBourse.ajouter("Stock disponible nul pour "+f
				+" | stock actuel : "+String.format("%.1f", stockActuel)+" t"
				+" | marge sécurité : "+String.format("%.1f", margeSecurite)+" t"
				+" | livraisons CC à venir : "+String.format("%.1f", livraisonsCCFutures)+" t"
				+" → aucune vente");
			return 0;
		}

		// 5. Lissage sur les étapes restantes de l'année
		int stepsRestants = 24 - stepDansAnnee;
		double quantite = stockDisponible / stepsRestants;

		// 6. ANCIEN PLAFOND SUPPRIMÉ (on laisse l'algorithme faire son calcul)
		// On s'assure juste de ne pas proposer des poussières par erreur
		if (quantite < 1.0) {
			return 0;
		}

		journalBourse.ajouter(Color.BLUE, Color.white,
			"Offre : "+String.format("%.1f", quantite)+" t. de "+f
			+" | stock actuel : "+String.format("%.1f", stockActuel)+" t"
			+" | protégé (marge+CC futurs) : "+String.format("%.1f", stockProtege)+" t"
			+" | disponible bourse : "+String.format("%.1f", stockDisponible)+" t"
			+" | sur "+stepsRestants+" steps restants");

		return quantite;
    }


	/**
	 * Methode appelee par la bourse pour avertir le vendeur qu'il est parvenu
	 * a vendre quantiteEnT tonnes
	 */
	public double notificationVente(Feve f, double quantiteEnT, double coursEnEuroParT){
        double vrai_quantite= Math.min(quantiteEnT,getStock(f));
        this.takeFeve(f, vrai_quantite);
		double revenu = vrai_quantite * coursEnEuroParT;
		journalBourse.ajouter(Color.GREEN, Color.white, "Vente réalisée : "+vrai_quantite+" tonnes de "+f+ " à "+coursEnEuroParT+" €/t → revenu = "+revenu+" €");

        return vrai_quantite;
    }

	/**
	 * Methode appelee par la bourse pour avertir le vendeur qu'il vient d'etre ajoute a la black list
	 */
	public void notificationBlackList(int dureeEnStep){
        this.blacklist = dureeEnStep;
		journalBourse.ajouter(Color.RED, Color.white, "BLACKLIST : exclusion de la bourse pendant "+dureeEnStep+" steps");
    }

	public List<Journal> getJournaux() {
		List<Journal> res=super.getJournaux();
		res.add(this.journalBourse);
		return res;
	}
}