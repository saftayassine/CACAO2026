package abstraction.eq1Producteur1;

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

public class Producteur1Acteur implements IActeur {

	protected Journal journal;
	protected int cryptogramme;
	protected Variable stockTot;
	protected Journal journalBanque;

	public Producteur1Acteur() {
		this.journal = new Journal("Journal "+this.getNom(), this);
		this.journalBanque = new Journal("Journal "+this.getNom()+ " banque", this);

		// Jauge visuelle pour suivre le remplissage global de nos entrepôts sur l'interface du jeu.
		this.stockTot = new VariableReadOnly("stock de EQ1", "<html>Stock de EQ1</html>",this, 0.0, 1000000000.0, 6000.0);
	}
	
	public void initialiser() {
	}

	// Identifiant lu par le simulateur, à ne pas modifier.
	public String getNom() {
		return "EQ1";
	}
	
	public String toString() {
		return this.getNom();
	}

	public void next() {
		int etape = Filiere.LA_FILIERE.getEtape();
		// À chaque nouveau tour, on marque l'étape dans notre journal de bord.
		this.journal.ajouter("Etape : "+ String.valueOf(etape));
	}

	public Color getColor() {
		return new Color(243, 165, 175); 
	}

	public String getDescription() {
		return "Bla bla bla";
	}

	// Fournit nos variables à l'interface graphique.
	public List<Variable> getIndicateurs() {
		List<Variable> res = new ArrayList<Variable>();
		res.add(this.stockTot);
		return res;
	}

	public List<Variable> getParametres() {
		List<Variable> res=new ArrayList<Variable>();
		return res;
	}

	public List<Journal> getJournaux() {
		List<Journal> res=new ArrayList<Journal>();
		res.add(this.journal);
		res.add(this.journalBanque);
		return res;
	}

	// Le banquier nous donne notre code secret en début de partie. On le garde précieusement.
	public void setCryptogramme(Integer crypto) {
		this.cryptogramme = crypto;
	}

	public void notificationFaillite(IActeur acteur) {
	}

	public void notificationOperationBancaire(double montant) {
	}
	
	protected double getSolde() {
		return Filiere.LA_FILIERE.getBanque().getSolde(Filiere.LA_FILIERE.getActeur(getNom()), this.cryptogramme);
	}

	public List<String> getNomsFilieresProposees() {
		ArrayList<String> filieres = new ArrayList<String>();
		return(filieres);
	}

	public Filiere getFiliere(String nom) {
		return Filiere.LA_FILIERE;
	}

	public double getQuantiteEnStock(IProduit p, int cryptogramme) {
		// On filtre les curieux : on ne donne l'information de nos stocks qu'aux instances officielles qui ont le bon mot de passe.
		if (this.cryptogramme==cryptogramme) { 
			return 0; 
		} else {
			return 0; 
		}
	}
}