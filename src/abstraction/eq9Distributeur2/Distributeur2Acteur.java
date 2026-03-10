package abstraction.eq9Distributeur2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.IProduit;

public class Distributeur2Acteur implements IActeur {
	protected int cryptogramme;
	protected Journal journal;
	protected Map<IProduit, Double> stock;
	protected Variable indicateurStockTotal;

	public Distributeur2Acteur() {
		this.journal = new Journal("Journal EQ9", this);
		this.stock = new HashMap<>();
		this.indicateurStockTotal = new Variable("EQ9_stock_total", this, 0.0);
	}
	    
	
	// Ajoute 100 tonnes d'un produit en rayon 
		/**
         * @author Anass Ouisrani
         */ 
	public void initialiser() {
		this.stock.clear();
		List<abstraction.eqXRomu.produits.ChocolatDeMarque> produits = abstraction.eqXRomu.filiere.Filiere.LA_FILIERE.getChocolatsProduits();
		if (produits != null && !produits.isEmpty()) {
			IProduit produit = produits.get(0);
			this.stock.put(produit, 100000.0); // 100 tonnes = 100000 kg
		}
		this.indicateurStockTotal.setValeur(this, getStockTotal());
	}

	public String getNom() {// NE PAS MODIFIER
		return "EQ9";
	}
	
	public String toString() {// NE PAS MODIFIER
		return this.getNom();
	}

	////////////////////////////////////////////////////////
	//         En lien avec l'interface graphique         //
	////////////////////////////////////////////////////////
     
		/**
         * @author Paul Juhel
		 * @author Anass Ouisrani
         */ 
	public void next() {
		int etape = Filiere.LA_FILIERE.getEtape();
		this.journal.ajouter("ETAPE" + etape);
		List<abstraction.eqXRomu.produits.ChocolatDeMarque> produits = abstraction.eqXRomu.filiere.Filiere.LA_FILIERE.getChocolatsProduits();
		if (produits != null && !produits.isEmpty()) {
			IProduit produit = produits.get(0);
			double quantiteActuelle = this.stock.getOrDefault(produit, 0.0);
			this.stock.put(produit, quantiteActuelle + 100000.0);
		// Mettre à jour l'indicateur de stock total
		this.indicateurStockTotal.setValeur(this, getStockTotal());
		}
	}

	private double getStockTotal() {
		double total = 0.0;
		for (double q : stock.values()) {
			total += q;
		}
		return total;
	}

	public Color getColor() {// NE PAS MODIFIER
		return new Color(245, 155, 185); 
	}

	public String getDescription() {
		return "Bla bla bla";
	}
     
		/**
         * @author Anass Ouisrani
         */ 
	// Renvoie les indicateurs
	public List<Variable> getIndicateurs() {
		List<Variable> res = new ArrayList<Variable>();
		res.add(indicateurStockTotal);
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
		res.add(journal);
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
        if (this.cryptogramme==cryptogramme) { 
            return this.stock.getOrDefault(p, 0.0); 
        } else {
            return 0; 
        }
    }
}
