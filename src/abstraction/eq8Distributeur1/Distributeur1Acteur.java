package abstraction.eq8Distributeur1;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.IProduit;

public class Distributeur1Acteur implements IActeur {
	
	protected Journal journal0;/** @author Ewen Landron */
	protected Variable volumeStock;/** @author Alexandre Cornet */
	protected HashMap<IProduit, Double> Rayon;/** @author Alexandre Cornet */
	protected int cryptogramme;/** @author Alexandre Cornet */
	protected HashMap<IProduit, Double> Stock;/** @author Alexandre Cornet */
	protected double TailleRayon;/** @author Alexandre Cornet */
	protected double volumerayon;/** @author Alexandre Cornet */

	public Distributeur1Acteur() {
		this.journal0 = new Journal("Journal Eq 8 numéro étape ", this);
		this.volumeStock=new Variable("volumeStock", this); /** @author Alexandre Cornet */
		this.Rayon = new HashMap<IProduit, Double>(); /** @author Alexandre Cornet */
		this.Stock = new HashMap<IProduit,Double>();/** @author Alexandre Cornet */
		this.TailleRayon = 100.0;/** @author Alexandre Cornet */
		this.volumerayon = 0.0;/** @author Alexandre Cornet */
	}
	
	public void initialiser() {
		/** @author Alexandre Cornet */
		List<ChocolatDeMarque> p=Filiere.LA_FILIERE.getChocolatsProduits();
		for (int i=0; i<p.size(); i++){
			this.Stock.put((IProduit)(p.get(i)),200.0);
			this.Rayon.put((IProduit)(p.get(i)),0.0);
			this.volumeStock.ajouter(this,getQuantiteEnStock((IProduit)(p.get(i)),this.cryptogramme));
		}
	}

	public String getNom() {// NE PAS MODIFIER
		return "EQ8";
	}
	
	public String toString() {// NE PAS MODIFIER
		return this.getNom();
	}

	////////////////////////////////////////////////////////
	//         En lien avec l'interface graphique         //
	////////////////////////////////////////////////////////

	public void next() {
		this.journal0.ajouter("Numéro de tour : " + Filiere.LA_FILIERE.getEtape());/** @author Ewen Landron */
		this.getvolumestock();/** @author Alexandre Cornet */
	}

	public Color getColor() {// NE PAS MODIFIER
		return new Color(209, 179, 221);
	}

	/** @author Alexandre Cornet */
	public Variable getvolumestock(){
		List<ChocolatDeMarque> p=Filiere.LA_FILIERE.getChocolatsProduits();
		this.volumeStock.setValeur(this,0.0);
		for (int i=0; i<p.size(); i++){
			this.volumeStock.ajouter(this,getQuantiteEnStock((IProduit)(p.get(i)),this.cryptogramme));
		}
		return this.volumeStock;
	}

	/** @author Alexandre Cornet */
	public double getvolumerayon(){
		List<ChocolatDeMarque> p=Filiere.LA_FILIERE.getChocolatsProduits();
		this.volumerayon=0.0;
		for (int i=0; i<p.size(); i++){
			this.volumerayon+=getQuantiteEnStock((IProduit)(p.get(i)),this.cryptogramme);
		}
		return this.volumerayon;
	}

	/** @author Alexandre Cornet */
	public String AjoutenRayon(IProduit p ,double d){
		double v = this.getvolumerayon();
		double q = this.getQuantiteEnStock(p, this.cryptogramme);
		double f = this.getQuantiteEnRayon(p, this.cryptogramme);
		if(v+d>this.TailleRayon){
			String  s="il n'y a pas assez de place dans le rayon";
			return s;
		}else if(q>d){
			String s="vous n'avez pas assez de stock pour ajouter cette quantité";
			return s;
		}else{
			this.Rayon.put(p,f+d);
			this.Stock.put(p,q-d);
			String s="Vous avez ajouté le produit en rayon";
			return s;
		}

	}

	public String getDescription() {
		return "Bla bla bla";
	}

	// Renvoie les indicateurs
	/** @author Alexandre Cornet */
	public List<Variable> getIndicateurs() {
		List<Variable> res = new ArrayList<Variable>();
		res.add(this.volumeStock);
		return res;
	}

	// Renvoie les parametres
	public List<Variable> getParametres() {
		List<Variable> res=new ArrayList<Variable>();
		return res;
	}

	// Renvoie les journaux
	/** @author Ewen Landron */
	public List<Journal> getJournaux() {
		List<Journal> res=new ArrayList<Journal>();
		res.add(this.journal0);
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

	/** @author Alexandre Cornet */
	public double getQuantiteEnStock(IProduit p, int cryptogramme) {
		if (this.cryptogramme==cryptogramme) { // c'est donc bien un acteur assermente qui demande a consulter la quantite en stock
			return this.Stock.get(p);
		} else {
			return 0; // Les acteurs non assermentes n'ont pas a connaitre notre stock
		}
	}

	/** @author Alexandre Cornet */
	public double getQuantiteEnRayon(IProduit p, int cryptogramme) {
		if (this.cryptogramme==cryptogramme) { // c'est donc bien un acteur assermente qui demande a consulter la quantite en stock
			return this.Rayon.get(p);
		} else {
			return 0; // Les acteurs non assermentes n'ont pas a connaitre notre stock
		}
	}
}
