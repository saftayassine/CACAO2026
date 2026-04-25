package abstraction.eq6Transformateur3;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.bourseCacao.IAcheteurBourse;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;

import abstraction.eqXRomu.general.VariablePrivee;

import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eq6Transformateur3.StockFeve;
import abstraction.eq6Transformateur3.StockChocolat;
public class Transformateur3Acteur implements IActeur {
	
	protected Journal journal = new Journal("Journal Eq6", this);
	protected int cryptogramme;

	protected StockFeve stockFeve;
	protected Variable Eq6TotalStock;
	protected StockChocolat stockChocolat;

	public Transformateur3Acteur() {
		this.stockFeve = new StockFeve();
		this.stockChocolat = new StockChocolat();
		this.Eq6TotalStock = new VariablePrivee("Eq6TotalStock", "<html>Stock total de fèves+chocolats+chocolats de marque</html>", this, 0.0, 1000000.0, 0.0);

	}
	
	public void initialiser() {
		//* @author : Pol Bailleul */
		for (Feve feve : stockFeve.getFeves()) {
			this.journal.ajouter("Stock de "+Journal.texteSurUneLargeurDe(feve+"", 15)+" = "+this.stockFeve.getQuantite(feve));
			this.Eq6TotalStock.ajouter(this, this.stockFeve.getQuantite(feve),this.cryptogramme);
		}
		for (Chocolat choco : stockChocolat.getChocolat()) {
			this.journal.ajouter("Stock de "+Journal.texteSurUneLargeurDe(choco+"", 15)+" = "+this.stockChocolat.getQuantite(choco));
			this.Eq6TotalStock.ajouter(this, this.stockChocolat.getQuantite(choco),this.cryptogramme);
		}

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
		//* @author : Pol Bailleul */
		this.journal.ajouter("=== STOCKS === ");
		for (Feve feve : stockFeve.getFeves()) {
			this.journal.ajouter("Stock de "+Journal.texteSurUneLargeurDe(feve+"", 15)+" = "+this.stockFeve.getQuantite(feve));
		}

		for (Chocolat chocolat : stockChocolat.getChocolat()) {
			this.journal.ajouter("Stock de "+Journal.texteSurUneLargeurDe(chocolat+"", 15)+" = "+this.stockChocolat.getQuantite(chocolat));
		}


		int etape = Filiere.LA_FILIERE.getEtape();
		journal.ajouter("Étape " + etape);
		

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

//		res.add(this.stocks.getTotalStockVolume());

		 //* @author : Pol Bailleul */
		res.add(this.Eq6TotalStock);

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
		//* @author : Pol Bailleul */
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

/*<<<<<<< HEAD
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

*/
	public double getQuantiteEnStock(IProduit p, int cryptogramme) {
		if (this.cryptogramme!=cryptogramme) { // Les acteurs non assermentes n'ont pas a connaitre notre stock
			return 0;
		}
		if (p instanceof Feve) {
			return this.stockFeve.getQuantite((Feve)p);
		}
		if (p instanceof Chocolat) {
			return this.stockChocolat.getQuantite((Chocolat)p);
		}
		return 0;
	}

	/* =============================================================== */
	/*                  IAcheteurBourse implementation                 */
	/* =============================================================== */

	


}
