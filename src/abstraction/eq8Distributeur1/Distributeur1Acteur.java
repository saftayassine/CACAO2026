package abstraction.eq8Distributeur1;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import abstraction.eqXRomu.clients.ClientFinal;
import abstraction.eqXRomu.filiere.Banque;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.filiere.IDistributeurChocolatDeMarque;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.IProduit;

public class Distributeur1Acteur implements IActeur, IDistributeurChocolatDeMarque {
	
	protected Journal journal0;/** @author Ewen Landron */
	protected Journal journal1;/** @author Alexandre Cornet */
	protected Journal journal2;/** @author Alexandre Cornet */
	protected Journal journal3;/** @author Alexandre Cornet */
	protected Journal journal4;/** @author Alexandre Cornet */
	protected Journal journal5;/** @author Ewen Landron */
	protected Variable volumeStock;/** @author Alexandre Cornet */
	protected HashMap<IProduit, Double> Rayon;/** @author Alexandre Cornet */
	protected int cryptogramme;/** @author Alexandre Cornet */
	protected HashMap<IProduit, Double> Stock;/** @author Alexandre Cornet */
	protected HashMap<IProduit, Double> Prix;/** @author Alexandre Cornet */
	protected double TailleRayon;/** @author Alexandre Cornet */
	protected double volumerayon;/** @author Alexandre Cornet */
	protected HashMap<ChocolatDeMarque, Double> ChocolatsAchetes;/** @author Lucas Levillain */
	/**
         * @author Alexandre Cornet
		 * @author Ewen Landron
		 * @author Lucas Levillain
         */ 
	public Distributeur1Acteur() {
		this.journal0 = new Journal("Journal EQ8 étapes ", this);
		this.journal1 = new Journal("Journal EQ8 Rayon ", this);
		this.journal2 = new Journal("Journal EQ8 Stock ", this);
		this.journal3 = new Journal("Journal EQ8 Actions ", this);
		this.journal4 = new Journal("Journal EQ8 Frais ", this);
		this.journal5 = new Journal("Journal EQ8 Contrats ", this);
		this.volumeStock=new Variable("EQ8 StockTotal", this); 
		this.Rayon = new HashMap<IProduit, Double>(); 
		this.Stock = new HashMap<IProduit, Double>();
		this.Prix = new HashMap<IProduit, Double>();
		this.ChocolatsAchetes = new HashMap<ChocolatDeMarque, Double>();
		this.TailleRayon = 1000.0;
		this.volumerayon = 0.0;
	}
	/** @author Alexandre Cornet */
	public void initialiser() {
		List<ChocolatDeMarque> p=Filiere.LA_FILIERE.getChocolatsProduits();
		for (int i=0; i<p.size(); i++){
			this.Stock.put((IProduit)(p.get(i)),500.0);
			this.Rayon.put((IProduit)(p.get(i)),0.0);
			this.Prix.put((IProduit)(p.get(i)),8000.0);
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
	/**
         * @author Alexandre Cornet
		 * @author Ewen Landron
         */ 
	public void next() {
		List<ChocolatDeMarque> p=Filiere.LA_FILIERE.getChocolatsProduits();
		Banque b=Filiere.LA_FILIERE.getBanque();
		Variable v=this.getvolumestock();
		double v1=v.getValeur();

		//JournalActions
		this.journal3.ajouter("Numéro de tour : " + Filiere.LA_FILIERE.getEtape());
		for (int i=0; i<p.size(); i++){
			this.journal3.ajouter(AjoutenRayon(p.get(i), 100));
		}
		this.journal3.ajouter(changerTailleRayon(0));
		this.journal3.ajouter("----------------------------------------------");

		//Journal Étapes
		this.journal0.ajouter("Numéro de tour : " + Filiere.LA_FILIERE.getEtape());

		//Journal Rayon
		this.journal1.ajouter("Numéro de tour : " + Filiere.LA_FILIERE.getEtape());
		this.journal1.ajouter("Taille du Rayon : "+this.TailleRayon+"T");
		this.journal1.ajouter("Quantité en rayon : "+this.volumerayon+"T");
		for (int i=0; i<p.size(); i++){
			double q=this.getQuantiteEnRayon(p.get(i),this.cryptogramme);
			this.journal1.ajouter(p.get(i)+" : "+q+"T");
		}
		this.journal1.ajouter("----------------------------------------------");

		//Journal Stock
		this.journal2.ajouter("Numéro de tour : " + Filiere.LA_FILIERE.getEtape());
		for (int i=0; i<p.size(); i++){
			double q=this.getQuantiteEnStock(p.get(i),this.cryptogramme);
			this.journal2.ajouter(p.get(i)+" : "+q+"T");
		}
		this.journal2.ajouter("----------------------------------------------");

		//Journal Frais
		if(this.volumerayon<this.TailleRayon){
			this.TailleRayon=this.volumerayon;
		}
		this.journal4.ajouter("Numéro de tour : " + Filiere.LA_FILIERE.getEtape());
		b.payerCout(this, this.cryptogramme, "Frais de Rayonnage", TailleRayon*0.01);
		this.journal4.ajouter("Frais de Rayon : "+TailleRayon*0.01 +" €");
		b.payerCout(this, this.cryptogramme, "Frais de Stockage", v1*0.01);
		this.journal4.ajouter("Frais de Stockage : "+v1*0.01+" €");
		this.journal4.ajouter("----------------------------------------------");
		
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
			this.volumerayon+=getQuantiteEnRayon((IProduit)(p.get(i)),this.cryptogramme);
		}
		return this.volumerayon;
	}

	/** @author Alexandre Cornet */
	public String changerTailleRayon(double d){
		//Banque b=Filiere.LA_FILIERE.getBanque();
		this.TailleRayon+=d;
		if(d>=0){
			//b.payerCout(this, this.cryptogramme, "Achat de Rayonnage", 0);
			return ("La taille du rayon a été augmentée de "+d+"T");
		}else{
			d=-d;
			double v=this.getvolumerayon();
			if(this.TailleRayon<v){
				this.TailleRayon+=d;
				return("Il y a trop de quantité en rayon pour baisser la taille du rayon");
			}else{
				//b.payerCout(this, this.cryptogramme, "Vente de Rayonnage", 0);
				return ("La taille du rayon a été diminuée de "+d+"T");
			}
		}
		
	}


	/** @author Alexandre Cornet */
	public String AjoutenRayon(IProduit p ,double d){
		double v = this.getvolumerayon();
		double q = this.getQuantiteEnStock(p, this.cryptogramme);
		double f = this.getQuantiteEnRayon(p, this.cryptogramme);
		if(v+d>this.TailleRayon){
			String  s="il n'y a pas assez de place dans le rayon";
			return s;
		}else if(q<d){
			String s="vous n'avez pas assez de stock pour ajouter cette quantité";
			return s;
		}else{
			this.Rayon.put(p,f+d);
			this.Stock.put(p,q-d);
			String s="Vous avez ajouté " + d + "T de " + p + " en rayon.";
			this.volumerayon+=d;
			this.volumeStock.ajouter(this, -d);
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
		res.add(this.journal1);
		res.add(this.journal2);
		res.add(this.journal3);
		res.add(this.journal4);
		return res;
	}

		// Renvoie les couts
	/** @author Lucas Levillain */
	public HashMap<String, Double> getCouts() {
		HashMap<String, Double> couts = new HashMap<String, Double>();

		double coutStockage = 0.0;
		double coutMiseEnRayon = 0.0;
		double coutStockageParTonne = 120.0;
		double coutMiseEnRayonParTonne = 0.0

		List<ChocolatDeMarque> p = Filiere.LA_FILIERE.getChocolatsProduits();
		for (int i = 0; i < p.size(); i++) {
			IProduit prod = (IProduit) p.get(i);
			coutStockage += getQuantiteEnStock(prod, this.cryptogramme) * coutStockageParTonne;
			coutMiseEnRayon += getQuantiteEnRayon(prod, this.cryptogramme) * coutMiseEnRayonParTonne;
		}

		couts.put("stockage", coutStockage);
		couts.put("miseEnRayon", coutMiseEnRayon);
		return couts;
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
	/** @author Alexandre Cornet */
	public double getPrixProduit(IProduit p, int cryptogramme) {
		if (this.cryptogramme==cryptogramme) { // c'est donc bien un acteur assermente qui demande a consulter la quantite en stock
			/** @author Lucas Levillain */ 
			this.Prix(p).put(IProduit)(p, value : (CoutParArticle.getOrDefault() + prixDAchat.getOrDefault())*1,1)	
			return this.Prix.get(p);
		} else {
			return 0; // Les acteurs non assermentes n'ont pas a connaitre notre stock
		}
	}

	/** @author Alexandre Cornet */
	@Override
	public double prix(ChocolatDeMarque choco) {
		return this.getPrixProduit(choco, this.cryptogramme);
	}
	/** @author Alexandre Cornet */
	@Override
	public double quantiteEnVente(ChocolatDeMarque choco, int crypto) {
		return this.getQuantiteEnRayon(choco, this.cryptogramme);
	}
	/** @author Alexandre Cornet */
	@Override
	public double quantiteEnVenteTG(ChocolatDeMarque choco, int crypto) {
		return 0.0;
		
	}
	/** @author Alexandre Cornet */
	@Override
	public void vendre(ClientFinal client, ChocolatDeMarque choco, double quantite, double montant, int crypto) {
		double v=getQuantiteEnRayon(choco,this.cryptogramme);
		this.Rayon.put((IProduit)(choco),v-quantite);

	}
	/** @author Alexandre Cornet */
	@Override
	public void notificationRayonVide(ChocolatDeMarque choco, int crypto) {
		this.journal0.ajouter("Rayon de "+choco+" en rupture");
		
	}
}
