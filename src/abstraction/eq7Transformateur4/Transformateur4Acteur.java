package abstraction.eq7Transformateur4;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.IProduit;

public class Transformateur4Acteur implements IActeur {
	
	protected int cryptogramme;
	protected Journal journal; //Aymeric
	private StockEq7 stock_Equitable;
	private StockEq7 stock_PasEquitable;
	private Variable LQ; //Indicateur LQ Equitable + pas equitable
	private Variable MQ; //Idem pour MQ
	private Variable HQ; //Idem pour HQ
	protected Variable StockChoco_BQ;
	protected Variable StockChoco_MQ;
	protected Variable StockChoco_HQ;
	public Transformateur4Acteur() {
		//Aymeric
		this.journal = new Journal("Journal equipe 7 (transformateur)", this);
		this.stock_Equitable = new StockEq7(this);
		this.stock_PasEquitable = new StockEq7(this);
		
		//Matteo
		this.LQ=new Variable("LQ", this,0);
		this.MQ=new Variable("MQ",this,0);
		this.HQ=new Variable("HQ", this, 0);
		
		//Paul
		this.StockChoco_BQ=new Variable("StockChoco_BQ", this, 0);
		this.StockChoco_MQ=new Variable("StockChoco_MQ", this, 0);
		this.StockChoco_HQ=new Variable("StockChoco_HQ", this, 0);	


	}
	
	public void initialiser() {
	}

	public String getNom() {// NE PAS MODIFIER
		return "EQ7";
	}
	
	public String toString() {// NE PAS MODIFIER
		return this.getNom();
	}

	////////////////////////////////////////////////////////
	//         En lien avec l'interface graphique         //
	////////////////////////////////////////////////////////

	public void next() {
		//Aymeric
		int etape=Filiere.LA_FILIERE.getEtape();
		this.journal.ajouter("Etape "+ String.valueOf(etape));

		//Matteo
		this.LQ.setValeur(this,this.stock_Equitable.getLowQ()+this.stock_PasEquitable.getLowQ());
		this.MQ.setValeur(this,this.stock_Equitable.getMedQ()+this.stock_PasEquitable.getMedQ());
		this.HQ.setValeur(this,this.stock_Equitable.getHighQ()+this.stock_PasEquitable.getHighQ());

		//Matteo
		this.stock_Equitable.next();
		this.stock_PasEquitable.next();
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
		//Matteo
		res.add(this.LQ);
		res.add(this.MQ);
		res.add(this.HQ);
		

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

	//Matteo
	public StockEq7 get_EqStock(){
		return this.stock_Equitable;
	}
	public StockEq7 get_Stock(){
		return this.stock_PasEquitable;
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

	// Appelle lorsqu'un acteur fait faillite (potentiellement vous)
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

	//Matteo
	public Variable get_StockChoco_HQ(){
		return this.StockChoco_HQ;
	}

	public Variable get_StockChoco_MQ(){
		return this.StockChoco_MQ;
	}

	public Variable get_StockChoco_BQ(){
		return this.StockChoco_BQ;
	}

	public Variable get_LQ(){
		return this.LQ;
	}

	public Variable get_MQ(){
		return this.MQ;
	}

	public Variable get_HQ(){
		return this.HQ;
	}

	public double getQuantiteEnStock(IProduit p, int cryptogramme) {
		if (this.cryptogramme==cryptogramme) { // c'est donc bien un acteur assermente qui demande a consulter la quantite en stock
			if (p == Chocolat.C_BQ) {
				return this.StockChoco_BQ.getValeur();
			} else if (p == Chocolat.C_MQ) {
				return this.StockChoco_MQ.getValeur();
			} else if (p == Chocolat.C_HQ) {
				return this.StockChoco_HQ.getValeur();
			} else if (p instanceof ChocolatDeMarque) {
				// Pour ChocolatDeMarque, on pourrait avoir des stocks séparés, mais pour l'instant 0
				return 0;
			} else {
				return 0;
			}
		} else {
			return 0; // Les acteurs non assermentes n'ont pas a connaitre notre stock
		}
	}
}




