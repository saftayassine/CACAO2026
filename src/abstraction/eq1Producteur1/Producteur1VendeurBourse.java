package abstraction.eq1Producteur1;

import java.util.HashMap;
import java.util.List;

import abstraction.eqXRomu.bourseCacao.IVendeurBourse;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.general.Journal;
import java.awt.Color;

public class Producteur1VendeurBourse extends Producteur1VendeurContratCadre implements IVendeurBourse{

    private int blacklist=0;
	protected Journal journalBourse;

    public Producteur1VendeurBourse(){
        super();
		this.journalBourse = new Journal("Journal " + this.getNom()+" journal Bourse", this);
    }

	// On calcule ce qu'il nous reste à livrer cette année via nos contrats cadres, 
	// pour s'assurer de ne pas vendre par accident ces fèves promises en bourse.
	protected double getLivraisonsCCRestantesCetteAnnee(Feve f) {
		int etape = Filiere.LA_FILIERE.getEtape();
		int finAnnee = etape + 23 - (etape % 24);
		double total = 0;

		for (ExemplaireContratCadre contrat : this.contratsEnCours) {
			if (contrat.getProduit() == f) {
				double qteFinAnnee = contrat.getEcheancier().getQuantiteJusquA(finAnnee);
				double qteDejaPassee = etape > 0 ? contrat.getEcheancier().getQuantiteJusquA(etape - 1) : 0;
				total += Math.max(0.0, qteFinAnnee - qteDejaPassee);
			}
		}
		return total;
	}

	public double offre(Feve f, double cours){
		if (blacklist > 0){
			journalBourse.ajouter(Color.RED, Color.white, "Blacklist active ("+blacklist+" steps restants) → aucune vente");
			blacklist--;
			return 0;
		}

		int etape = Filiere.LA_FILIERE.getEtape();
		int stepDansAnnee = etape % 24;

		// On laisse d'abord se dérouler les négociations de début d'année avant d'aller sur la bourse.
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

		// --- Calcul de l'offre en bourse ---
		// On part de notre stock réel immédiat pour éviter de proposer des fèves qu'on ne possède plus.
		double stockRef = this.stockDebutAnnee.getOrDefault(f, 0.0);

		// Étape 1 : On sécurise notre réserve intouchable (équivalente à 500 tonnes).
		double plafondEngagement = this.getPlafondEngagement(f);
		double margeSecuritePct = 100.0 - plafondEngagement;
		double margeSecurite = stockRef * (margeSecuritePct / 100.0); 

		// Étape 2 : On sanctuarise les fèves promises à nos acheteurs réguliers pour l'année en cours.
		double livraisonsCCFutures = this.getLivraisonsCCRestantesCetteAnnee(f);

		// On fait la somme de tout ce qui ne doit surtout pas être vendu.
		double stockProtege = margeSecurite + livraisonsCCFutures;

		// Étape 3 : Ce qui reste est notre véritable excédent disponible.
		double stockDisponible = Math.max(0.0, stockActuel - stockProtege);

		if (stockDisponible <= 0.1) {
			journalBourse.ajouter("Stock disponible nul pour "+f
				+" | stock actuel : "+String.format("%.1f", stockActuel)+" t"
				+" | marge sécurité : "+String.format("%.1f", margeSecurite)+" t"
				+" | livraisons CC à venir : "+String.format("%.1f", livraisonsCCFutures)+" t"
				+" → aucune vente");
			return 0;
		}

		// Étape 4 : On propose tout notre excédent d'un coup pour répondre à la demande de la bourse.
		double quantite = stockDisponible;

		// On évite d'inonder le marché avec des micro-quantités ridicules.
		if (quantite < 1.0) {
			return 0;
		}

		journalBourse.ajouter(Color.BLUE, Color.white,
			"Offre : "+String.format("%.1f", quantite)+" t. de "+f
			+" | stock actuel : "+String.format("%.1f", stockActuel)+" t"
			+" | protégé (marge+CC futurs) : "+String.format("%.1f", stockProtege)+" t"
			+" | disponible bourse : "+String.format("%.1f", stockDisponible)+" t");

		return quantite;
    }

	public double notificationVente(Feve f, double quantiteEnT, double coursEnEuroParT){
        double vrai_quantite= Math.min(quantiteEnT,getStock(f));
        this.takeFeve(f, vrai_quantite);
		double revenu = vrai_quantite * coursEnEuroParT;
		journalBourse.ajouter(Color.GREEN, Color.white, "Vente réalisée : "+vrai_quantite+" tonnes de "+f+ " à "+coursEnEuroParT+" €/t → revenu = "+revenu+" €");

        return vrai_quantite;
    }

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