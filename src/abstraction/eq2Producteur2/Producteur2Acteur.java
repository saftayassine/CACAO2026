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


public class Producteur2Acteur extends Producteur2couts implements IActeur {
	/** @author Thomas */
	protected HashMap<Feve,Double> fevesSeches = new HashMap<Feve, Double>();
	protected Journal journal = new Journal("Journal Eq2", this);
	protected Journal JournalBanque  = new Journal("Journal Banque Eq2", this);
	protected Journal journalContratCadre = new Journal("Journal Contrat Cadre Eq2", this);
	protected List<Plantation> plantations;

	/** @author Thomas */
	public Producteur2Acteur() {
		for (Feve f : Feve.values()) {
			this.fevesSeches.put(f, 0.0);
		}
		this.stockTotal = new Variable("Stock Total EQ2", this, 0.0);
		this.plantations = new ArrayList<Plantation>();
		// Initialiser le journal des coûts avec l'acteur correct
		this.JournalCout = new Journal("Journal Coûts Eq2", this);
	}
	
	/** @author Thomas */
	public void initialiser() {
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
		double total = 0.0;
		for (Feve f : Feve.values()) {
			Variable v = this.stockvar.get(f);
			double valeur = v != null ? v.getValeur() : 0.0;
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
			Variable v = this.stockvar.get(f);
			return v != null ? v.getValeur(this.cryptogramme) : 0.0;
		} else {
			return 0; // Les acteurs non assermentes n'ont pas a connaitre notre stock
		}
	}


}
