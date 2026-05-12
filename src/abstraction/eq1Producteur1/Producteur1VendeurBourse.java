package abstraction.eq1Producteur1;

import java.util.HashMap;
import java.util.List;

import abstraction.eqXRomu.bourseCacao.IVendeurBourse;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.general.Journal;
import java.awt.Color;

/** 
 * @author Elise Dossal & Théophile Trillat
 */
public class Producteur1VendeurBourse extends Producteur1VendeurContratCadre implements IVendeurBourse{
///*
    private int blacklist=0;
	protected HashMap<Feve , Double > pourcentageAVendre = new HashMap<Feve , Double>();
	protected Journal journalBourse;


    public Producteur1VendeurBourse(){
        super();
		this.journalBourse = new Journal("Journal " + this.getNom()+" journal Bourse", this);

    }



	/**
	 * Retourne la quantite en tonnes de feves de type f que le vendeur 
	 * souhaite vendre a cette etape sachant que le cours actuel de 
	 * la feve f est cours
	 * @param f le type de feve
	 * @param cours le cours actuel des feves de type f
	 * @return la quantite en tonnes de feves de type f que this souhaite vendre 
	 */
	public double offre(Feve f, double cours){
		if (blacklist > 0){
			journalBourse.ajouter(Color.RED, Color.white, "Blacklist active ("+blacklist+" steps restants) → aucune vente");
			blacklist--;
			return 0;
		}

		int etape = Filiere.LA_FILIERE.getEtape();
		if (etape % 24 >= this.periode) {  // vendre après une certaine période du cycle

			double stock = getStock(f);

			if (stock <= 0){
				journalBourse.ajouter("Stock nul pour "+f+" → aucune vente");
				return 0;
			}

			if (cours < 2800 && f == Feve.F_BQ){
				journalBourse.ajouter(Color.ORANGE, Color.white, "Prix trop bas ("+cours+" €/t) pour "+f+" → vente refusée");
				return 0;
			}

			if (cours < 3300 && f == Feve.F_MQ){
				journalBourse.ajouter(Color.ORANGE, Color.white, "Prix trop bas ("+cours+" €/t) pour "+f+" → vente refusée");
				return 0;
			}

			double quantite = 0.05*stock;

			quantite = Math.min(quantite, 20000);

			journalBourse.ajouter(Color.BLUE, Color.white, "Offre : "+quantite+" tonnes de "+f+" au cours de "+cours+" €/t (stock="+stock+")");

			return quantite;
		}

		return 0.;

    }


	/**
	 * Methode appelee par la bourse pour avertir le vendeur qu'il est parvenu
	 * a vendre quantiteEnT tonnes de feve f au prix de coursEnEuroParT euros par tonne.
	 * L'acteur this doit determiner la quantite qu'il livre reellement et destocker cette
	 * quantite. 
	 * La quantite quantiteEnT est inferieure ou egale a ce que le vendeur this a specifie
	 * vouloir vendre, et il doit donc normalement etre en mesure de retirer cette 
	 * quantite de ses sotcks afin de la livrer et de retourner quantiteEnT. 
	 * Mais il se peut qu'il retourne une quantite inferieure si ses stocks de feve f ne 
	 * sont pas suffisants pour livrer quantiteEnT tonnes.
	 * Remarque : le superviseur s'occupe des virements, vendeurs et acheteurs n'ont pas a
	 *  les gerer
	 * @return la quantite en tonnes de feves de type f rellement livree (retiree du stock) 
	 */
	public double notificationVente(Feve f, double quantiteEnT, double coursEnEuroParT){
        double vrai_quantite= Math.min(quantiteEnT,getStock(f));
        this.takeFeve(f, vrai_quantite);
		double revenu = vrai_quantite * coursEnEuroParT;
		journalBourse.ajouter(Color.GREEN, Color.white, "Vente réalisée : "+vrai_quantite+" tonnes de "+f+ " à "+coursEnEuroParT+" €/t → revenu = "+revenu+" €");

        return vrai_quantite;
    }

	/**
	 * Methode appelee par la bourse pour avertir le vendeur qu'il vient 
	 * d'etre ajoute a la black list : l'acteur a precise une quantite qu'il desirait 
	 * mettre en vente qu'il n'a pas pu honorer (il n'a pu livrer qu'une quantite insuffisante)
	 * this ne pourra pas vendre en bourse pendant la duree precisee en 
	 * parametre 
	 */
	public void notificationBlackList(int dureeEnStep){
        this.blacklist = dureeEnStep;
		journalBourse.ajouter(Color.RED, Color.white, "BLACKLIST : exclusion de la bourse pendant "+dureeEnStep+" steps");
    }

	public List<Journal> getJournaux() {
		List<Journal> res=super.getJournaux();
		res.add(this.journalBourse);
		return res;
	}


}

//*/
