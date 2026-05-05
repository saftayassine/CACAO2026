package abstraction.eq8Distributeur1;

import java.util.List;

import abstraction.eqXRomu.filiere.Banque;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.ChocolatDeMarque;

public class Distributeur1 extends MiseEnRayon {
	
	public Distributeur1() {
		super();
	}

	/**
    * @author Alexandre Cornet
	* @author Ewen Landron
    */ 
	public void next() {
		super.next();
		List<ChocolatDeMarque> p=Filiere.LA_FILIERE.getChocolatsProduits();
		Banque b=Filiere.LA_FILIERE.getBanque();
		Variable v=this.getvolumestock();
		this.getvolumerayon();
		double v1=v.getValeur();
		double volumeCibleTotal = 3600000.0;

		// On remet tout les produits du rayon en stock pour simplifier les calculs
		for (int i = 0; i < p.size(); i++) {
            double q = this.getQuantiteEnStock(p.get(i), this.cryptogramme);
			double f = this.getQuantiteEnRayon(p.get(i), this.cryptogramme);
            this.Rayon.put(p.get(i),0.0);
			this.Stock.put(p.get(i),q+f);
        }
		//Choix de l'acteur
		this.trierChocolatsParPrix();
		if (Filiere.LA_FILIERE.getEtape() == 1) {
			this.initialiserPrixReferenceUniquementChocolats();
		}
		this.lancerApprovisionnementGeneral(volumeCibleTotal);
		this.executerMiseEnRayon();
		for (int j=0; j<p.size(); j++){
			double f=this.getQuantiteEnRayon(p.get(j),this.cryptogramme);
		}
		this.actualiserPrixDachatParContrats();

		//JournalActions
		this.journal3.ajouter("Numéro de tour : " + Filiere.LA_FILIERE.getEtape());
		/**
		for (int i=0; i<p.size(); i++){
			this.journal3.ajouter(AjoutenRayon(p.get(i), 100));
		}
		*/ 
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

		/** 
		//Journal Frais
		if(this.volumerayon<this.TailleRayon){
			this.TailleRayon=this.volumerayon;
		}
		*/
		this.journal4.ajouter("Numéro de tour : " + Filiere.LA_FILIERE.getEtape());
		b.payerCout(this, this.cryptogramme, "Frais de Rayonnage", TailleRayon*0.01);
		this.journal4.ajouter("Frais de Rayon : "+TailleRayon*0.01 +" €");
		if(v1!=0){
			b.payerCout(this, this.cryptogramme, "Frais de Stockage", v1*0.01);
			this.journal4.ajouter("Frais de Stockage : "+v1*0.01+" €");
		}
	
		this.journal4.ajouter("----------------------------------------------");
		
	}

}
