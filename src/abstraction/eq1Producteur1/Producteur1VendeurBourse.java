package abstraction.eq1Producteur1;

import java.util.HashMap;
import java.util.List;

import abstraction.eqXRomu.bourseCacao.IVendeurBourse;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.general.Journal;
import java.awt.Color;

/** * @author Elise Dossal & Théophile Trillat
 */
public class Producteur1VendeurBourse extends Producteur1VendeurContratCadre implements IVendeurBourse{
///*
    private int blacklist=0;
	protected Journal journalBourse;


    public Producteur1VendeurBourse(){
        super();
		this.journalBourse = new Journal("Journal " + this.getNom()+" journal Bourse", this);
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

		if (stepDansAnnee >= this.periode) {  // vendre après une certaine période du cycle

			double stockActuel = getStock(f);

			if (stockActuel <= 0){
				journalBourse.ajouter("Stock nul pour "+f+" → aucune vente");
				return 0;
			}

			if (cours < 600 && f == Feve.F_BQ){
				journalBourse.ajouter(Color.ORANGE, Color.white, "Prix trop bas ("+cours+" €/t) pour "+f+" → vente refusée");
				return 0;
			}

			if (cours < 600 && f == Feve.F_MQ){
				journalBourse.ajouter(Color.ORANGE, Color.white, "Prix trop bas ("+cours+" €/t) pour "+f+" → vente refusée");
				return 0;
			}

			// --- CALCUL DE LA QUANTITÉ À ÉCOULER ---
			
			// 1. Récupération des données du début d'année
			double pourcentEngage = this.pourcentageAVendre.getOrDefault(f, 0.0);
			double stockRef = this.stockDebutAnnee.getOrDefault(f, 0.0);
			
			// 2. Ce qu'on ne doit SURTOUT PAS vendre (Contrats Cadres + marge de 5% du stock initial)
			double quantiteReserveeCC = stockRef * (pourcentEngage / 100.0);
			double margeSecurite = stockRef * 0.05; 
			double stockIntouchable = quantiteReserveeCC + margeSecurite;
			
			// 3. Ce qu'il nous reste de "libre" à écouler pour vider les stocks
			double stockAecouler = Math.max(0.0, stockActuel - stockIntouchable);

			if (stockAecouler <= 0.1) {
				journalBourse.ajouter("Objectif de stock atteint ou dépassé pour "+f+" (reste "+stockActuel+" t) → aucune vente en bourse");
				return 0;
			}

			// 4. Lissage sur le reste de l'année
			// Il reste (24 - stepDansAnnee) étapes pour vendre cette quantité
			int stepsRestants = 24 - stepDansAnnee;
			double quantite = stockAecouler / stepsRestants;

			// Maintien d'un plafond max par sécurité (ex: 20000)
			quantite = Math.min(quantite, 20000);

			journalBourse.ajouter(Color.BLUE, Color.white, "Offre : "+quantite+" t. de "+f+" (à écouler : "+stockAecouler+" t. sur "+stepsRestants+" steps restants)");

			return quantite;
		}

		return 0.;
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
//*/