package abstraction.eq2Producteur2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.bourseCacao.IVendeurBourse;

public class Producteur2Acteur extends Producteur2Stock implements IActeur, IVendeurBourse {
	/** @author Thomas */
	protected HashMap<Feve, Variable> stocks;
	protected HashMap<Feve,Double> fevesSeches;
	protected Journal journal = new Journal("Journal Eq2", this);
	protected Journal JournalBanque  = new Journal("Journal Banque Eq2", this);
	protected Journal journalBourse = new Journal("Journal Bourse Eq2", this);
	protected Journal journalContratCadre = new Journal("Journal Contrat Cadre Eq2", this);
	protected List<Plantation> plantations;
	protected Producteur2couts stockManager;

	/** @author Thomas */
	public Producteur2Acteur() {

		this.stocks = new HashMap<Feve, Variable>();
		for (Feve f : Feve.values()) {
			this.stocks.put(f, new Variable("Stock " + f, this, 0.0));
		}
		this.stockTotal = new Variable("Stock Total EQ2", this, 0.0);
		this.plantations = new ArrayList<Plantation>();
		this.stockManager = new Producteur2couts();
		for (Feve f : Feve.values()) {
			this.stocks.get(f).setValeur(this, this.stockManager.stock_initial.get(f));
		}
	}
	
	/** @author Thomas */
	public void initialiser() {
    	int ageMature = 72; 
	}

	public String getNom() {// NE PAS MODIFIER
		return "EQ2";
	}
	
	public String toString() {// NE PAS MODIFIER
		return this.getNom();
	}

	////////////////////////////////////////////////////////
	//         En lien avec l'interface graphique         //
	////////////////////////////////////////////////////////
	/** @author Thomas */
	public void next() {
		super.next();
		// Synchronisation des stocks visibles avec le stock interne produit
		double total = 0.0;
		for (Feve f : Feve.values()) {
			Variable v = this.stockvar.get(f);
			double valeur = v != null ? v.getValeur() : 0.0;
			Variable stockActeur = this.stocks.get(f);
			if (stockActeur != null) {
				stockActeur.setValeur(this, valeur);
			}
			total += valeur;
		}
		this.stockTotal.setValeur(this, total);
		journal.ajouter("Numero : " + Filiere.LA_FILIERE.getEtape() + " | Stock total : " + total + " fèves");
	}

	public Color getColor() {// NE PAS MODIFIER
		return new Color(244, 198, 156); 
	}

	public String getDescription() {
		return "Producteur de fèves de cacao simples (BQ, MQ, HQ).";
	}

	// Renvoie les indicateurs
	public List<Variable> getIndicateurs() {
		List<Variable> res = new ArrayList<Variable>();
		res.add(this.stockTotal);
		
		// Ajouter les indicateurs pour chaque type de plantation
		for (Plantation p : this.plantations) {
			res.add(new Variable("Hectares " + p.getTypeFeve(), this, p.getParcelles()));
		}
		
		return res;
	}

	// Renvoie les parametres
	public List<Variable> getParametres() {
		List<Variable> res=new ArrayList<Variable>();
		return res;
	}

	// Renvoie les journaux
	/** @author Thomas */
	public List<Journal> getJournaux() {
		List<Journal> res=new ArrayList<Journal>();
		res.add(this.journal);
		res.add(this.JournalBanque);
		res.add(this.journalBourse);
		res.add(this.journalContratCadre);
		return res;
	}

	////////////////////////////////////////////////////////
	//               En lien avec la Banque               //
	////////////////////////////////////////////////////////

	// Appelee en debut de simulation pour vous communiquer 
	// votre cryptogramme personnel, indispensable pour les
	// transactions.
	public void setCryptogramme(Integer crypto) {
		this.cryptogramme = crypto;
	}

	// Appelee lorsqu'un acteur fait faillite (potentiellement vous)
	// afin de vous en informer.
	public void notificationFaillite(IActeur acteur) {
	}

