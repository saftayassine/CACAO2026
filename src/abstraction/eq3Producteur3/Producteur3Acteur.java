package abstraction.eq3Producteur3;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.general.VariableReadOnly;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;

public class Producteur3Acteur implements IActeur {
	private Journal journal_periode;
	protected int cryptogramme;
	protected Producteur3Stock stock; 
	public Variable StockTotal;
	public Plantation3 plantationeq3;
	protected Journal journal_vente_bouse;
	private Journal journal_stock;
	private Gestion_couts3 gestionCouts;
	public Journal journal_cout_periode;
	public Agriculteurs3 agriculteurs;

	public Producteur3Acteur() {
		/** @author Vassili Spiridonov */
		this.journal_periode = new Journal("Journal des périodes EQ3", this); 
		this.journal_vente_bouse = new Journal("Journal Ventes en bourse EQ3", this);
		this.journal_stock = new Journal("Journal des Stocks détaillé EQ3", this);
		this.journal_cout_periode = new Journal("Journal des coûts par période", this);

		/** @author Guillaume Leroy */
		this.stock = new Producteur3Stock(this.journal_stock);
		this.StockTotal= new VariableReadOnly(this + " Stock total", this, this.stock.getStockTotal());
		this.plantationeq3= new Plantation3();
		this.gestionCouts = new Gestion_couts3();
		this.agriculteurs = new Agriculteurs3(this.plantationeq3);
	}
	
	public void initialiser() {
		/** @author Guillaume Leroy */
		this.stock.addStock(Feve.F_BQ , 250.0);
		this.stock.addStock(Feve.F_MQ , 250.0);
		this.stock.addStock(Feve.F_HQ , 250.0);
	}

	public String getNom() {// NE PAS MODIFIER
		return "EQ3";
	}
	
	public String toString() {// NE PAS MODIFIER
		return this.getNom();
	}

	////////////////////////////////////////////////////////
	//         En lien avec l'interface graphique         //
	////////////////////////////////////////////////////////

	public void next() {
		// défi 1 
		this.journal_periode.ajouter("période : "+ Filiere.LA_FILIERE.getEtape()); /** @author Vassili Spiridonov */
		/** @author Guillaume Leroy */
		for (Feve f : List.of(Feve.F_BQ, Feve.F_MQ, Feve.F_HQ)){
			this.stock.addStock(f, this.plantationeq3.getProductionFeve(f)); // ajoute le nouveau stock de fève et fait vieillir le restant
			// vente des feves par contrat, en bourse .... (à faire après avoir implémenter la classe)
		}
		// défi 2
		this.mettreAJourIndicateurStock(); /** @author Guillaume Leroy */
		this.gestionCouts.nextCout(this);//fait payer le coût de stockage final des feves et impôt sur le nombre d'hectare de plantation
		this.plantationeq3.nextStep(); // permet de gérer nos hectares de plantation pour la V1
		this.stock.recapJournal();
	}

	public Color getColor() {// NE PAS MODIFIER
		return new Color(249, 230, 151); 
	}

	public String getDescription() {
		return "Bla bla bla";
	}

	// Renvoie les indicateurs
	public List<Variable> getIndicateurs() {
		/** @author Guillaume Leroy */
		List<Variable> res = new ArrayList<Variable>();
		res.add(this.StockTotal);
		return res;
	}

	public void mettreAJourIndicateurStock() {
		/** @author Guillaume Leroy */
    	this.StockTotal.setValeur(this, this.stock.getStockTotal(), this.cryptogramme);
	}

	// Renvoie les parametres
	public List<Variable> getParametres() {
		List<Variable> res=new ArrayList<Variable>();
		return res;
	}

	// Renvoie les journaux
	public List<Journal> getJournaux() {
		/** @author Vassili Spiridonov */
		List<Journal> res=new ArrayList<Journal>(); 
		res.add(this.journal_periode);
		res.add(journal_vente_bouse);
		res.add(journal_stock);
		res.add(journal_cout_periode);
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

	public double getQuantiteEnStock(IProduit p, int cryptogramme) {
		if (this.cryptogramme==cryptogramme) { // c'est donc bien un acteur assermente qui demande a consulter la quantite en stock
			return 0; // A modifier
		} else {
			return 0; // Les acteurs non assermentes n'ont pas a connaitre notre stock
		}
	}
}
