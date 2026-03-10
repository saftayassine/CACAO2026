package abstraction.eq4Transformateur1;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.IProduit;


public class Transformateur1Acteur implements IActeur {
	/**@author Ewan Lefort */
	private Journal journal= new Journal("numéro de l'étape",this );
	Transformateur1Stock stock= new Transformateur1Stock();
	protected int cryptogramme;

	public Transformateur1Acteur() {
	}
	/**@author Ewan Lefort */
	public void initialiser() 
	{if (Filiere.LA_FILIERE.getEtape()>=0){
		journal.ajouter("etape:"+Filiere.LA_FILIERE.getEtape());
		
	}

	}

	public String getNom() {// NE PAS MODIFIER
		return "EQ4";
	}
	
	public String toString() {// NE PAS MODIFIER
		return this.getNom();
	}

	////////////////////////////////////////////////////////
	//         En lien avec l'interface graphique         //
	////////////////////////////////////////////////////////
	/**@author Ewan Lefort */
	public void next() {
		if (Filiere.LA_FILIERE.getEtape()>=0){
			int etape= Filiere.LA_FILIERE.getEtape()+1;
		journal.ajouter("etape:"+etape);}
	}

	public Color getColor() {// NE PAS MODIFIER
		return new Color(229, 243, 157); 
	}

	public String getDescription() {
		return "Bla bla bla";
	}
	/**@author Ewan Lefort */
	// Renvoie les indicateurs.
	public List<Variable> getIndicateurs() {
		List<Variable> res = new ArrayList<Variable>();
		Variable totalstocks= new Variable("EQ4T Total Stocks", "<html>Quantite totale de feves en stock</html>", this,0);
		totalstocks.setValeur(this, this.stock.getTotalStocks(), cryptogramme);
		res.add(totalstocks);
		return res;
	}

	// Renvoie les parametres
	public List<Variable> getParametres() {
		List<Variable> res=new ArrayList<Variable>();
		return res;
	}
	/**@author Ewan Lefort */
	// Renvoie les journaux.
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
		if (this.cryptogramme==cryptogramme) { // c'est donc bien un acteur assermente qui demande a consulter la quantite en stock
			return 0; // A modifier
		} else {
			return 0; // Les acteurs non assermentes n'ont pas a connaitre notre stock
		}
	}
}
