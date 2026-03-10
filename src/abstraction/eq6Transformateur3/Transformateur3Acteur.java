package abstraction.eq6Transformateur3;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;

public class Transformateur3Acteur implements IActeur {
	
	protected Journal journal = new Journal("Journal Eq6", this);
	protected int cryptogramme;
	protected Transformateur3Stocks stocks;

	public Transformateur3Acteur() {
		this.stocks = new Transformateur3Stocks(this);
	}
	
	public void initialiser() {
		this.stocks.initialiserStocks(this, this.cryptogramme);
		journal.ajouter("Stocks initialisés :");
		for (Feve f : this.stocks.getStockFeves().keySet()) {
			double q = this.stocks.getStockFeves().get(f);
			journal.ajouter("Fèves " + f.getGamme() + " " + (f.isEquitable() ? "équitable" : "non équitable") + " : " + q + " kg");
		}
		for (Chocolat c : this.stocks.getStockChoco().keySet()) {
			double q = this.stocks.getStockChoco().get(c);
			journal.ajouter("Chocolat " + c.getGamme() + " " + (c.isEquitable() ? "équitable" : "non équitable") + " : " + q + " kg");
		}
		journal.ajouter("Volume total = " + this.stocks.getTotalStockVolume().getValeur() + " kg");
	}

	public String getNom() {// NE PAS MODIFIER
		return "EQ6";
	}
	
	public String toString() {// NE PAS MODIFIER
		return this.getNom();
	}

	////////////////////////////////////////////////////////
	//         En lien avec l'interface graphique         //
	////////////////////////////////////////////////////////

	public void next() {
		int etape = Filiere.LA_FILIERE.getEtape();
		journal.ajouter("Étape " + etape);
		journal.ajouter("=== État des stocks ===");
		for (Feve f : this.stocks.getStockFeves().keySet()) {
			double q = this.stocks.getStockFeves().get(f);
			if (q > 0) {
				journal.ajouter("Fèves " + f.getGamme() + " " + (f.isEquitable() ? "équitable" : "non équitable") + " : " + q + " kg");
			}
		}
		for (Chocolat c : this.stocks.getStockChoco().keySet()) {
			double q = this.stocks.getStockChoco().get(c);
			if (q > 0) {
				journal.ajouter("Chocolat " + c.getGamme() + " " + (c.isEquitable() ? "équitable" : "non équitable") + " : " + q + " kg");
			}
		}
		for (ChocolatDeMarque cm : this.stocks.getStockChocoMarque().keySet()) {
			double q = this.stocks.getStockChocoMarque().get(cm);
			if (q > 0) {
				journal.ajouter("Chocolat de marque " + cm.getMarque() + " : " + q + " kg");
			}
		}
		journal.ajouter("Volume total de stock : " + this.stocks.getTotalStockVolume().getValeur() + " kg");
	}

	public Color getColor() {// NE PAS MODIFIER
		return new Color(158, 242, 226); 
	}

	public String getDescription() {
		return "Bla bla bla";
	}

	// Renvoie les indicateurs
	public List<Variable> getIndicateurs() {
		List<Variable> res = new ArrayList<Variable>();
		res.add(this.stocks.getTotalStockVolume());
		return res;
	}

	// Renvoie les parametres
	public List<Variable> getParametres() {
		List<Variable> res=new ArrayList<Variable>();
		return res;
	}

	// Renvoie les journaux
	public List<Journal> getJournaux() {
		List<Journal> res=new ArrayList<Journal>();
		res.add(this.journal);
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

	public Transformateur3Stocks getStocks() {
		return this.stocks;
	}

	public double getQuantiteEnStock(IProduit p, int cryptogramme) {
		if (this.cryptogramme==cryptogramme) { // c'est donc bien un acteur assermente qui demande a consulter la quantite en stock
			if (this.stocks != null) {
				return this.stocks.getQuantiteEnStock(p);
			} else {
				return 0.0;
			}
		} else {
			return 0; // Les acteurs non assermentes n'ont pas a connaitre notre stock
		}
	}
}