	// Apres chaque operation sur votre compte bancaire, cette
	// operation est appelee pour vous en informer
	public void notificationOperationBancaire(double montant) {
		this.JournalBanque.ajouter("Opération bancaire : " + montant + "€ | Solde actuel : " + this.getSolde() + "€");
	}
	
	// Renvoie le solde actuel de l'acteur
	protected double getSolde() {
		return Filiere.LA_FILIERE.getBanque().getSolde(Filiere.LA_FILIERE.getActeur(getNom()), this.cryptogramme);
	}

	////////////////////////////////////////////////////////
	//        Pour la creation de filieres de test        //
	////////////////////////////////////////////////////////

	// Renvoie la liste des filieres proposees par l'acteur
	public List<String> getNomsFilieresProposees() {
		ArrayList<String> filieres = new ArrayList<String>();
		return(filieres);
	}

	// Renvoie une instance d'une filiere d'apres son nom
	public Filiere getFiliere(String nom) {
		return Filiere.LA_FILIERE;
	}

	public double getQuantiteEnStock(IProduit p, int cryptogramme) {
		if (this.cryptogramme == cryptogramme && p instanceof Feve) {
			Feve f = (Feve) p;
			Variable v = this.stocks.get(f);
			return v != null ? v.getValeur(this.cryptogramme) : 0.0;
		} else {
			return 0; // Les acteurs non assermentes n'ont pas a connaitre notre stock
		}
	}

	////////////////////////////////////////////////////////
	//             En lien avec la Bourse                //
	////////////////////////////////////////////////////////

	/** @author Thomas */
	@Override
	public double offre(Feve f, double cours) {
		// Vendre uniquement BQ et MQ en bourse
		if (f != Feve.F_BQ && f != Feve.F_MQ) {
			return 0.0;
		}

		this.stockManager.setStockMin(0.1);

		double offre = 0;
		if (this.stockvar.containsKey(f) && this.stockManager.cout_unit_t.containsKey(f) && this.stockManager.seuil_stock.containsKey(f)) {
			double stockActuel = this.stockvar.get(f).getValeur();
			double quantiteAGarder = this.restantDu(f);
			double marge = 1.2;
			double prixMinimal = this.stockManager.cout_unit_t.get(f) * marge;

			this.journalBourse.ajouter(Filiere.LA_FILIERE.getEtape() + " : Cours=" + cours + ", seuil=" + this.stockManager.seuil_stock.get(f) + ", stock=" + stockActuel);

			if ((stockActuel - quantiteAGarder > this.stockManager.seuil_stock.get(f)) && (prixMinimal < cours)) {
				offre = stockActuel - quantiteAGarder - this.stockManager.seuil_stock.get(f);
				this.journalBourse.ajouter(Filiere.LA_FILIERE.getEtape() + " : Je mets en vente " + offre + " T de " + f + " à " + cours + " €/t (prix mini=" + prixMinimal + ")");
			}
		}

		return offre;
	}

	/** @author Simon */
	@Override
	public double notificationVente(Feve f, double quantiteEnT, double coursEnEuroParT) {
		Variable v = this.stocks.get(f);
		double livrable = 0.0;
		if (v != null) {
			livrable = Math.min(quantiteEnT, v.getValeur());
			v.retirer(this, livrable, this.cryptogramme);
			Variable sv = this.stockvar.get(f);
			if (sv != null) {
				sv.retirer(this, livrable, this.cryptogramme);
			}
		}
		journalBourse.ajouter("Vente bourse : " + livrable + " t de " + f + " a " + coursEnEuroParT + "€/t");
		return livrable;
	}

	@Override
	public void notificationBlackList(int dureeEnStep) {
		journalBourse.ajouter("Blacklisté pendant " + dureeEnStep + " étapes");
	}

	public double restantDu(Feve f) {
		return 0.0; // Pas de contrats cadres, donc rien réservé
	}
}
